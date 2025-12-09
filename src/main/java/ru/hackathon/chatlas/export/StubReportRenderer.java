package ru.hackathon.chatlas.export;

import lombok.extern.slf4j.Slf4j;
import ru.hackathon.chatlas.analysis.ChatAnalyzer;

/**
 * Заглушка рендерера отчетов для демонстрации работы архитектуры.
 *
 * @implNote TODO: Dev4: Заменить на реальную реализацию с логикой выбора формата и генерации Excel
 */
@Slf4j
public class StubReportRenderer implements ReportRenderer {

    @Override
    public ReportResult render(ChatAnalyzer.ChatAnalysisResult analysisResult) throws ReportRenderException {
        log.info("StubReportRenderer.render() - stub implementation");

        // TODO: Dev4: В реальной реализации здесь будет:
        // TODO: Dev4: 1. Получение totalCount из analysisResult (participantsCount + mentionsCount).
        // TODO: Dev4: 2. Если totalCount < 50 - формирование текстового ответа с участниками и упоминаниями.
        // TODO: Dev4: 3. Если totalCount >= 51 - генерация Excel через Apache POI (два листа: Participants и Mentions).
        // TODO: Dev4: 4. Возврат соответствующего ReportResult.

        return new StubReportResult();
    }

    /**
     * Заглушка результата рендеринга.
     */
    private static class StubReportResult implements ReportResult {
        @Override
        public OutputType getType() {
            return OutputType.TEXT; // Заглушка: всегда возвращаем текст
        }

        @Override
        public String getText() {
            return "Заглушка: результат обработки.\n(Dev4 должен реализовать реальный рендеринг)";
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

