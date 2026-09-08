package com.ktsa.foosball.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MatchResponseDto {

    private Long id;
    private String stage;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime scheduledAt;

    private String status;

    // Frontend expects these field names (names as strings)
    private String playerOne;
    private String playerTwo;
    private String teamOne;
    private String teamTwo;

    /** Challonge-format identifier: "userName1 & userName2" — for Challonge sync reference */
    private String teamOneChallongeName;
    private String teamTwoChallongeName;

    private Long teamOneScore;
    private Long teamTwoScore;
    private String winnerTeam;
    private String winnerPlayer;

    private LocalDateTime createdAt;
    private int roundNumber;
    private long tournamentId;

    /** Category this match belongs to */
    private String category;

    /** Convenience fields for player-facing views */
    private String tournamentName;
    private String venue;

    // Helper setters used by service layer (map entity name fields to DTO fields)
    public void setPlayerOneName(String name) { this.playerOne = name; }
    public void setPlayerTwoName(String name) { this.playerTwo = name; }
    public void setTeamOneName(String name)   { this.teamOne = name; }
    public void setTeamTwoName(String name)   { this.teamTwo = name; }
}



