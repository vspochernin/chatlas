package ru.hackathon.chatlas.telegram;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.hackathon.chatlas.config.BotConfig;
import ru.hackathon.chatlas.excel.ExcelExportService;
import ru.hackathon.chatlas.service.ReportGenerationService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ChatlasBot implements LongPollingSingleThreadUpdateConsumer {

    private static final String COMMAND_START = "/start";
    private static final String COMMAND_HELP = "/help";
    private static final String COMMAND_PROCESS = "/process";
    private static final String COMMAND_CLEAR = "/clear";

    private final TelegramClient telegramClient;
    private final String botToken;
    private final ReportGenerationService reportGenerationService;
    private final ExcelExportService excelExportService;

    /**
     * Хранилище накопленных файлов для каждого пользователя (chatId -> список файлов).
     * Файлы хранятся в памяти только до обработки, затем удаляются (приватность).
     */
    private final Map<Long, List<PendingFile>> pendingFilesByChat = new ConcurrentHashMap<>();

    /**
     * Запись о файле, ожидающем обработки.
     */
    private record PendingFile(String fileName, byte[] content) {
    }

    public ChatlasBot(String botToken,
                     ReportGenerationService reportGenerationService,
                     ExcelExportService excelExportService) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.botToken = botToken;
        this.reportGenerationService = reportGenerationService;
        this.excelExportService = excelExportService;
        log.info("ChatlasBot instance created with service dependencies");
    }

    @Override
    public void consume(Update update) {
        if (update == null) {
            log.warn("Received null update, ignoring");
            return;
        }

        if (!update.hasMessage()) {
            log.debug("Update without message, ignoring: {}", update);
            return;
        }

        Message message = update.getMessage();
        Long chatId = message.getChatId();

        try {
            if (message.hasText() && message.getText().startsWith("/")) {
                handleCommand(chatId, message.getText());
            } else if (message.hasDocument()) {
                handleDocumentMessage(chatId, message.getDocument());
            } else if (message.hasText()) {
                handlePlainTextMessage(chatId);
            } else {
                log.debug("Unsupported message type in chat {}: {}", chatId, message);
            }
        } catch (Exception e) {
            log.error("Error while processing update in chat {}", chatId, e);
            safeSendText(chatId, "Произошла непредвиденная ошибка. Попробуйте ещё раз позже.");
        }
    }

    private void handleCommand(Long chatId, String text) {
        String command = text.split("\\s+", 2)[0];
        switch (command) {
            case COMMAND_START -> sendStartMessage(chatId);
            case COMMAND_HELP -> sendHelpMessage(chatId);
            case COMMAND_PROCESS -> processPendingFiles(chatId);
            case COMMAND_CLEAR -> clearPendingFiles(chatId);
            default -> safeSendText(chatId, "Неизвестная команда. Используйте /start или /help.");
        }
    }

    private void sendStartMessage(Long chatId) {
        String msg = """
                Привет! Я бот Chatlas

                Пришлите мне один или несколько JSON-файлов экспорта чата из Telegram Desktop.
                Я обработаю их и подготовлю список участников / Excel-файл согласно заданию хакатона.

                Если нужна справка - используйте команду /help.
                """.strip();
        safeSendText(chatId, msg);
    }

    private void sendHelpMessage(Long chatId) {
        String msg = String.format("""
                Что я умею:

                - Принимаю JSON-экспорт истории чата (Telegram Desktop → Export chat history → JSON).
                - Можно отправить до %d файлов подряд, они будут накоплены.
                - Для обработки всех накопленных файлов используйте команду /process.
                - Для очистки накопленных файлов используйте команду /clear.
                - Я разберу всех участников и упоминания из всех файлов.
                - Если участников < 50 - отправлю список прямо в чат.
                - Если участников ≥ 51 - сформирую и отправлю Excel-файл.

                Просто отправьте мне .json-файл(ы) и затем /process.
                """, BotConfig.MAX_FILES_PER_USER).strip();
        safeSendText(chatId, msg);
    }

    private void handlePlainTextMessage(Long chatId) {
        String msg = """
                Я жду JSON-файлы экспорта чата

                1) В Telegram Desktop сделайте экспорт истории чата в формате JSON.
                2) Пришлите полученный .json-файл сюда как документ.
                3) Я обработаю его и верну результат.

                Подробности - команда /help.
                """.strip();
        safeSendText(chatId, msg);
    }

    private void handleDocumentMessage(Long chatId, Document document) {
        String fileName = document.getFileName();
        String mimeType = document.getMimeType();

        log.info("Received document from chat {}: name='{}', mime='{}', size={}",
                chatId, fileName, mimeType, document.getFileSize());

        if (fileName == null) {
            fileName = "unknown.json";
        }

        if (!fileName.toLowerCase().endsWith(".json")) {
            safeSendText(chatId, "Я принимаю только JSON-файлы экспорта чата (расширение .json). " +
                    "Проверьте, что вы отправили именно экспорт истории чата Telegram Desktop.");
            return;
        }

        // Проверяем лимит файлов
        List<PendingFile> pendingFiles = pendingFilesByChat.getOrDefault(chatId, new ArrayList<>());
        if (pendingFiles.size() >= BotConfig.MAX_FILES_PER_USER) {
            safeSendText(chatId, String.format(
                    "Достигнут лимит файлов (%d). Используйте /process для обработки накопленных файлов или /clear для очистки.",
                    BotConfig.MAX_FILES_PER_USER));
            return;
        }

        String fileId = document.getFileId();

        try (InputStream inputStream = downloadFileAsStream(fileId)) {
            // Считываем файл в память (небольшие файлы, поэтому допустимо)
            byte[] fileContent = inputStream.readAllBytes();

            // Добавляем в накопленные файлы
            pendingFilesByChat.computeIfAbsent(chatId, k -> new ArrayList<>())
                    .add(new PendingFile(fileName, fileContent));

            int totalFiles = pendingFilesByChat.get(chatId).size();
            safeSendText(chatId, String.format(
                    "Файл \"%s\" добавлен. Всего накоплено: %d/%d файлов.\n" +
                    "Отправьте ещё файлы или используйте /process для обработки.",
                    fileName, totalFiles, BotConfig.MAX_FILES_PER_USER));

        } catch (TelegramApiException e) {
            log.error("Failed to download file from Telegram for chat {} fileId {}", chatId, fileId, e);
            safeSendText(chatId, "Не удалось скачать файл \"" + fileName + "\".");
        } catch (IOException e) {
            log.error("IO error while downloading/parsing file for chat {} fileId {}", chatId, fileId, e);
            safeSendText(chatId, "Произошла ошибка при чтении файла \"" + fileName + "\".");
        }
    }

    /**
     * Обрабатывает все накопленные файлы пользователя.
     */
    private void processPendingFiles(Long chatId) {
        List<PendingFile> pendingFiles = pendingFilesByChat.get(chatId);

        if (pendingFiles == null || pendingFiles.isEmpty()) {
            safeSendText(chatId, "Нет накопленных файлов для обработки. Отправьте JSON-файлы.");
            return;
        }

        try {
            safeSendText(chatId, "Обрабатываю " + pendingFiles.size() + " файл(ов)...");

            // Преобразуем накопленные файлы в потоки и имена
            List<InputStream> fileStreams = new ArrayList<>();
            List<String> fileNames = new ArrayList<>();

            for (PendingFile file : pendingFiles) {
                fileStreams.add(new ByteArrayInputStream(file.content()));
                fileNames.add(file.fileName());
            }

            // Обрабатываем через сервис
            ReportGenerationService.ReportResult result = reportGenerationService.processFiles(fileStreams, fileNames);

            // Закрываем потоки
            fileStreams.forEach(stream -> {
                try { stream.close(); } catch (IOException ignored) {}
            });

            // Отправляем результат
            if (result.isExcelFormat()) {
                sendExcelResult(chatId, result.getExcelData());
            } else {
                sendTextResult(chatId, result.getTextLines());
            }

            // Очищаем накопленные файлы (приватность)
            pendingFilesByChat.remove(chatId);

        } catch (ReportGenerationService.ReportGenerationException e) {
            log.error("Failed to generate report for chat {}", chatId, e);
            safeSendText(chatId, "Ошибка при обработке файлов: " + e.getMessage());
            // Очищаем файлы даже при ошибке
            pendingFilesByChat.remove(chatId);
        } catch (Exception e) {
            log.error("Unexpected error processing files for chat {}", chatId, e);
            safeSendText(chatId, "Произошла непредвиденная ошибка при обработке файлов.");
            pendingFilesByChat.remove(chatId);
        }
    }

    /**
     * Очищает накопленные файлы пользователя.
     */
    private void clearPendingFiles(Long chatId) {
        int removed = pendingFilesByChat.remove(chatId) != null 
                ? pendingFilesByChat.getOrDefault(chatId, List.of()).size() 
                : 0;

        if (removed > 0) {
            safeSendText(chatId, "Очищено " + removed + " файл(ов).");
        } else {
            safeSendText(chatId, "Нет накопленных файлов для очистки.");
        }
    }

    /**
     * Отправляет текстовый результат в чат.
     */
    private void sendTextResult(Long chatId, List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            safeSendText(chatId, "Результат обработки пуст.");
            return;
        }

        // Telegram ограничивает длину сообщения 4096 символами, разбиваем на части при необходимости
        StringBuilder currentMessage = new StringBuilder();

        for (String line : lines) {
            if (currentMessage.length() + line.length() + 1 > 4000) {
                // Отправляем текущее сообщение и начинаем новое
                safeSendText(chatId, currentMessage.toString());
                currentMessage = new StringBuilder(line).append("\n");
            } else {
                currentMessage.append(line).append("\n");
            }
        }

        // Отправляем последнее сообщение
        if (currentMessage.length() > 0) {
            safeSendText(chatId, currentMessage.toString());
        }
    }

    /**
     * Отправляет Excel-файл в чат.
     */
    private void sendExcelResult(Long chatId, ReportGenerationService.ExcelData excelData) {
        if (excelData == null) {
            safeSendText(chatId, "Данные для Excel отсутствуют.");
            return;
        }

        try {
            byte[] excelBytes = excelExportService.generateExcel(excelData);

            if (excelBytes == null || excelBytes.length == 0) {
                safeSendText(chatId, "Ошибка: Excel-файл пуст.");
                return;
            }

            InputFile inputFile = new InputFile(new ByteArrayInputStream(excelBytes), "chatlas_report.xlsx");
            SendDocument sendDocument = SendDocument.builder()
                    .chatId(String.valueOf(chatId))
                    .document(inputFile)
                    .caption("Отчет по участникам чата")
                    .build();

            telegramClient.execute(sendDocument);
            log.info("Excel file sent to chat {}", chatId);

        } catch (ExcelExportService.ExcelExportException e) {
            log.error("Failed to generate Excel for chat {}", chatId, e);
            safeSendText(chatId, "Ошибка при создании Excel-файла: " + e.getMessage());
        } catch (TelegramApiException e) {
            log.error("Failed to send Excel file to chat {}", chatId, e);
            safeSendText(chatId, "Не удалось отправить Excel-файл.");
        }
    }

    private InputStream downloadFileAsStream(String fileId) throws TelegramApiException, IOException {
        GetFile getFileMethod = new GetFile(fileId);
        org.telegram.telegrambots.meta.api.objects.File file = telegramClient.execute(getFileMethod);

        String filePath = file.getFilePath();
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalStateException("Received empty filePath for fileId " + fileId);
        }

        String urlString = "https://api.telegram.org/file/bot" + botToken + "/" + filePath;
        log.debug("Downloading file from Telegram: {}", urlString);

        URL url = URI.create(urlString).toURL();
        URLConnection connection = url.openConnection();
        return connection.getInputStream();
    }

    private void safeSendText(Long chatId, String text) {
        if (chatId == null) {
            log.warn("Attempted to send message with null chatId, skipping");
            return;
        }

        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chat {}", chatId, e);
        }
    }
}
