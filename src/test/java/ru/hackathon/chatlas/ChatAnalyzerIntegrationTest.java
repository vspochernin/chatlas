package ru.hackathon.chatlas;

import org.junit.jupiter.api.Test;
import ru.hackathon.chatlas.analysis.ChatAnalyzerImpl;
import ru.hackathon.chatlas.domain.ChatAnalysisResult;
import ru.hackathon.chatlas.domain.ChatExport;
import ru.hackathon.chatlas.domain.Mention;
import ru.hackathon.chatlas.domain.Participant;
import ru.hackathon.chatlas.parser.JacksonChatExportParserImpl;
import ru.hackathon.chatlas.parser.ChatExportParser.ChatExportParseException;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ChatAnalyzerIntegrationTest {

    @Test
    void shouldAnalyzeRealChatExport() throws Exception {
        String jsonContent = readResourceAsString("chat1.json");
        var parser = new JacksonChatExportParserImpl();
        var analyzer = new ChatAnalyzerImpl();

        ChatExport chatExport = parser.parse(new ru.hackathon.chatlas.domain.RawChatFile("chat1.json", jsonContent));
        ChatAnalysisResult result = analyzer.analyze(chatExport);

        // Проверяем участников: должны быть 2 уникальных (Владислав Почернин и Егор Мартынов).
        assertEquals(2, result.getParticipantsCount());
        assertTrue(result.participants().contains(new Participant("user123456789", "Владислав Почернин")));
        assertTrue(result.participants().contains(new Participant("user123123123", "Егор Мартынов")));

        // Проверяем упоминания: должны быть 2 уникальных (@vspochernin и @vspocherninwork).
        assertEquals(2, result.getMentionsCount());
        assertTrue(result.mentions().contains(new Mention("@vspochernin")));
        assertTrue(result.mentions().contains(new Mention("@vspocherninwork")));

        // Общее количество.
        assertEquals(4, result.getTotalCount());
    }

    private String readResourceAsString(String resourceName) throws Exception {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(resourceName);
        assertNotNull(stream, "Resource not found: " + resourceName);
        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }
}
