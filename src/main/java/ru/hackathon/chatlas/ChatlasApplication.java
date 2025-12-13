package ru.hackathon.chatlas;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.hackathon.chatlas.analysis.ChatAnalyzer;
import ru.hackathon.chatlas.analysis.ChatAnalyzerImpl;
import ru.hackathon.chatlas.export.ReportRenderer;
import ru.hackathon.chatlas.export.StubReportRenderer;
import ru.hackathon.chatlas.parser.ChatExportParser;
import ru.hackathon.chatlas.parser.JacksonChatExportParserImpl;
import ru.hackathon.chatlas.telegram.ChatlasBot;
import ru.hackathon.chatlas.telegram.ChatProcessingService;

@Slf4j
public class ChatlasApplication {

    public static void main(String[] args) {
        String botToken = System.getenv("TELEGRAM_BOT_TOKEN");
        if (botToken == null || botToken.isBlank()) {
            log.error("Environment variable TELEGRAM_BOT_TOKEN is null or blank");
            System.exit(1);
        }

        try {
            // TODO: Dev4 - заменить StubReportRenderer на реальную реализацию.
            ChatExportParser parser = new JacksonChatExportParserImpl();
            ChatAnalyzer analyzer = new ChatAnalyzerImpl();
            ReportRenderer renderer = new StubReportRenderer();

            ChatProcessingService processingService = new ChatProcessingService(parser, analyzer, renderer);
            ChatlasBot bot = new ChatlasBot(botToken, processingService);

            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
            botsApplication.registerBot(botToken, bot);
            log.info("Chatlas bot successfully started");
        } catch (TelegramApiException e) {
            log.error("Failed to register Telegram bot", e);
            System.exit(1);
        } catch (Exception e) {
            log.error("Unexpected error in ChatlasApplication", e);
            System.exit(1);
        }
    }
}
