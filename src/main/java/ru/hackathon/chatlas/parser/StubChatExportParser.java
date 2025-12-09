package ru.hackathon.chatlas.parser;

import lombok.extern.slf4j.Slf4j;
import ru.hackathon.chatlas.domain.ChatExport;
import ru.hackathon.chatlas.domain.RawChatFile;

/**
 * Заглушка парсера для демонстрации работы архитектуры.
 *
 * @implNote TODO: Dev2: Заменить на реальную реализацию JacksonChatExportParser
 */
@Slf4j
public class StubChatExportParser implements ChatExportParser {

    @Override
    public ChatExport parse(RawChatFile file) throws ChatExportParseException {
        log.info("StubChatExportParser.parse(fileName={}) - stub implementation", file.fileName());

        // TODO: Dev2: В реальной реализации здесь будет парсинг JSON через Jackson.

        // Заглушка: просто проверяем, что строка не пустая.
        if (file.jsonContent().isBlank()) {
            throw new ChatExportParseException("JSON content is blank");
        }

        log.info("Stub parser: JSON content length = {} characters", file.jsonContent().length());
        return null;
    }
}

