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
     *
     * @implNote TODO: Dev3: Определить структуру с Set<Participant> и Set<Mention>
     */
    interface ChatAnalysisResult {
        // TODO: Dev3: Добавить методы для получения участников, упоминаний и их количества
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

