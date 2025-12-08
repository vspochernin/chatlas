package ru.hackathon.chatlas.config;

import lombok.Getter;

/**
 * Конфигурация бота с основными параметрами.
 * Все параметры можно менять здесь, не затрагивая остальной код.
 */
@Getter
public class BotConfig {

    /**
     * Максимальное количество файлов, которые пользователь может отправить за раз.
     * Согласно заданию, нужно явно указать ограничение Telegram (например: до 10 файлов).
     */
    public static final int MAX_FILES_PER_USER = 10;

    /**
     * Порог для выбора формата результата.
     * Если количество участников >= этого значения, отправляется Excel-файл.
     * Иначе - список в чат.
     */
    public static final int EXCEL_THRESHOLD = 51;

    /**
     * Минимальное количество участников для списка в чат.
     */
    public static final int MIN_PARTICIPANTS_FOR_LIST = 0;

    private BotConfig() {
        // Utility class
    }
}

