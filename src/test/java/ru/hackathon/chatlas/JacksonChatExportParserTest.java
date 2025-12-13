package ru.hackathon.chatlas;

import org.junit.jupiter.api.Test;
import ru.hackathon.chatlas.domain.ChatExport;
import ru.hackathon.chatlas.domain.RawChatFile;
import ru.hackathon.chatlas.parser.ChatExportParser.ChatExportParseException;
import ru.hackathon.chatlas.parser.JacksonChatExportParserImpl;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JacksonChatExportParserTest {

    @Test
    void shouldParseRealChatJson() throws Exception {
        String jsonContent = readResourceAsString("chat1.json");
        RawChatFile file = new RawChatFile("chat1.json", jsonContent);
        JacksonChatExportParserImpl parser = new JacksonChatExportParserImpl();

        ChatExport result = parser.parse(file);

        assertNotNull(result);
        assertEquals("Егор", result.getName());
        assertEquals("personal_chat", result.getType());
        assertEquals(123123123L, result.getId());

        List<ChatExport.Message> messages = result.getMessages();
        assertNotNull(messages);
        assertEquals(5, messages.size());
    }

    @Test
    void shouldParseMessagesCorrectly() throws Exception {
        String jsonContent = readResourceAsString("chat1.json");
        RawChatFile file = new RawChatFile("chat1.json", jsonContent);
        JacksonChatExportParserImpl parser = new JacksonChatExportParserImpl();

        ChatExport result = parser.parse(file);
        List<ChatExport.Message> messages = result.getMessages();

        ChatExport.Message firstMessage = messages.get(0);
        assertEquals("Владислав Почернин", firstMessage.getFrom());
        assertEquals("user123456789", firstMessage.getFromId());
        assertNotNull(firstMessage.getTextEntities());
        assertFalse(firstMessage.getTextEntities().isEmpty());
        assertEquals("first message", firstMessage.getText());
    }

    @Test
    void shouldParseMentionsCorrectly() throws Exception {
        String jsonContent = readResourceAsString("chat1.json");
        RawChatFile file = new RawChatFile("chat1.json", jsonContent);
        JacksonChatExportParserImpl parser = new JacksonChatExportParserImpl();

        ChatExport result = parser.parse(file);
        List<ChatExport.Message> messages = result.getMessages();

        // Сообщение с упоминанием (4-е по счету, индекс 3).
        ChatExport.Message messageWithMention = messages.get(3);
        List<ChatExport.TextEntity> entities = messageWithMention.getTextEntities();

        assertTrue(entities.stream()
                .anyMatch(e -> "mention".equals(e.getType()) && "@vspochernin".equals(e.getText())));

        // Проверяем, что есть и plain текст.
        assertTrue(entities.stream().anyMatch(e -> "plain".equals(e.getType())));
    }

    @Test
    void shouldThrowExceptionOnBlankContent() {
        RawChatFile file = new RawChatFile("empty.json", "");
        JacksonChatExportParserImpl parser = new JacksonChatExportParserImpl();

        assertThrows(ChatExportParseException.class, () -> parser.parse(file));
    }

    @Test
    void shouldThrowExceptionOnInvalidJson() {
        RawChatFile file = new RawChatFile("invalid.json", "{ invalid json }");
        JacksonChatExportParserImpl parser = new JacksonChatExportParserImpl();

        assertThrows(ChatExportParseException.class, () -> parser.parse(file));
    }

    @Test
    void shouldParseMultipleMentionsInOneMessage() throws Exception {
        String jsonContent = readResourceAsString("chat1.json");
        RawChatFile file = new RawChatFile("chat1.json", jsonContent);
        JacksonChatExportParserImpl parser = new JacksonChatExportParserImpl();

        ChatExport result = parser.parse(file);
        List<ChatExport.Message> messages = result.getMessages();

        // Последнее сообщение с двумя упоминаниями (индекс 4).
        ChatExport.Message messageWithMultipleMentions = messages.get(4);
        List<ChatExport.TextEntity> entities = messageWithMultipleMentions.getTextEntities();

        long mentionCount = entities.stream()
                .filter(e -> "mention".equals(e.getType()))
                .count();

        assertEquals(2, mentionCount, "Should have 2 mentions");
        assertTrue(entities.stream()
                .anyMatch(e -> "mention".equals(e.getType()) && "@vspochernin".equals(e.getText())));
        assertTrue(entities.stream()
                .anyMatch(e -> "mention".equals(e.getType()) && "@vspocherninwork".equals(e.getText())));
    }

    @Test
    void shouldHandleMessageWithEmptyTextEntities() throws Exception {
        String jsonWithEmptyEntities = """
                {
                  "name": "Test",
                  "type": "personal_chat",
                  "id": 123,
                  "messages": [
                    {
                      "from": "User",
                      "from_id": "user123",
                      "text_entities": []
                    }
                  ]
                }
                """;

        RawChatFile file = new RawChatFile("test.json", jsonWithEmptyEntities);
        JacksonChatExportParserImpl parser = new JacksonChatExportParserImpl();

        ChatExport result = parser.parse(file);
        assertNotNull(result);
        assertEquals(1, result.getMessages().size());

        ChatExport.Message message = result.getMessages().get(0);
        assertNotNull(message.getTextEntities());
        assertTrue(message.getTextEntities().isEmpty());
        assertEquals("", message.getText());
    }

    private String readResourceAsString(String resourceName) throws Exception {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(resourceName);
        assertNotNull(stream, "Resource not found: " + resourceName);
        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }
}
