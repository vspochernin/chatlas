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
        log.info("StubChatAnalyzer.analyze() - stub implementation");

        // TODO: Dev3: В реальной реализации здесь будет:
        // TODO: Dev3: 1. Извлечение участников (по fromId + from) с фильтрацией удалённых аккаунтов.
        // TODO: Dev3: 2. Извлечение упоминаний (TextEntity.type == "mention").
        // TODO: Dev3: 3. Возврат ChatAnalysisResult с Set<Participant> и Set<Mention>.

        return new StubChatAnalysisResult();
    }

    /**
     * Заглушка результата анализа.
     */
    private static class StubChatAnalysisResult implements ChatAnalysisResult {
        // Будет определено Dev3
    }
}

