package com.ktsa.foosball.dto;

import lombok.Data;
import java.util.List;

/**
 * A single request that carries registrations for multiple categories at once.
 * The backend validates ALL entries first — if any fails, nothing is saved.
 */
@Data
public class BatchRegistrationRequestDto {

    /** Email of the main player (player one). */
    private String playerOneEmail;

    /** Email of the partner — required for doubles categories. */
    private String playerTwoEmail;

    /** For doubles free-agent: Defender / Attacker / All-rounder */
    private String partnerPreference;

    /** Optional custom team name when creating a new doubles team. */
    private String teamName;

    /** Use an existing team by ID instead of creating a new one. */
    private Long existingTeamId;

    /** UTR number submitted by the player after payment. */
    private String utrNumber;

    /** List of category registrations the player wants to sign up for. */
    private List<CategoryEntry> categories;

    @Data
    public static class CategoryEntry {
        /** e.g. "Open Singles", "Women's Singles", "Mixed Doubles", "Open Doubles" */
        private String category;

        /**
         * How the doubles slot is filled:
         *   "WITH_PARTNER"      – partnerEmail + optional teamName provided
         *   "EXISTING_TEAM"     – existingTeamId provided
         *   "NEED_PARTNER"      – free agent, partnerPreference required
         *   null / "SINGLE"     – singles category, no partner needed
         */
        private String doublesMode;
    }
}
