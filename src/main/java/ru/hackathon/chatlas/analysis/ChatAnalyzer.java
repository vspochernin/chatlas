package ru.hackathon.chatlas.analysis;

import ru.hackathon.chatlas.domain.ChatAnalysisResult;
import ru.hackathon.chatlas.domain.ChatExport;

/**
 * Интерфейс для анализа экспорта чата и извлечения участников и упоминаний.
 */
public interface ChatAnalyzer {

    /**
     * Проанализировать экспорт чата и извлечь участников и упоминания.
     *
     * @param chatExport объект доменной модели ChatExport.
     * @return результат анализа с уникальными участниками и упоминаниями.
     * @throws ChatAnalysisException если анализ не удался.
     */
    ChatAnalysisResult analyze(ChatExport chatExport) throws ChatAnalysisException;

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
