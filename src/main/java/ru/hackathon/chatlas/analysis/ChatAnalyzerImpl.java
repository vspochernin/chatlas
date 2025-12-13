package ru.hackathon.chatlas.analysis;

import lombok.extern.slf4j.Slf4j;
import ru.hackathon.chatlas.domain.ChatAnalysisResult;
import ru.hackathon.chatlas.domain.ChatExport;
import ru.hackathon.chatlas.domain.Mention;
import ru.hackathon.chatlas.domain.Participant;

import java.util.HashSet;
import java.util.Set;

/**
 * Реализация анализатора чата: извлекает участников и упоминания из экспорта.
 */
@Slf4j
public class ChatAnalyzerImpl implements ChatAnalyzer {

    private static final String DELETED_ACCOUNT_NAME_EN = "Deleted Account";
    private static final String DELETED_ACCOUNT_NAME_RU = "Удалённый аккаунт";

    @Override
    public ChatAnalysisResult analyze(ChatExport chatExport) throws ChatAnalysisException {
        if (chatExport == null) {
            throw new ChatAnalysisException("ChatExport cannot be null");
        }

        if (chatExport.getMessages() == null) {
            log.warn("ChatExport has null messages list, returning empty result");
            return new ChatAnalysisResult(Set.of(), Set.of());
        }

        Set<Participant> participants = new HashSet<>();
        Set<Mention> mentions = new HashSet<>();

        for (ChatExport.Message message : chatExport.getMessages()) {
            if (message == null) {
                continue;
            }

            // Извлекаем участника (если это не удалённый аккаунт).
            extractParticipant(message, participants);

            // Извлекаем упоминания.
            extractMentions(message, mentions);
        }

        log.info("Analysis completed: {} participants, {} mentions", participants.size(), mentions.size());
        return new ChatAnalysisResult(participants, mentions);
    }

    /**
     * Извлечь участника из сообщения (если он не является удалённым аккаунтом).
     */
    private void extractParticipant(ChatExport.Message message, Set<Participant> participants) {
        String from = message.getFrom();
        String fromId = message.getFromId();

        // Пропускаем, если нет обязательных полей.
        // fromId должен быть не null и не blank.
        // from должен быть не null (может быть blank, кажется, в Telegram можно указать пустое имя).
        if (fromId == null || fromId.isBlank() || from == null) {
            return;
        }

        // Пропускаем удалённые аккаунты.
        if (isDeletedAccount(from)) {
            log.info("Skipping deleted account: fromId={}, from={}", fromId, from);
            return;
        }

        try {
            participants.add(new Participant(fromId, from));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create Participant: fromId={}, from={}, error={}", fromId, from, e.getMessage());
        }
    }

    /**
     * Извлечь упоминания из сообщения.
     */
    private void extractMentions(ChatExport.Message message, Set<Mention> mentions) {
        if (message.getTextEntities() == null) {
            return;
        }

        for (ChatExport.TextEntity entity : message.getTextEntities()) {
            if (entity == null) {
                continue;
            }

            // Ищем сущности типа "mention".
            if ("mention".equals(entity.getType()) && entity.getText() != null) {
                String mentionText = entity.getText().trim();
                if (!mentionText.isBlank()) {
                    try {
                        mentions.add(new Mention(mentionText));
                    } catch (IllegalArgumentException e) {
                        log.warn("Failed to create Mention: text={}, error={}", mentionText, e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Проверить, является ли аккаунт удалённым.
     */
    private boolean isDeletedAccount(String from) {
        if (from == null) {
            return false;
        }
        String fromLower = from.toLowerCase().trim();
        return fromLower.equals(DELETED_ACCOUNT_NAME_EN.toLowerCase())
                || fromLower.equals(DELETED_ACCOUNT_NAME_RU.toLowerCase());
    }
}
