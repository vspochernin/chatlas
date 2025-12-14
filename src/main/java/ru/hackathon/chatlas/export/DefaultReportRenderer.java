package ru.hackathon.chatlas.export;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.hackathon.chatlas.domain.ChatAnalysisResult;
import ru.hackathon.chatlas.domain.Mention;
import ru.hackathon.chatlas.domain.Participant;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DefaultReportRenderer implements ReportRenderer {

    private static final int TEXT_LIMIT = 50;

    @Override
    public ReportResult render(ChatAnalysisResult analysisResult) throws ReportRenderException {
        try {
            if (analysisResult.getParticipantsCount() < TEXT_LIMIT) {
                return renderText(analysisResult);
            }
            return renderExcel(analysisResult);
        } catch (Exception e) {
            throw new ReportRenderException("Failed to render report", e);
        }
    }

    private ReportResult renderText(ChatAnalysisResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("👥 Участники чата:\n\n");

        result.participants().forEach(p ->
                sb.append("- ")
                        .append(p.fromId())
                        .append(p.displayName().isBlank() ? "" : " (" + p.displayName() + ")")
                        .append("\n")
        );

        sb.append("\n👥 Упоминания:\n\n");

        result.mentions().forEach(p ->
                sb.append("- ")
                        .append(p.mentionText())
                        .append("\n")
        );

        return new TextReportResult(sb.toString());
    }

    private ReportResult renderExcel(ChatAnalysisResult result) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheetMembers = workbook.createSheet("Chat Export Участники");
            Sheet sheetMentions = workbook.createSheet("Chat Export Упоминания");

            createHeaderMembers(sheetMembers);
            createHeaderMentions(sheetMentions);

            List<RowData> rowsMembers = collectRowsMembers(result);
            writeRows(sheetMembers, rowsMembers);

            List<RowData> rowsMentions = collectRowsMentions(result);
            writeRows(sheetMentions, rowsMentions);

            autosize(sheetMembers, 3);
            autosize(sheetMentions, 2);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            return new ExcelReportResult(
                    out.toByteArray(),
                    "chat-export-" + LocalDate.now() + ".xlsx"
            );
        }
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
                "Ник в упоминании"
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

    private void writeRows(Sheet sheet, List<RowData> rows) {
        int rowIndex = 1;
        for (RowData data : rows) {
            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(data.exportDate);
            row.createCell(1).setCellValue(data.username);
            row.createCell(2).setCellValue(data.fullName);
        }
    }

    private void autosize(Sheet sheet, int count) {
        for (int i = 0; i < count; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /* ================= DTO ================= */

    private record RowData(
            String exportDate,
            String username,
            String fullName
    ) {}

    /* ================= RESULT IMPL ================= */

    private static class TextReportResult implements ReportResult {
        private final String text;

        TextReportResult(String text) {
            this.text = text;
        }

        @Override
        public OutputType getType() {
            return OutputType.TEXT;
        }

        @Override
        public String getText() {
            return text;
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

    private static class ExcelReportResult implements ReportResult {
        private final byte[] bytes;
        private final String filename;

        ExcelReportResult(byte[] bytes, String filename) {
            this.bytes = bytes;
            this.filename = filename;
        }

        @Override
        public OutputType getType() {
            return OutputType.EXCEL;
        }

        @Override
        public String getText() {
            return null;
        }

        @Override
        public byte[] getExcelBytes() {
            return bytes;
        }

        @Override
        public String getExcelFileName() {
            return filename;
        }
    }
}
