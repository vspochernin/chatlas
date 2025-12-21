package ru.hackathon.chatlas.domain;

/**
 * Результат форматирования отчета.
 */
public record ReportResult(
        ReportOutputType type,
        String text,
        byte[] excelBytes,
        String excelFileName
) {
    public ReportResult {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        // text и excelBytes могут быть null в зависимости от type
        // excelFileName может быть null, если type == TEXT
    }
}

