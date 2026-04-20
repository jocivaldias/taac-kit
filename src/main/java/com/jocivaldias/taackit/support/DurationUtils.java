package com.jocivaldias.taackit.support;

import java.time.Duration;

public final class DurationUtils {

    private DurationUtils() {
    }

    public static Duration parse(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Duration string must not be blank");
        }
        String trimmed = value.trim();
        try {
            return Duration.parse(trimmed);
        } catch (Exception ignored) {
        }
        try {
            if (trimmed.endsWith("ms")) {
                long ms = Long.parseLong(trimmed.substring(0, trimmed.length() - 2).trim());
                return Duration.ofMillis(ms);
            }
            if (trimmed.endsWith("m")) {
                long m = Long.parseLong(trimmed.substring(0, trimmed.length() - 1).trim());
                return Duration.ofMinutes(m);
            }
            if (trimmed.endsWith("s")) {
                long s = Long.parseLong(trimmed.substring(0, trimmed.length() - 1).trim());
                return Duration.ofSeconds(s);
            }
            return Duration.ofSeconds(Long.parseLong(trimmed));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot parse duration: '" + value + "'", e);
        }
    }
}
