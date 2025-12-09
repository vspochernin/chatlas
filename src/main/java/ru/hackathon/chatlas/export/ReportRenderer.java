package ru.hackathon.chatlas.export;

import ru.hackathon.chatlas.analysis.ChatAnalyzer;

/**
 * Сервис для форматирования результата анализа: текст или Excel.
 *
 * @implNote TODO: Dev4: Реализовать логику выбора формата и генерации результата
 */
public interface ReportRenderer {

    /**
     * Отформатировать результат анализа в зависимости от количества сущностей.
     *
     * @param analysisResult результат анализа чата.
     * @return результат в формате текста или Excel.
     * @throws ReportRenderException если не удалось сформировать результат.
     */
    ReportResult render(ChatAnalyzer.ChatAnalysisResult analysisResult) throws ReportRenderException;

    /**
     * Результат форматирования отчета.
     */
    enum OutputType {
        TEXT,
        EXCEL
    }

    /**
     * Результат форматирования.
     */
    interface ReportResult {
        /**
         * @return тип результата (TEXT или EXCEL).
         */
        OutputType getType();

        /**
         * @return текст для отправки в чат (null, если getType() == EXCEL).
         */
        String getText();

        /**
         * @return данные Excel в виде байтов (null, если getType() == TEXT).
         */
        byte[] getExcelBytes();

        /**
         * @return имя файла Excel (null, если getType() == TEXT).
         */
        String getExcelFileName();
    }

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

