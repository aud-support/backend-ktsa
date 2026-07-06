package com.ktsa.foosball.dto;

import java.time.LocalDateTime;


import com.ktsa.foosball.model.Teams;
import com.ktsa.foosball.model.Users;
import lombok.Data;
@Data
public class MatchResponseDto {


    private Long id;
    private String stage;
    private String scheduledAt;
    private String status;
    private String playerOneName;
    private String playerTwoName;
    private String teamOneName;
    private String teamTwoName;
    private String winnerTeam;
    private String winnerPlayer;
    private LocalDateTime createdAt;
    private int roundNumber;
    private long tournamentId;


}



