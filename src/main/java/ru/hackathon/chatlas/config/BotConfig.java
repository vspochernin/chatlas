package ru.hackathon.chatlas.config;

import lombok.Getter;

/**
 * Конфигурация бота с основными параметрами.
 */
@Getter
public class BotConfig {

    /**
     * Порог для выбора формата результата.
     * Если общее количество сущностей (участники + упоминания) >= этого значения, отправляется Excel-файл.
     * Иначе - список в чат.
     */
    public static final int EXCEL_THRESHOLD = 51;

    private BotConfig() {
        // Утильный класс.
    }
}

