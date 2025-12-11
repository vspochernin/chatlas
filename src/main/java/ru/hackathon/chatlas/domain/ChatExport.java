package ru.hackathon.chatlas.domain;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Модель экспорта чата.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatExport {
    private String name;
    private String type;
    private Long id;
    private List<ChatExport.Message> messages;

    @Data
    @Slf4j
    @JsonIgnoreProperties({"text"})
    public static class Message {
        private String from;

        @JsonProperty("from_id")
        private String fromId;

        @JsonProperty("text_entities")
        private List<TextEntity> textEntities;

        public String getText() {
            if (textEntities == null) {
                return "";
            }
            return textEntities.stream()
                    .filter(Objects::nonNull)
                    .map(TextEntity::getText)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining());
        }
    }

    @Data
    public static class TextEntity {
        private String type;
        private String text;
    }
}

