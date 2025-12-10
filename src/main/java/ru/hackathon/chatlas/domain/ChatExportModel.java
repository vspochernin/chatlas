package ru.hackathon.chatlas.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatExportModel implements ChatExport {
    private String name;
    private String type;
    private Long id;
    private List<Message> messages;

    @Data
    public static class Message {
        private Long id;
        private String type;
        private LocalDateTime date;
        private Long dateUnixtime;
        private String from;
        private String fromId;
        private String actor;
        private String actorId;
        private String action;
        private List<String> members;
        private String text;
        private List<TextEntity> textEntities;
    }

    @Data
    public static class TextEntity {
        private String type;
        private String text;
    }
}
