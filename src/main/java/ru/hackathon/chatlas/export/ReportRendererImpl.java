package ru.hackathon.chatlas.export;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.hackathon.chatlas.config.BotConfig;
import ru.hackathon.chatlas.domain.ChatAnalysisResult;
import ru.hackathon.chatlas.domain.Mention;
import ru.hackathon.chatlas.domain.Participant;
import ru.hackathon.chatlas.domain.ReportExcelResult;
import ru.hackathon.chatlas.domain.ReportResult;
import ru.hackathon.chatlas.domain.ReportTextResult;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportRendererImpl implements ReportRenderer {

    @Override
    public ReportResult render(ChatAnalysisResult analysisResult, String fileName) throws ReportRenderException {
        try {
            int totalCount = analysisResult.getTotalCount();

            if (totalCount < BotConfig.EXCEL_THRESHOLD) {
                // Генерируем только текстовый ответ
                String text = renderText(analysisResult, fileName);
                return new ReportTextResult(fileName, text);
            } else {
                // Генерируем только Excel-файл
                byte[] excelBytes = renderExcel(analysisResult, fileName);
                String excelFileName = generateExcelFileName(fileName);
                return new ReportExcelResult(fileName, excelBytes, excelFileName);
            }
        } catch (Exception e) {
            throw new ReportRenderException("Failed to render report", e);
        }
    }

    private String renderText(ChatAnalysisResult result, String fileName) {
        StringBuilder sb = new StringBuilder();
        int participantsCount = result.getParticipantsCount();
        int mentionsCount = result.getMentionsCount();

        sb.append("Файл: ").append(fileName).append("\n");
        sb.append("Количество участников: ").append(participantsCount).append("\n");
        sb.append("Количество упоминаний: ").append(mentionsCount).append("\n\n");

        sb.append("Участники:\n");
        result.participants().forEach(p ->
                sb.append("- ")
                        .append(p.displayName())
                        .append("\n")
        );

        if (mentionsCount > 0) {
            sb.append("\nУпоминания:\n");
            result.mentions().forEach(m ->
                    sb.append("- ")
                            .append(m.mentionText())
                            .append("\n")
            );
        }

        return sb.toString();
    }

    private byte[] renderExcel(ChatAnalysisResult result, String fileName) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheetMembers = workbook.createSheet("Chat Export Участники");
            Sheet sheetMentions = workbook.createSheet("Chat Export Упоминания");

            createHeaderMembers(sheetMembers);
            createHeaderMentions(sheetMentions);

            List<RowData> rowsMembers = collectRowsMembers(result);
            writeRowsMembers(sheetMembers, rowsMembers);

            List<RowData> rowsMentions = collectRowsMentions(result);
            writeRowsMentions(sheetMentions, rowsMentions);

            autosize(sheetMembers, 3);
            autosize(sheetMentions, 2);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private String generateExcelFileName(String fileName) {
        String baseFileName = fileName != null && !fileName.isBlank()
                ? sanitizeFileName(fileName.replace(".json", ""))
                : "chat-export";
        return baseFileName + "-" + LocalDate.now() + ".xlsx";
    }

    private void createHeaderMembers(Sheet sheet) {
        Row header = sheet.createRow(0);

        String[] columns = {
                "Дата экспорта",
                "UserId",
                "Имя и фамилия",
        };

        for (int i = 0; i < columns.length; i++) {
            header.createCell(i).setCellValue(columns[i]);
        }
    }

    private void createHeaderMentions(Sheet sheet) {
        Row header = sheet.createRow(0);

        String[] columns = {
                "Дата экспорта",
                "Username"
        };

        for (int i = 0; i < columns.length; i++) {
            header.createCell(i).setCellValue(columns[i]);
        }
    }

    private List<RowData> collectRowsMembers(ChatAnalysisResult result) {
        List<RowData> rows = new ArrayList<>();
        LocalDate exportDate = LocalDate.now();

        // Участники
        for (Participant p : result.participants()) {
            rows.add(new RowData(
                    exportDate.toString(),
                    p.fromId(),
                    p.displayName()
            ));
        }

        return rows;
    }

    private List<RowData> collectRowsMentions(ChatAnalysisResult result) {
        List<RowData> rows = new ArrayList<>();
        LocalDate exportDate = LocalDate.now();

        // Упоминания
        for (Mention m : result.mentions()) {
            rows.add(new RowData(
                    exportDate.toString(),
                    m.mentionText(),
                    ""
            ));
        }

        return rows;
    }

    private void writeRowsMembers(Sheet sheet, List<RowData> rows) {
        int rowIndex = 1;
        for (RowData data : rows) {
            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(data.exportDate);
            row.createCell(1).setCellValue(data.username);
            row.createCell(2).setCellValue(data.fullName);
        }
    }

    private void writeRowsMentions(Sheet sheet, List<RowData> rows) {
        int rowIndex = 1;
        for (RowData data : rows) {
            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(data.exportDate);
            row.createCell(1).setCellValue(data.username);
        }
    }

    private void autosize(Sheet sheet, int count) {
        for (int i = 0; i < count; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "chat-export";
        }
        // Удаляем недопустимые символы для имени файла
        return fileName.replaceAll("[^a-zA-Z0-9а-яА-ЯёЁ_\\-\\s]", "_")
                .replaceAll("\\s+", "_")
                .trim();
    }

    /* ================= DTO ================= */

    private record RowData(
            String exportDate,
            String username,
            String fullName
    ) {}

}
