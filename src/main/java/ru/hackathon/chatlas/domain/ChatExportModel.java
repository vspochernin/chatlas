package ru.hackathon.chatlas.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatExportModel implements ChatExport {
    private String name;
    private String type;
    private Long id;
    private List<Message> messages;

    @Data
    @Slf4j
    public static class Message {
        private String from;

        @JsonProperty("from_id")
        private String fromId;

        @JsonDeserialize(using = TextDeserializer.class)
        private String text;

        @JsonProperty("text_entities")
        private List<TextEntity> textEntities;

        @Override
        public String toString() {
            return "Message{" +
                    "from='" + from + '\'' +
                    ", fromId='" + fromId + '\'' +
                    ", text='" + text + '\'' +
                    ", textEntities=" + textEntities +
                    '}';
        }
    }

    @Data
    public static class TextEntity {
        private String type;
        private String text;
    }

    // Десериализатор для поля text
    @Slf4j
    public static class TextDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.currentToken() == JsonToken.START_ARRAY) {
                StringBuilder result = new StringBuilder();
                while (p.nextToken() != JsonToken.END_ARRAY) {
                    if (p.currentToken() == JsonToken.VALUE_STRING) {
                        result.append(p.getText());
                    } else if (p.currentToken() == JsonToken.START_OBJECT) {
                        String textValue = extractTextFromObject(p);
                        if (textValue != null) {
                            result.append(textValue);
                        }
                    }
                }
                return result.toString();
            } else if (p.currentToken() == JsonToken.VALUE_STRING) {
                return p.getText();
            } else if (p.currentToken() == JsonToken.VALUE_NULL) {
                return null;
            }
            return p.getText();
        }

        private String extractTextFromObject(JsonParser p) throws IOException {
            String textValue = null;
            while (p.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = p.currentName();
                p.nextToken();
                if ("text".equals(fieldName) && p.currentToken() == JsonToken.VALUE_STRING) {
                    textValue = p.getText();
                } else {
                    p.skipChildren();
                }
            }
            return textValue;
        }
    }
}