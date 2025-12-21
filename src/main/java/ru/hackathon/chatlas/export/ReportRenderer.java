package ru.hackathon.chatlas.export;

import ru.hackathon.chatlas.domain.ChatAnalysisResult;
import ru.hackathon.chatlas.domain.ReportResult;

/**
 * Сервис для форматирования результата анализа: текст или Excel.
 *
 */
public interface ReportRenderer {

    /**
     * Отформатировать результат анализа в зависимости от количества сущностей.
     *
     * @param analysisResult результат анализа чата.
     * @param fileName имя исходного файла экспорта.
     * @return результат в формате текста или Excel.
     * @throws ReportRenderException если не удалось сформировать результат.
     */
    ReportResult render(ChatAnalysisResult analysisResult, String fileName) throws ReportRenderException;

    /**
     * Исключение при форматировании отчета.
     */
    class ReportRenderException extends Exception {
        public ReportRenderException(String message) {
            super(message);
        }

        public ReportRenderException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
