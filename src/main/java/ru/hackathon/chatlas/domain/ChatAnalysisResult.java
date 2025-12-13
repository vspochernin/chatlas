package ru.hackathon.chatlas.domain;

import java.util.Set;

/**
 * Результат анализа чата: уникальные участники и упоминания.
 */
public record ChatAnalysisResult(Set<Participant> participants, Set<Mention> mentions) {

    public ChatAnalysisResult {
        if (participants == null) {
            throw new IllegalArgumentException("participants cannot be null");
        }
        if (mentions == null) {
            throw new IllegalArgumentException("mentions cannot be null");
        }
    }

    /**
     * Получить количество уникальных участников.
     */
    public int getParticipantsCount() {
        return participants.size();
    }

    /**
     * Получить количество уникальных упоминаний.
     */
    public int getMentionsCount() {
        return mentions.size();
    }

    /**
     * Получить общее количество уникальных сущностей (участники + упоминания).
     */
    public int getTotalCount() {
        return getParticipantsCount() + getMentionsCount();
    }
}
