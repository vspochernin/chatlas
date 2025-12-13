package ru.hackathon.chatlas.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParticipantTest {

    @Test
    void shouldCreateParticipantWithBlankDisplayName() {
        Participant participant = new Participant("user123", "");
        assertEquals("user123", participant.fromId());
        assertEquals("", participant.displayName());
    }

    @Test
    void shouldCreateParticipantWithWhitespaceDisplayName() {
        Participant participant = new Participant("user123", "   ");
        assertEquals("user123", participant.fromId());
        assertEquals("   ", participant.displayName());
    }

    @Test
    void shouldThrowExceptionOnNullDisplayName() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Participant("user123", null);
        });
    }

    @Test
    void shouldThrowExceptionOnNullFromId() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Participant(null, "Display Name");
        });
    }

    @Test
    void shouldThrowExceptionOnBlankFromId() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Participant("", "Display Name");
        });
    }

    @Test
    void shouldEqualByFromId() {
        Participant p1 = new Participant("user123", "Name 1");
        Participant p2 = new Participant("user123", "Name 2");
        Participant p3 = new Participant("user123", "");

        assertEquals(p1, p2);
        assertEquals(p1, p3);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertEquals(p1.hashCode(), p3.hashCode());
    }

    @Test
    void shouldNotEqualByDifferentFromId() {
        Participant p1 = new Participant("user123", "Name");
        Participant p2 = new Participant("user456", "Name");

        assertNotEquals(p1, p2);
    }
}

