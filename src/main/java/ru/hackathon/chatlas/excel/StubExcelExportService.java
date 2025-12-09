package ru.hackathon.chatlas.excel;

import lombok.extern.slf4j.Slf4j;

/**
 * Заглушка сервиса генерации Excel для демонстрации работы архитектуры.
 *
 * @implNote Dev4: Заменить на реальную реализацию через Apache POI.
 */
@Slf4j
public class StubExcelExportService implements ExcelExportService {

    @Override
    public byte[] generateExcel(Object excelData) throws ExcelExportException {
        log.info("StubExcelExportService.generateExcel() - stub implementation");

        // В реальной реализации здесь будет:
        // 1. Создание Workbook через Apache POI.
        // 2. Создание листов Participants и Mentions.
        // 3. Заполнение колонок согласно спецификации из task.md.
        // 4. Запись в ByteArrayOutputStream и возврат байтов.

        // Заглушка: возвращаем пустой массив.
        log.warn("Stub implementation: returning empty Excel file");
        return new byte[0];
    }
}

