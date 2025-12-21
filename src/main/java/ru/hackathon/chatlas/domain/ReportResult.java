package ru.hackathon.chatlas.domain;

/**
 * Результат форматирования отчета.
 * Всегда содержит и текстовый ответ, и Excel-файл.
 * Выбор что использовать делается на уровне вывода на основе порога.
 */
public record ReportResult(
        ReportOutputType recommendedType,
        String text,
        byte[] excelBytes,
        String excelFileName
) {
    public ReportResult {
        if (recommendedType == null) {
            throw new IllegalArgumentException("recommendedType cannot be null");
        }
        if (text == null) {
            throw new IllegalArgumentException("text cannot be null");
        }
        if (excelBytes == null) {
            throw new IllegalArgumentException("excelBytes cannot be null");
        }
        if (excelFileName == null) {
            throw new IllegalArgumentException("excelFileName cannot be null");
        }
    }
}

