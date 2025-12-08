package ru.hackathon.chatlas.telegram;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
public class ChatlasBot implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;

    public ChatlasBot(String botToken) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        log.info("ChatlasBot instance created");
    }

    @Override
    public void consume(Update update) {
        if (update == null) {
            log.warn("Received null update, ignoring");
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText();

            log.info("Received message from chat {}: '{}'", chatId, text);

            SendMessage message = SendMessage.builder()
                    .chatId(String.valueOf(chatId))
                    .text("Echo: " + text)
                    .build();

            try {
                telegramClient.execute(message);
                log.debug("Sent echo message to chat {}", chatId);
            } catch (TelegramApiException e) {
                log.error("Failed to send echo message to chat {}", chatId, e);
            }
        } else {
            log.debug("Ignoring non-text update: {}", update);
        }
    }
}
