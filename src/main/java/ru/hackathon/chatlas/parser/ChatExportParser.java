package ru.hackathon.chatlas.parser;

import java.io.InputStream;

/**
 * Интерфейс для парсинга JSON-экспорта чата Telegram в доменную модель.
 * <p>
 *
 * @implNote Dev2: Реализовать JacksonChatExportParser с использованием Jackson
 */
public interface ChatExportParser {

    /**
     * Парсит JSON-файл экспорта чата из InputStream.
     *
     * @param inputStream поток с JSON-данными (UTF-8).
     * @param fileName    имя файла (для логирования и сообщений об ошибках).
     * @return объект доменной модели ChatExport (будет создан Dev2).
     * @throws ChatExportParseException если файл не получается распарсить.
     */
    // TODO: заменить на record.
    Object parse(InputStream inputStream, String fileName) throws ChatExportParseException;

    /**
     * Исключение при парсинге JSON-экспорта.
     */
    class ChatExportParseException extends Exception {
        public ChatExportParseException(String message) {
            super(message);
        }

        public ChatExportParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

