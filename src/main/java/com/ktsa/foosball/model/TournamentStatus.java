package com.ktsa.foosball.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TournamentStatus {
    UPCOMING,
    ACTIVE,
    COMPLETED,
    CANCELLED;

    @JsonCreator
    public static TournamentStatus fromValue(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim().toUpperCase();

        return switch (normalized) {
            case "UPCOMING" -> UPCOMING;
            case "ACTIVE", "ONGOING" -> ACTIVE;
            case "COMPLETED" -> COMPLETED;
            case "CANCELLED" -> CANCELLED;
            default -> throw new IllegalArgumentException("Unknown status: " + value);
        };
    }
}