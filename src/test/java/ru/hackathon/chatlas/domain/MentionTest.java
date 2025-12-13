package ru.hackathon.chatlas.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MentionTest {

    @Test
    void shouldCreateMentionWithValidText() {
        Mention mention = new Mention("@username");
        assertEquals("@username", mention.mentionText());
    }

    @Test
    void shouldCreateMentionWithComplexText() {
        Mention mention = new Mention("@user_name_123");
        assertEquals("@user_name_123", mention.mentionText());
    }

    @Test
    void shouldThrowExceptionOnNullMentionText() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Mention(null);
        });
    }

    @Test
    void shouldThrowExceptionOnBlankMentionText() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Mention("");
        });
    }

    @Test
    void shouldThrowExceptionOnWhitespaceMentionText() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Mention("   ");
        });
    }

    @Test
    void shouldThrowExceptionIfNotStartsWithAt() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Mention("username");
        });
    }

    @Test
    void shouldThrowExceptionIfStartsWithSpaceThenAt() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Mention(" @username");
        });
    }

    @Test
    void shouldEqualByMentionText() {
        Mention m1 = new Mention("@username");
        Mention m2 = new Mention("@username");
        Mention m3 = new Mention("@username");

        assertEquals(m1, m2);
        assertEquals(m1, m3);
        assertEquals(m1.hashCode(), m2.hashCode());
        assertEquals(m1.hashCode(), m3.hashCode());
    }

    @Test
    void shouldNotEqualByDifferentMentionText() {
        Mention m1 = new Mention("@username1");
        Mention m2 = new Mention("@username2");

        assertNotEquals(m1, m2);
        assertNotEquals(m1.hashCode(), m2.hashCode());
    }

    @Test
    void shouldBeCaseSensitive() {
        Mention m1 = new Mention("@Username");
        Mention m2 = new Mention("@username");

        assertNotEquals(m1, m2);
    }
}

