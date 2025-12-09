package ru.hackathon.chatlas;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.hackathon.chatlas.excel.ExcelExportService;
import ru.hackathon.chatlas.excel.StubExcelExportService;
import ru.hackathon.chatlas.service.ReportGenerationService;
import ru.hackathon.chatlas.service.StubReportGenerationService;
import ru.hackathon.chatlas.telegram.ChatlasBot;

@Slf4j
public class ChatlasApplication {

    public static void main(String[] args) {
        String botToken = System.getenv("TELEGRAM_BOT_TOKEN");
        if (botToken == null || botToken.isBlank()) {
            log.error("Environment variable TELEGRAM_BOT_TOKEN is null or blank");
            System.exit(1);
        }

        try {
            // Dev2, Dev3, Dev4 - заменить на реальные реализации.
            // PS. ChatExportParser будет использоваться внутри ReportGenerationService (актуально для Dev3).
            ReportGenerationService reportService = new StubReportGenerationService();
            ExcelExportService excelService = new StubExcelExportService();

            // Создаем бота с зависимостями
            ChatlasBot bot = new ChatlasBot(botToken, reportService, excelService);

            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
            botsApplication.registerBot(botToken, bot);
            log.info("Chatlas bot successfully started");

            // Так как у нас long-polling => приложение будет работать бесконечно до завершения процесса.
        } catch (TelegramApiException e) {
            log.error("Failed to register Telegram bot", e);
            System.exit(1);
        } catch (Exception e) {
            log.error("Unexpected error in ChatlasApplication", e);
            System.exit(1);
        }
    }
}
