package ru.hackathon.chatlas.domain;

import java.io.InputStream;

/**
 * Представление сырого файла экспорта чата.
 * Используется для передачи между слоями до парсинга.
 */
public record RawChatFile(String fileName, InputStream content) {
    
    /**
     * Создает RawChatFile из имени файла и потока.
     */
    public RawChatFile {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("fileName cannot be null or blank");
        }
        if (content == null) {
            throw new IllegalArgumentException("content cannot be null");
        }
    }
}

