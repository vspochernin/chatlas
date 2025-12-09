package ru.hackathon.chatlas.telegram;

import ru.hackathon.chatlas.analysis.ChatAnalyzer;
import ru.hackathon.chatlas.domain.RawChatFile;
import ru.hackathon.chatlas.export.ReportRenderer;
import ru.hackathon.chatlas.parser.ChatExportParser;

/**
 * Фасадный сервис для обработки файла экспорта чата.
 * Координирует работу парсера, анализатора и рендерера.
 *
 * @implNote TODO: Dev3, Dev4: Интегрировать реальные реализации сервисов
 */
public class ChatProcessingService {

    private final ChatExportParser parser;
    private final ChatAnalyzer analyzer;
    private final ReportRenderer renderer;

    public ChatProcessingService(
            ChatExportParser parser,
            ChatAnalyzer analyzer,
            ReportRenderer renderer)
    {
        this.parser = parser;
        this.analyzer = analyzer;
        this.renderer = renderer;
    }

    /**
     * Обработать один файл экспорта чата: распарсить, проанализировать, отформатировать результат.
     *
     * @param file файл экспорта чата.
     * @return результат обработки в формате текста или Excel.
     * @throws ChatProcessingException если обработка не удалась.
     */
    public ReportRenderer.ReportResult process(RawChatFile file) throws ChatProcessingException {
        try {
            // 1. Парсим JSON в доменную модель
            var chatExport = parser.parse(file);

            // 2. Анализируем и извлекаем участников/упоминания
            ChatAnalyzer.ChatAnalysisResult analysisResult = analyzer.analyze(chatExport);

            // 3. Форматируем результат (текст или Excel)
            return renderer.render(analysisResult);

        } catch (ChatExportParser.ChatExportParseException e) {
            throw new ChatProcessingException("Failed to parse chat export", e);
        } catch (ChatAnalyzer.ChatAnalysisException e) {
            throw new ChatProcessingException("Failed to analyze chat export", e);
        } catch (ReportRenderer.ReportRenderException e) {
            throw new ChatProcessingException("Failed to render report", e);
        }
    }

    /**
     * Исключение при обработке файла.
     */
    public static class ChatProcessingException extends Exception {
        public ChatProcessingException(String message) {
            super(message);
        }

        public ChatProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

