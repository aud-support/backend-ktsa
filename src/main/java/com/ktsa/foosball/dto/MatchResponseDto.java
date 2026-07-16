package com.ktsa.foosball.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MatchResponseDto {

    private Long id;
    private String stage;
    private String scheduledAt;
    private String status;

    // Frontend expects these field names (names as strings)
    private String playerOne;
    private String playerTwo;
    private String teamOne;
    private String teamTwo;

    private Long teamOneScore;
    private Long teamTwoScore;
    private String winnerTeam;
    private String winnerPlayer;

    private LocalDateTime createdAt;
    private int roundNumber;
    private long tournamentId;

    // Helper setters used by service layer (map entity name fields to DTO fields)
    public void setPlayerOneName(String name) { this.playerOne = name; }
    public void setPlayerTwoName(String name) { this.playerTwo = name; }
    public void setTeamOneName(String name)   { this.teamOne = name; }
    public void setTeamTwoName(String name)   { this.teamTwo = name; }
}



