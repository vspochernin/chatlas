package ru.hackathon.chatlas.parser;

import lombok.extern.slf4j.Slf4j;
import ru.hackathon.chatlas.domain.RawChatFile;

import java.io.IOException;

/**
 * Заглушка парсера для демонстрации работы архитектуры.
 *
 * @implNote Dev2: Заменить на реальную реализацию JacksonChatExportParser
 */
@Slf4j
public class StubChatExportParser implements ChatExportParser {

    @Override
    public Object parse(RawChatFile file) throws ChatExportParseException {
        log.info("StubChatExportParser.parse(fileName={}) - stub implementation", file.fileName());

        // В реальной реализации здесь будет парсинг JSON через Jackson.
        // Dev2 должен вернуть объект типа ChatExport из пакета domain.

        // Сейчас просто проверяем, что поток не пустой.
        try {
            int bytesAvailable = file.content().available();
            log.info("Stub parser: InputStream has {} bytes available", bytesAvailable);
            return new StubParseResult(file.fileName(), bytesAvailable);
        } catch (IOException e) {
            throw new ChatExportParseException("Stub parser failed to read stream", e);
        }
    }

    /**
     * Заглушка результата парсинга.
     */
    private record StubParseResult(String fileName, int bytesCount) {
    }
}

