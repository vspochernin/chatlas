package ru.hackathon.chatlas.export;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import ru.hackathon.chatlas.analysis.ChatAnalyzerImpl;
import ru.hackathon.chatlas.config.BotConfig;
import ru.hackathon.chatlas.domain.ChatAnalysisResult;
import ru.hackathon.chatlas.domain.ReportOutputType;
import ru.hackathon.chatlas.domain.ReportResult;
import ru.hackathon.chatlas.domain.ChatExport;
import ru.hackathon.chatlas.domain.Mention;
import ru.hackathon.chatlas.domain.Participant;
import ru.hackathon.chatlas.domain.RawChatFile;
import ru.hackathon.chatlas.parser.ChatExportParserImpl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class ReportRendererImplIntegrationTest {

    private final ChatExportParserImpl parser = new ChatExportParserImpl();
    private final ChatAnalyzerImpl analyzer = new ChatAnalyzerImpl();
    private final ReportRendererImpl renderer = new ReportRendererImpl();

    @Test
    void shouldRenderTextForRealChatExport() throws Exception {
        String jsonContent = readResourceAsString("chat1.json");
        RawChatFile rawFile = new RawChatFile("chat1.json", jsonContent);

        ChatExport chatExport = parser.parse(rawFile);
        ChatAnalysisResult analysisResult = analyzer.analyze(chatExport);
        ReportResult reportResult = renderer.render(analysisResult, "chat1.json");

        // Должен быть текстовый ответ, так как totalCount = 4 < 50
        assertEquals(ReportOutputType.TEXT, reportResult.recommendedType());
        assertNotNull(reportResult.text());
        assertNotNull(reportResult.excelBytes());
        assertNotNull(reportResult.excelFileName());

        String text = reportResult.text();

        // Проверяем заголовок с количеством
        assertTrue(text.contains("Количество участников: 2"));
        assertTrue(text.contains("Количество упоминаний: 2"));

        // Проверяем участников
        assertTrue(text.contains("Участники:"));
        assertTrue(text.contains("Владислав Почернин"));
        assertTrue(text.contains("Егор Мартынов"));

        // Проверяем упоминания
        assertTrue(text.contains("Упоминания:"));
        assertTrue(text.contains("@vspochernin"));
        assertTrue(text.contains("@vspocherninwork"));
    }

    @Test
    void shouldRenderExcelForLargeChatExport() throws Exception {
        // Создаем большой набор данных, чтобы превысить порог
        ChatAnalysisResult analysisResult = new ChatAnalysisResult(
                createParticipants(BotConfig.EXCEL_THRESHOLD / 2 + 1),
                createMentions(BotConfig.EXCEL_THRESHOLD / 2 + 1)
        );

        ReportResult reportResult = renderer.render(analysisResult, "chat1.json");

        // Должен быть Excel, так как totalCount >= 51
        assertEquals(ReportOutputType.EXCEL, reportResult.recommendedType());
        assertNotNull(reportResult.text());
        assertNotNull(reportResult.excelBytes());
        assertNotNull(reportResult.excelFileName());
        assertTrue(reportResult.excelFileName().endsWith(".xlsx"));

        // Проверяем структуру Excel файла
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(reportResult.excelBytes()))) {
            assertEquals(2, workbook.getNumberOfSheets());

            // Проверяем лист Participants
            Sheet participantsSheet = workbook.getSheetAt(0);
            assertEquals("Chat Export Участники", participantsSheet.getSheetName());
            Row headerRow = participantsSheet.getRow(0);
            assertEquals("Дата экспорта", headerRow.getCell(0).getStringCellValue());
            assertEquals("UserId", headerRow.getCell(1).getStringCellValue());
            assertEquals("Имя и фамилия", headerRow.getCell(2).getStringCellValue());
            assertTrue(participantsSheet.getLastRowNum() >= BotConfig.EXCEL_THRESHOLD / 2);

            // Проверяем лист Mentions
            Sheet mentionsSheet = workbook.getSheetAt(1);
            assertEquals("Chat Export Упоминания", mentionsSheet.getSheetName());
            Row mentionsHeaderRow = mentionsSheet.getRow(0);
            assertEquals("Дата экспорта", mentionsHeaderRow.getCell(0).getStringCellValue());
            assertEquals("Username", mentionsHeaderRow.getCell(1).getStringCellValue());
            assertEquals(2, mentionsHeaderRow.getLastCellNum());
            assertTrue(mentionsSheet.getLastRowNum() >= BotConfig.EXCEL_THRESHOLD / 2);
        }
    }

    @Test
    void shouldRenderTextWhenExactlyOneLessThanThreshold() throws Exception {
        // Создаем результат с totalCount = 50 (порог - 1)
        ChatAnalysisResult analysisResult = new ChatAnalysisResult(
                createParticipants(BotConfig.EXCEL_THRESHOLD - 1),
                Set.of()
        );

        ReportResult reportResult = renderer.render(analysisResult, "chat1.json");

        assertEquals(ReportOutputType.TEXT, reportResult.recommendedType());
        assertNotNull(reportResult.text());
    }

    @Test
    void shouldRenderExcelWhenExactlyAtThreshold() throws Exception {
        // Создаем результат с totalCount = 51 (порог)
        ChatAnalysisResult analysisResult = new ChatAnalysisResult(
                createParticipants(BotConfig.EXCEL_THRESHOLD),
                Set.of()
        );

        ReportResult reportResult = renderer.render(analysisResult, "chat1.json");

        assertEquals(ReportOutputType.EXCEL, reportResult.recommendedType());
        assertNotNull(reportResult.excelBytes());
    }

    private String readResourceAsString(String resourceName) throws Exception {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(resourceName);
        assertNotNull(stream, "Resource not found: " + resourceName);
        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }

    private Set<Participant> createParticipants(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> new Participant("user" + i, "Имя " + i))
                .collect(java.util.stream.Collectors.toSet());
    }

    private Set<Mention> createMentions(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> new Mention("@username" + i))
                .collect(java.util.stream.Collectors.toSet());
    }
}

