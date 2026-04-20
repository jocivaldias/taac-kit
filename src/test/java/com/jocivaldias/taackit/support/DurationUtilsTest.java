package com.jocivaldias.taackit.support;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class DurationUtilsTest {

    @Test
    void parse_iso8601_returnsCorrectDuration() {
        assertEquals(Duration.ofSeconds(5), DurationUtils.parse("PT5S"));
        assertEquals(Duration.ofMinutes(2), DurationUtils.parse("PT2M"));
        assertEquals(Duration.ofSeconds(90), DurationUtils.parse("PT1M30S"));
    }

    @Test
    void parse_milliseconds_returnsCorrectDuration() {
        assertEquals(Duration.ofMillis(500), DurationUtils.parse("500ms"));
        assertEquals(Duration.ofMillis(1000), DurationUtils.parse("1000ms"));
    }

    @Test
    void parse_seconds_returnsCorrectDuration() {
        assertEquals(Duration.ofSeconds(5), DurationUtils.parse("5s"));
        assertEquals(Duration.ofSeconds(30), DurationUtils.parse("30s"));
    }

    @Test
    void parse_minutes_returnsCorrectDuration() {
        assertEquals(Duration.ofMinutes(2), DurationUtils.parse("2m"));
        assertEquals(Duration.ofMinutes(10), DurationUtils.parse("10m"));
    }

    @Test
    void parse_plainNumber_treatedAsSeconds() {
        assertEquals(Duration.ofSeconds(5), DurationUtils.parse("5"));
        assertEquals(Duration.ofSeconds(60), DurationUtils.parse("60"));
    }

    @Test
    void parse_null_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> DurationUtils.parse(null));
    }

    @Test
    void parse_blank_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> DurationUtils.parse("  "));
    }

    @Test
    void parse_invalid_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> DurationUtils.parse("abc"));
        assertThrows(IllegalArgumentException.class, () -> DurationUtils.parse("1x"));
    }
}
