package ru.hackathon.chatlas;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import ru.hackathon.chatlas.domain.ChatExport;
import ru.hackathon.chatlas.domain.RawChatFile;
import ru.hackathon.chatlas.parser.JacksonChatExportParserImpl;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class JacksonChatExportParserTest {

    @Test
    void shouldParseRealChatJson() throws Exception {
        Path filePath = Paths.get("examples", "chat1.json");
        String jsonContent = Files.readString(filePath, StandardCharsets.UTF_8);
        RawChatFile file = new RawChatFile("chat1.json", jsonContent);
        JacksonChatExportParserImpl parser = new JacksonChatExportParserImpl();
        ChatExport result = parser.parse(file);
        assertNotNull(result);
        assertNotNull(result.getMessages());
        assertFalse(result.getMessages().isEmpty());
        boolean hasValidMessage = false;
        for (ChatExport.Message message : result.getMessages()) {
                hasValidMessage = true;
                System.out.println(message);
//                break;
        }
        assertTrue(hasValidMessage, "No valid messages found");
    }
}
// кодировка
//[Console]::OutputEncoding = [System.Text.Encoding]::UTF8