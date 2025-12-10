package ru.hackathon.chatlas.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.hackathon.chatlas.domain.ChatExportModel;
import ru.hackathon.chatlas.domain.ChatExport;
import ru.hackathon.chatlas.domain.RawChatFile;

@Slf4j
@RequiredArgsConstructor
public class JacksonChatExportParser implements ChatExportParser {

    private final ObjectMapper objectMapper;

    public JacksonChatExportParser() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    @Override
    public ChatExport parse(RawChatFile file) throws ChatExportParseException {
        try {
            log.info("Parsing file: {}", file.fileName());

            if (file.jsonContent().isBlank()) {
                throw new ChatExportParseException("JSON content is blank");
            }

            return objectMapper.readValue(file.jsonContent(), ChatExportModel.class);

        } catch (Exception e) {
            throw new ChatExportParseException("Failed to parse JSON", e);
        }
    }
}