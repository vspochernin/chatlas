package ru.hackathon.chatlas.analysis;

import ru.hackathon.chatlas.domain.ChatExport;

/**
 * Интерфейс для анализа экспорта чата и извлечения участников и упоминаний.
 *
 * @implNote TODO: Dev3: Реализовать логику извлечения участников и упоминаний из ChatExport
 */
public interface ChatAnalyzer {

    /**
     * Проанализировать экспорт чата и извлечь участников и упоминания.
     *
     * @param chatExport объект доменной модели ChatExport. TODO: Dev2: Будет создана конкретная реализация.
     * @return результат анализа с уникальными участниками и упоминаниями.
     * @throws ChatAnalysisException если анализ не удался.
     */
    ChatAnalysisResult analyze(ChatExport chatExport) throws ChatAnalysisException;

    /**
     * Результат анализа чата.
     * Содержит уникальных участников и упоминания.
     *
     * @implNote TODO: Dev3: Определить структуру с Set<Participant> и Set<Mention>
     */
    interface ChatAnalysisResult {
        // TODO: Dev3: Будет определено Dev3
        // TODO: Dev3: Должен содержать:
        // TODO: Dev3: - Set<Participant> participants
        // TODO: Dev3: - Set<Mention> mentions
        // TODO: Dev3: - методы для получения counts: participantsCount(), mentionsCount(), totalCount()
    }

    /**
     * Исключение при анализе чата.
     */
    class ChatAnalysisException extends Exception {
        public ChatAnalysisException(String message) {
            super(message);
        }

        public ChatAnalysisException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

