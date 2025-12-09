package ru.hackathon.chatlas.analysis;

import lombok.extern.slf4j.Slf4j;
import ru.hackathon.chatlas.domain.ChatExport;

/**
 * Заглушка анализатора для демонстрации работы архитектуры.
 *
 * @implNote TODO: Dev3: Заменить на реальную реализацию с логикой извлечения участников и упоминаний
 */
@Slf4j
public class StubChatAnalyzer implements ChatAnalyzer {

    @Override
    public ChatAnalysisResult analyze(ChatExport chatExport) throws ChatAnalysisException {
        log.info("Analyzing chat export");

        // TODO: Dev3: Извлечь участников и упоминания, вернуть ChatAnalysisResult
        return new StubChatAnalysisResult();
    }

    private static class StubChatAnalysisResult implements ChatAnalysisResult {
    }
}

