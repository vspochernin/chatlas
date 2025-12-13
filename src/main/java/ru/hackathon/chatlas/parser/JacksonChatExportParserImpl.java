package ru.hackathon.chatlas.parser;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import ru.hackathon.chatlas.domain.ChatExport;
import ru.hackathon.chatlas.domain.RawChatFile;

@Slf4j
public class JacksonChatExportParserImpl implements ChatExportParser {

    private final ObjectMapper objectMapper;

    public JacksonChatExportParserImpl() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public ChatExport parse(RawChatFile file) throws ChatExportParseException {
        try {
            log.info("Parsing file: {}", file.fileName());

            if (file.jsonContent().isBlank()) {
                throw new ChatExportParseException("JSON content is blank");
            }

            return objectMapper.readValue(file.jsonContent(), ChatExport.class);

        } catch (Exception e) {
            log.error("Parse error: {}", e.getMessage());
            throw new ChatExportParseException("Failed to parse JSON: " + e.getMessage(), e);
        }
    }
}
