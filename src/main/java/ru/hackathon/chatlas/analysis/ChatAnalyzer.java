package ru.hackathon.chatlas.analysis;

/**
 * Интерфейс для анализа экспорта чата и извлечения участников и упоминаний.
 *
 * @implNote TODO: Dev3: Реализовать логику извлечения участников и упоминаний из ChatExport
 */
public interface ChatAnalyzer {

    /**
     * Проанализировать экспорт чата и извлечь участников и упоминания.
     *
     * @param chatExport объект доменной модели ChatExport (будет создан Dev2).
     * @return результат анализа с уникальными участниками и упоминаниями.
     * @throws ChatAnalysisException если анализ не удался.
     */
    ChatAnalysisResult analyze(Object chatExport) throws ChatAnalysisException;

    /**
     * Результат анализа чата.
     * Содержит уникальных участников и упоминания.
     *
     * @implNote Dev3: Определить структуру с Set<Participant> и Set<Mention>
     */
    interface ChatAnalysisResult {
        // Будет определено Dev3
        // Должен содержать:
        // - Set<Participant> participants
        // - Set<Mention> mentions
        // - методы для получения counts: participantsCount(), mentionsCount(), totalCount()
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

