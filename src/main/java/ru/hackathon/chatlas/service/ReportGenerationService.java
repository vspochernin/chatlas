package ru.hackathon.chatlas.service;

import java.io.InputStream;
import java.util.List;

/**
 * Основной сервис для генерации отчетов по истории чата.
 * <p>
 * Координирует работу парсера и сервисов извлечения данных,
 * принимает решение о формате результата (список в чат или Excel).
 *
 * @implNote Dev3: Реализовать основную бизнес-логику с использованием
 * ParticipantExtractionService, MentionExtractionService, AggregationService.
 */
public interface ReportGenerationService {

    /**
     * Обрабатывает один или несколько файлов экспорта чата и возвращает результат.
     *
     * @param fileStreams список потоков файлов (в порядке получения от пользователя).
     * @param fileNames   список имен файлов (соответствует порядку fileStreams).
     * @return результат обработки.
     * @throws ReportGenerationException если обработка не удалась.
     */
    ReportResult processFiles(
            List<InputStream> fileStreams,
            List<String> fileNames)
            throws ReportGenerationException;

    /**
     * Результат обработки файлов.
     * <p>
     * В зависимости от количества участников содержит:
     * - либо список строк для отправки в чат (если участников < 50).
     * - либо данные для Excel-файла (если участников >= 51).
     */
    interface ReportResult {
        /**
         * @return true, если результат должен быть отправлен как Excel-файл.
         */
        boolean isExcelFormat();

        /**
         * @return список строк для отправки в чат (null, если isExcelFormat() == true).
         */
        List<String> getTextLines();

        /**
         * @return данные для Excel-файла (null, если isExcelFormat() == false).
         */
        ExcelData getExcelData();
    }

    /**
     * Данные для формирования Excel-файла.
     * Содержит агрегированные данные по участникам и упоминаниям.
     *
     * @implNote Dev3: Создать DTO с Set<Participant> и Set<Mention>
     */
    interface ExcelData {
        // Будет определено Dev3
    }

    /**
     * Исключение при генерации отчета.
     */
    class ReportGenerationException extends Exception {
        public ReportGenerationException(String message) {
            super(message);
        }

        public ReportGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

