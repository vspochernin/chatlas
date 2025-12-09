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
        log.info("Parsing file: {}", file.fileName());

        if (file.jsonContent().isBlank()) {
            throw new ChatExportParseException("JSON content is blank");
        }

        log.info("JSON content length: {} chars", file.jsonContent().length());
        
        // TODO: Dev2: Реализовать парсинг JSON через Jackson, вернуть ChatExport
        return new StubChatExportImpl();
    }

    // TODO: Dev2: Имплементации не будет, т.к. это заглушка. Будет просто модель.
    private static class StubChatExportImpl implements ChatExport {
    }
}

