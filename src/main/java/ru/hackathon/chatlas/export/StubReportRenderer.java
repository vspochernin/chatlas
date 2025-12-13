package ru.hackathon.chatlas.export;

import lombok.extern.slf4j.Slf4j;
import ru.hackathon.chatlas.domain.ChatAnalysisResult;

/**
 * Заглушка рендерера отчетов для демонстрации работы архитектуры.
 *
 * @implNote TODO: Dev4: Заменить на реальную реализацию с логикой выбора формата и генерации Excel
 */
@Slf4j
public class StubReportRenderer implements ReportRenderer {

    @Override
    public ReportResult render(ChatAnalysisResult analysisResult) throws ReportRenderException {
        log.info("Rendering report");

        // TODO: Dev4: Получить totalCount, выбрать формат (TEXT/EXCEL), сгенерировать результат
        return new StubReportResult();
    }

    private static class StubReportResult implements ReportResult {
        @Override
        public OutputType getType() {
            return OutputType.TEXT;
        }

        @Override
        public String getText() {
            return "Результат обработки (заглушка)";
        }

        @Override
        public byte[] getExcelBytes() {
            return null;
        }

        @Override
        public String getExcelFileName() {
            return null;
        }
    }
}
