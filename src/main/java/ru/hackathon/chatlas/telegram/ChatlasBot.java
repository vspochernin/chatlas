package ru.hackathon.chatlas.telegram;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ChatlasBot implements LongPollingSingleThreadUpdateConsumer {

    private static final String COMMAND_START = "/start";
    private static final String COMMAND_HELP = "/help";

    private final TelegramClient telegramClient;
    private final String botToken;

    public ChatlasBot(String botToken) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.botToken = botToken;
        log.info("ChatlasBot instance created");
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
        String msg = """
                Что я умею:
                
                - Принимаю JSON-экспорт истории чата (Telegram Desktop → Export chat history → JSON).
                - Для каждого файла разбираю участников и упоминания.
                - Если участников < 50 - отправляю список прямо в чат.
                - Если участников ≥ 51 - формирую и отправляю Excel-файл.
                
                Просто отправьте мне .json-файл(ы).
                """.strip();
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

        String fileId = document.getFileId();

        try (InputStream inputStream = downloadFileAsStream(fileId)) {

            // TODO: Dev2 - распарсить JSON в модель.
            // TODO: Dev3 - реализовать бизнес-логику определения участников и упоминаний (уже в другую модель).
            // TODO: Dev4 - вывести информацию в чат или в Excel.
            processChatHistoryFile(chatId, fileName, inputStream); // Stub.

            // Stub.
            safeSendText(chatId, "Успешно получен и обработан файл \"" + fileName + "\".");
        } catch (TelegramApiException e) {
            log.error("Failed to download file from Telegram for chat {} fileId {}", chatId, fileId, e);
            safeSendText(chatId, "Не удалось скачать файл \"" + fileName + "\".");
        } catch (IOException e) {
            log.error("IO error while downloading/parsing file for chat {} fileId {}", chatId, fileId, e);
            safeSendText(chatId, "Произошла ошибка при чтении файла \"" + fileName + "\".");
        }
    }

    // Stub.
    private void processChatHistoryFile(Long chatId, String fileName, InputStream inputStream) {
        log.info("processChatHistoryFile(chatId={}, fileName={}) invoked (stub implementation)", chatId, fileName);
        try {
            log.info("fileText='{}'", IOUtils.toString(inputStream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("Exception while reading file from Telegram for chat {} fileId {}", chatId, fileName, e);
        }
    }

    private InputStream downloadFileAsStream(String fileId) throws TelegramApiException, IOException {
        GetFile getFileMethod = new GetFile(fileId);
        org.telegram.telegrambots.meta.api.objects.File file = telegramClient.execute(getFileMethod);

        String filePath = file.getFilePath();
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalStateException("Received empty filePath for fileId " + fileId);
        }

        String url = "https://api.telegram.org/file/bot" + botToken + "/" + filePath;
        log.debug("Downloading file from Telegram: {}", url);

        URLConnection connection = new URL(url).openConnection();
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
