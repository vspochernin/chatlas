package ru.hackathon.chatlas;

import org.junit.jupiter.api.Test;
import ru.hackathon.chatlas.analysis.ChatAnalyzer;
import ru.hackathon.chatlas.analysis.ChatAnalyzerImpl;
import ru.hackathon.chatlas.domain.ChatAnalysisResult;
import ru.hackathon.chatlas.domain.ChatExport;
import ru.hackathon.chatlas.domain.Mention;
import ru.hackathon.chatlas.domain.Participant;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ChatAnalyzerImplTest {

    private final ChatAnalyzer analyzer = new ChatAnalyzerImpl();

    @Test
    void shouldExtractParticipantsAndMentions() throws Exception {
        ChatExport chatExport = createTestChatExport();

        ChatAnalysisResult result = analyzer.analyze(chatExport);

        assertEquals(2, result.getParticipantsCount());
        assertEquals(2, result.getMentionsCount());
        assertEquals(4, result.getTotalCount());
    }

    @Test
    void shouldExtractCorrectParticipants() throws Exception {
        ChatExport chatExport = createTestChatExport();

        ChatAnalysisResult result = analyzer.analyze(chatExport);
        Set<Participant> participants = result.participants();

        assertEquals(2, participants.size());
        assertTrue(participants.contains(new Participant("user123456789", "Владислав Почернин")));
        assertTrue(participants.contains(new Participant("user123123123", "Егор Мартынов")));
    }

    @Test
    void shouldExtractCorrectMentions() throws Exception {
        ChatExport chatExport = createTestChatExport();

        ChatAnalysisResult result = analyzer.analyze(chatExport);
        Set<Mention> mentions = result.mentions();

        assertEquals(2, mentions.size());
        assertTrue(mentions.contains(new Mention("@vspochernin")));
        assertTrue(mentions.contains(new Mention("@vspocherninwork")));
    }

    @Test
    void shouldIgnoreDeletedAccounts() throws Exception {
        ChatExport.Message deletedAccountMessage = new ChatExport.Message();
        deletedAccountMessage.setFrom("Deleted Account");
        deletedAccountMessage.setFromId("user999");

        ChatExport.Message normalMessage = new ChatExport.Message();
        normalMessage.setFrom("Нормальный Пользователь");
        normalMessage.setFromId("user111");

        ChatExport chatExport = new ChatExport();
        chatExport.setMessages(List.of(deletedAccountMessage, normalMessage));

        ChatAnalysisResult result = analyzer.analyze(chatExport);

        assertEquals(1, result.getParticipantsCount());
        assertTrue(result.participants().contains(new Participant("user111", "Нормальный Пользователь")));
    }

    @Test
    void shouldIgnoreDeletedAccountsRussian() throws Exception {
        ChatExport.Message deletedAccountMessage = new ChatExport.Message();
        deletedAccountMessage.setFrom("Удалённый аккаунт");
        deletedAccountMessage.setFromId("user999");

        ChatExport chatExport = new ChatExport();
        chatExport.setMessages(List.of(deletedAccountMessage));

        ChatAnalysisResult result = analyzer.analyze(chatExport);

        assertEquals(0, result.getParticipantsCount());
    }

    @Test
    void shouldIgnoreMessagesWithoutFromOrFromId() throws Exception {
        ChatExport.Message messageWithoutFrom = new ChatExport.Message();
        messageWithoutFrom.setFromId("user111");

        ChatExport.Message messageWithoutFromId = new ChatExport.Message();
        messageWithoutFromId.setFrom("Пользователь");

        ChatExport.Message normalMessage = new ChatExport.Message();
        normalMessage.setFrom("Нормальный Пользователь");
        normalMessage.setFromId("user222");

        ChatExport chatExport = new ChatExport();
        chatExport.setMessages(List.of(messageWithoutFrom, messageWithoutFromId, normalMessage));

        ChatAnalysisResult result = analyzer.analyze(chatExport);

        assertEquals(1, result.getParticipantsCount());
        assertTrue(result.participants().contains(new Participant("user222", "Нормальный Пользователь")));
    }

    @Test
    void shouldHandleParticipantWithBlankDisplayName() throws Exception {
        ChatExport.Message messageWithBlankName = new ChatExport.Message();
        messageWithBlankName.setFrom("");
        messageWithBlankName.setFromId("user999");

        ChatExport.Message messageWithWhitespaceName = new ChatExport.Message();
        messageWithWhitespaceName.setFrom("   ");
        messageWithWhitespaceName.setFromId("user888");

        ChatExport.Message normalMessage = new ChatExport.Message();
        normalMessage.setFrom("Нормальный Пользователь");
        normalMessage.setFromId("user222");

        ChatExport chatExport = new ChatExport();
        chatExport.setMessages(List.of(messageWithBlankName, messageWithWhitespaceName, normalMessage));

        ChatAnalysisResult result = analyzer.analyze(chatExport);

        // Должны быть все три участника (blank имя - это валидный случай).
        assertEquals(3, result.getParticipantsCount());
        assertTrue(result.participants().contains(new Participant("user999", "")));
        assertTrue(result.participants().contains(new Participant("user888", "   ")));
        assertTrue(result.participants().contains(new Participant("user222", "Нормальный Пользователь")));
    }

    @Test
    void shouldHandleEmptyMessagesList() throws Exception {
        ChatExport chatExport = new ChatExport();
        chatExport.setMessages(List.of());

        ChatAnalysisResult result = analyzer.analyze(chatExport);

        assertEquals(0, result.getParticipantsCount());
        assertEquals(0, result.getMentionsCount());
        assertEquals(0, result.getTotalCount());
    }

    @Test
    void shouldHandleNullMessagesList() throws Exception {
        ChatExport chatExport = new ChatExport();
        chatExport.setMessages(null);

        ChatAnalysisResult result = analyzer.analyze(chatExport);

        assertEquals(0, result.getParticipantsCount());
        assertEquals(0, result.getMentionsCount());
    }

    @Test
    void shouldHandleNullMessage() throws Exception {
        ChatExport.Message normalMessage = new ChatExport.Message();
        normalMessage.setFrom("Пользователь");
        normalMessage.setFromId("user111");

        ChatExport chatExport = new ChatExport();
        List<ChatExport.Message> messages = new java.util.ArrayList<>();
        messages.add(null);
        messages.add(normalMessage);
        chatExport.setMessages(messages);

        ChatAnalysisResult result = analyzer.analyze(chatExport);

        assertEquals(1, result.getParticipantsCount());
    }

    @Test
    void shouldHandleMessagesWithoutTextEntities() throws Exception {
        ChatExport.Message message = new ChatExport.Message();
        message.setFrom("Пользователь");
        message.setFromId("user111");
        message.setTextEntities(null);

        ChatExport chatExport = new ChatExport();
        chatExport.setMessages(List.of(message));

        ChatAnalysisResult result = analyzer.analyze(chatExport);

        assertEquals(1, result.getParticipantsCount());
        assertEquals(0, result.getMentionsCount());
    }

    @Test
    void shouldNotCountDuplicateParticipants() throws Exception {
        ChatExport.Message message1 = new ChatExport.Message();
        message1.setFrom("Пользователь");
        message1.setFromId("user111");

        ChatExport.Message message2 = new ChatExport.Message();
        message2.setFrom("Пользователь");
        message2.setFromId("user111");

        ChatExport chatExport = new ChatExport();
        chatExport.setMessages(List.of(message1, message2));

        ChatAnalysisResult result = analyzer.analyze(chatExport);

        assertEquals(1, result.getParticipantsCount());
    }

    @Test
    void shouldNotCountDuplicateMentions() throws Exception {
        ChatExport.TextEntity mention1 = new ChatExport.TextEntity();
        mention1.setType("mention");
        mention1.setText("@username");

        ChatExport.TextEntity mention2 = new ChatExport.TextEntity();
        mention2.setType("mention");
        mention2.setText("@username");

        ChatExport.Message message = new ChatExport.Message();
        message.setFrom("Пользователь");
        message.setFromId("user111");
        message.setTextEntities(List.of(mention1, mention2));

        ChatExport chatExport = new ChatExport();
        chatExport.setMessages(List.of(message));

        ChatAnalysisResult result = analyzer.analyze(chatExport);

        assertEquals(1, result.getMentionsCount());
    }

    @Test
    void shouldOnlyExtractMentionType() throws Exception {
        ChatExport.TextEntity plain = new ChatExport.TextEntity();
        plain.setType("plain");
        plain.setText("@notamention");

        ChatExport.TextEntity mention = new ChatExport.TextEntity();
        mention.setType("mention");
        mention.setText("@username");

        ChatExport.Message message = new ChatExport.Message();
        message.setFrom("Пользователь");
        message.setFromId("user111");
        message.setTextEntities(List.of(plain, mention));

        ChatExport chatExport = new ChatExport();
        chatExport.setMessages(List.of(message));

        ChatAnalysisResult result = analyzer.analyze(chatExport);

        assertEquals(1, result.getMentionsCount());
        assertTrue(result.mentions().contains(new Mention("@username")));
    }

    @Test
    void shouldThrowExceptionOnNullChatExport() {
        assertThrows(ChatAnalyzer.ChatAnalysisException.class, () -> analyzer.analyze(null));
    }

    private ChatExport createTestChatExport() {
        ChatExport chatExport = new ChatExport();
        chatExport.setName("Тест");
        chatExport.setType("personal_chat");

        ChatExport.Message message1 = new ChatExport.Message();
        message1.setFrom("Владислав Почернин");
        message1.setFromId("user123456789");
        ChatExport.TextEntity entity1 = new ChatExport.TextEntity();
        entity1.setType("plain");
        entity1.setText("first message");
        message1.setTextEntities(List.of(entity1));

        ChatExport.Message message2 = new ChatExport.Message();
        message2.setFrom("Егор Мартынов");
        message2.setFromId("user123123123");
        ChatExport.TextEntity entity2 = new ChatExport.TextEntity();
        entity2.setType("plain");
        entity2.setText("second message");
        message2.setTextEntities(List.of(entity2));

        ChatExport.Message message3 = new ChatExport.Message();
        message3.setFrom("Владислав Почернин");
        message3.setFromId("user123456789");
        ChatExport.TextEntity entity3a = new ChatExport.TextEntity();
        entity3a.setType("plain");
        entity3a.setText("message with ");
        ChatExport.TextEntity entity3b = new ChatExport.TextEntity();
        entity3b.setType("mention");
        entity3b.setText("@vspochernin");
        message3.setTextEntities(List.of(entity3a, entity3b));

        ChatExport.Message message4 = new ChatExport.Message();
        message4.setFrom("Владислав Почернин");
        message4.setFromId("user123456789");
        ChatExport.TextEntity entity4a = new ChatExport.TextEntity();
        entity4a.setType("mention");
        entity4a.setText("@vspochernin");
        ChatExport.TextEntity entity4b = new ChatExport.TextEntity();
        entity4b.setType("mention");
        entity4b.setText("@vspocherninwork");
        message4.setTextEntities(List.of(entity4a, entity4b));

        chatExport.setMessages(List.of(message1, message2, message3, message4));
        return chatExport;
    }
}
