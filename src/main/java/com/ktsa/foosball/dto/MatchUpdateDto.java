package com.ktsa.foosball.dto;

import lombok.Data;

@Data
public class MatchUpdateDto {
    private Long teamOneScore;
    private Long teamTwoScore;
    private Long winnerTeam;
    private Long winnerPlayer;
    private String status;
    /** When true, explicitly clears any existing winner from the match. */
    private Boolean clearWinner;
}
