package ru.hackathon.chatlas.export;

import ru.hackathon.chatlas.domain.ChatAnalysisResult;
import ru.hackathon.chatlas.domain.ReportResult;

/**
 * Сервис для форматирования результата анализа.
 * Генерирует текстовый ответ или Excel-файл в зависимости от количества сущностей.
 *
 */
public interface ReportRenderer {

    /**
     * Отформатировать результат анализа: создаёт текстовый ответ или Excel-файл в зависимости от количества сущностей.
     *
     * @param analysisResult результат анализа чата.
     * @param fileName имя исходного файла экспорта.
     * @return результат форматирования (ReportTextResult или ReportExcelResult).
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
