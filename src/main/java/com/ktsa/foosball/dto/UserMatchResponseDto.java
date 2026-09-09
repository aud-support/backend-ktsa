package com.ktsa.foosball.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Match summary tailored for a specific user's profile view.
 * Includes the tournament name, the user's opponent, result, and score.
 */
@Data
public class UserMatchResponseDto {

    private Long id;

    /** Tournament name */
    private String tournamentName;

    /** Venue / location of the tournament */
    private String location;

    /** Match category (e.g. MENS_SINGLES, OPEN_DOUBLES) */
    private String category;

    /** "upcoming" or "past" derived from status */
    private String status;

    /** Opponent name — the other player/team the user is facing */
    private String opponent;

    /** Score string e.g. "3-1" — null for upcoming matches */
    private String score;

    /** "win", "loss", or null for upcoming/draw */
    private String result;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledAt;

    private Long tournamentId;
}
