package ru.hackathon.chatlas.parser;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

/**
 * Заглушка парсера для демонстрации работы архитектуры.
 *
 * @implNote Dev2: Заменить на реальную реализацию JacksonChatExportParser
 */
@Slf4j
public class StubChatExportParser implements ChatExportParser {

    @Override
    public Object parse(InputStream inputStream, String fileName) throws ChatExportParseException {
        log.info("StubChatExportParser.parse(fileName={}) - stub implementation", fileName);

        // В реальной реализации здесь будет парсинг JSON через Jackson.
        // Dev2 должен вернуть объект типа ChatExport из пакета domain.

        // Сейчас просто проверяем, что поток не пустой.
        try {
            int bytesRead = inputStream.available();
            log.info("Stub parser: InputStream has {} bytes available", bytesRead);
            return new StubParseResult(fileName, bytesRead);
        } catch (Exception e) {
            throw new ChatExportParseException("Stub parser failed to read stream", e);
        }
    }

    /**
     * Заглушка результата парсинга.
     */
    private record StubParseResult(String fileName, int bytesCount) {
    }
}

