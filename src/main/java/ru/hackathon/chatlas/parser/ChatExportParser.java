package ru.hackathon.chatlas.parser;

import ru.hackathon.chatlas.domain.ChatExport;
import ru.hackathon.chatlas.domain.RawChatFile;

/**
 * Интерфейс для парсинга JSON-экспорта чата Telegram в доменную модель.
 *
 */
public interface ChatExportParser {

    /**
     * Распарсить JSON-файл экспорта чата в доменную модель.
     *
     * @param file сырой файл экспорта чата.
     * @return объект доменной модели ChatExport.
     * @throws ChatExportParseException если файл не получается распарсить.
     */
    ChatExport parse(RawChatFile file) throws ChatExportParseException;

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

