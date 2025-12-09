package ru.hackathon.chatlas.service;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Заглушка сервиса генерации отчетов для демонстрации работы архитектуры.
 *
 * @implNote Dev3: Заменить на реальную реализацию с бизнес-логикой
 */
@Slf4j
public class StubReportGenerationService implements ReportGenerationService {

    @Override
    // TODO: заменить на List<record>.
    public ReportResult processFiles(List<InputStream> fileStreams, List<String> fileNames)
            throws ReportGenerationException
    {
        log.info("StubReportGenerationService.processFiles(fileCount={}, files={})", fileStreams.size(), fileNames);

        // В реальной реализации здесь будет:
        // 1. Вызов ChatExportParser для каждого файла.
        // 2. Вызов ParticipantExtractionService для сбора участников.
        // 3. Вызов MentionExtractionService для сбора упоминаний.
        // 4. Вызов AggregationService для объединения результатов.
        // 5. Проверка количества участников и выбор формата (список или Excel).

        // Заглушка.
        List<String> stubLines = new ArrayList<>();
        stubLines.add("Заглушка: файлы получены и обработаны.");
        stubLines.add("Количество файлов: " + fileStreams.size());
        stubLines.add("Имена файлов: " + String.join(", ", fileNames));
        stubLines.add("");

        return new StubReportResult(stubLines);
    }

    /**
     * Заглушка результата отчета.
     */
    private static class StubReportResult implements ReportResult {

        private final List<String> textLines;

        public StubReportResult(List<String> textLines) {
            this.textLines = textLines;
        }

        @Override
        public boolean isExcelFormat() {
            return false;
        }

        @Override
        public List<String> getTextLines() {
            return textLines;
        }

        @Override
        public ExcelData getExcelData() {
            return null;
        }
    }
}

