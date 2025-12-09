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
import ru.hackathon.chatlas.domain.RawChatFile;
import ru.hackathon.chatlas.export.ReportRenderer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

@Slf4j
public class ChatlasBot implements LongPollingSingleThreadUpdateConsumer {

    private static final String COMMAND_START = "/start";
    private static final String COMMAND_HELP = "/help";

    private final TelegramClient telegramClient;
    private final String botToken;
    private final ChatProcessingService processingService;

    public ChatlasBot(String botToken, ChatProcessingService processingService) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.botToken = botToken;
        this.processingService = processingService;
        log.info("ChatlasBot instance created");
    }

    @Override
    public void consume(Update update) {
        if (update == null) {
            log.warn("Received null update, ignore it");
            return;
        }

        if (!update.hasMessage()) {
            log.warn("Update without message, ignore it: {}", update);
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
                log.warn("Unsupported message type in chat {}: {}", chatId, message);
                safeSendText(chatId, "Я способен обработать только текст, файлы или команды.");
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
            default -> safeSendText(chatId, "Неизвестная команда. Используйте /start или /help.");
        }
    }

    private void sendStartMessage(Long chatId) {
        String msg = """
                Привет! Я бот Chatlas.
                
                Пришлите мне один или несколько JSON-файлов экспорта чата из Telegram Desktop.
                Я обработаю их и подготовлю список участников / Excel-файл согласно заданию хакатона.
                
                Если нужна справка - используйте команду /help.
                """.strip();
        safeSendText(chatId, msg);
    }

    private void sendHelpMessage(Long chatId) {
        String msg = """
                Что я умею:
                
                - Принимаю JSON-экспорт истории чата (Telegram Desktop -> Export chat history -> JSON).
                - Каждый файл обрабатывается сразу после отправки.
                - Извлекаю участников (авторов сообщений) и упоминания (@username).
                - Если всего сущностей < 50 - отправляю список прямо в чат.
                - Если всего сущностей >= 51 - формирую и отправляю Excel-файл.
                
                Просто отправьте мне .json-файл экспорта чата.
                """.strip();
        safeSendText(chatId, msg);
    }

    private void handlePlainTextMessage(Long chatId) {
        String msg = """
                Я жду JSON-файлы экспорта чата.
                
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

        log.info(
                "Received document from chat {}: name='{}', mime='{}', size={}",
                chatId,
                fileName,
                mimeType,
                document.getFileSize());

        if (fileName == null) {
            fileName = "unknown.json";
        }

        if (!fileName.toLowerCase().endsWith(".json")) {
            safeSendText(chatId, "Я принимаю только JSON-файлы экспорта чата (расширение .json). " +
                    "Проверьте, что вы отправили именно экспорт истории чата Telegram Desktop в формате JSON.");
            return;
        }

        String fileId = document.getFileId();

        try (InputStream inputStream = downloadFileAsStream(fileId)) {
            // Скачиваем файл в память (обрабатываем "на лету", не сохраняем на диск).
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            inputStream.transferTo(buffer);
            byte[] fileContent = buffer.toByteArray();

            // Создаем RawChatFile и обрабатываем сразу
            RawChatFile rawFile = new RawChatFile(fileName, new ByteArrayInputStream(fileContent));
            safeSendText(chatId, "Обрабатываю файл \"" + fileName + "\"...");

            // Обрабатываем через сервис
            ReportRenderer.ReportResult result = processingService.process(rawFile);

            // Отправляем результат
            if (result.getType() == ReportRenderer.OutputType.EXCEL) {
                sendExcelResult(chatId, result.getExcelBytes(), result.getExcelFileName());
            } else {
                sendTextResult(chatId, result.getText());
            }

            log.info("File {} processed successfully for chat {}", fileName, chatId);

        } catch (TelegramApiException e) {
            log.error("Failed to download file from Telegram for chat {}, fileId {}", chatId, fileId, e);
            safeSendText(chatId, "Не удалось скачать файл \"" + fileName + "\".");
        } catch (IOException e) {
            log.error("IO error while downloading file for chat {}, fileId {}", chatId, fileId, e);
            safeSendText(chatId, "Произошла ошибка при чтении файла \"" + fileName + "\".");
        } catch (ChatProcessingService.ChatProcessingException e) {
            log.error("Failed to process file {} for chat {}", fileName, chatId, e);
            safeSendText(chatId, "Ошибка при обработке файла \"" + fileName + "\".");
        } catch (Exception e) {
            log.error("Unexpected error while processing file for chat {}, fileId {}", chatId, fileId, e);
            safeSendText(chatId, "Произошла непредвиденная ошибка при обработке файла \"" + fileName + "\".");
        }
    }

    /**
     * Отправляет текстовый результат в чат.
     */
    private void sendTextResult(Long chatId, String text) {
        if (text == null || text.isBlank()) {
            safeSendText(chatId, "Результат обработки пуст.");
            return;
        }

        // Telegram ограничивает длину сообщения 4096 символами, разбиваем при необходимости
        if (text.length() <= 4096) {
            safeSendText(chatId, text);
        } else {
            // Разбиваем на части
            int offset = 0;
            while (offset < text.length()) {
                int endIndex = Math.min(offset + 4000, text.length());
                String chunk = text.substring(offset, endIndex);
                safeSendText(chatId, chunk);
                offset = endIndex;
            }
        }
    }

    /**
     * Отправляет Excel-файл в чат.
     */
    private void sendExcelResult(Long chatId, byte[] excelBytes, String fileName) {
        if (excelBytes == null || excelBytes.length == 0) {
            safeSendText(chatId, "Ошибка: Excel-файл пуст.");
            return;
        }

        try {
            String excelFileName = fileName != null && !fileName.isBlank()
                    ? fileName
                    : "chatlas_report.xlsx";

            InputFile inputFile = new InputFile(new ByteArrayInputStream(excelBytes), excelFileName);
            SendDocument sendDocument = SendDocument.builder()
                    .chatId(String.valueOf(chatId))
                    .document(inputFile)
                    .caption("Отчет по участникам чата")
                    .build();

            telegramClient.execute(sendDocument);
            log.info("Excel file sent to chat {}", chatId);

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
        log.info("Downloading file from Telegram: {}", urlString);

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
