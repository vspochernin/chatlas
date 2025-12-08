package ru.hackathon.chatlas;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
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
            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
            botsApplication.registerBot(botToken, new ChatlasBot(botToken));
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
