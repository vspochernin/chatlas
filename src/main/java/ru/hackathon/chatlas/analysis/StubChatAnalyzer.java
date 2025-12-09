package ru.hackathon.chatlas.analysis;

import lombok.extern.slf4j.Slf4j;

/**
 * Заглушка анализатора для демонстрации работы архитектуры.
 *
 * @implNote Dev3: Заменить на реальную реализацию с логикой извлечения участников и упоминаний
 */
@Slf4j
public class StubChatAnalyzer implements ChatAnalyzer {

    @Override
    public ChatAnalysisResult analyze(Object chatExport) throws ChatAnalysisException {
        log.info("StubChatAnalyzer.analyze() - stub implementation");

        // В реальной реализации здесь будет:
        // 1. Извлечение участников (по fromId + from) с фильтрацией удалённых аккаунтов.
        // 2. Извлечение упоминаний (TextEntity.type == "mention").
        // 3. Возврат ChatAnalysisResult с Set<Participant> и Set<Mention>.

        return new StubChatAnalysisResult();
    }

    /**
     * Заглушка результата анализа.
     */
    private static class StubChatAnalysisResult implements ChatAnalysisResult {
        // Будет определено Dev3
    }
}

