package ru.hackathon.chatlas.excel;

import java.io.ByteArrayOutputStream;

/**
 * Сервис для генерации Excel-файлов с результатами анализа чата.
 * <p>
 * Создает XLSX-файл с двумя вкладками:
 * - Participants (авторы сообщений).
 * - Mentions (упомянутые юзернеймы).
 *
 * @implNote Dev4: Реализовать генерацию Excel через Apache POI согласно спецификации из task.md.
 */
public interface ExcelExportService {

    /**
     * Создает Excel-файл (XLSX) с результатами анализа чата.
     *
     * @param excelData данные для заполнения таблицы.
     * @return массив байтов готового Excel-файла
     * @throws ExcelExportException если не удалось создать файл.
     */
    // TODO: заменить на List<record>
    byte[] generateExcel(Object excelData) throws ExcelExportException;

    /**
     * Создает Excel-файл и записывает его в поток.
     *
     * @param excelData    данные для заполнения таблицы.
     * @param outputStream поток для записи файла.
     * @throws ExcelExportException если не удалось создать файл.
     */
    default void generateExcelToStream(Object excelData, ByteArrayOutputStream outputStream)
            throws ExcelExportException
    {
        byte[] excelBytes = generateExcel(excelData);
        try {
            outputStream.write(excelBytes);
        } catch (Exception e) {
            throw new ExcelExportException("Failed to write Excel to stream", e);
        }
    }

    /**
     * Исключение при генерации Excel-файла.
     */
    class ExcelExportException extends Exception {
        public ExcelExportException(String message) {
            super(message);
        }

        public ExcelExportException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

