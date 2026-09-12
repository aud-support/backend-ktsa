package com.ktsa.foosball.dto;


import com.ktsa.foosball.model.Teams;
import com.ktsa.foosball.model.Users;
import lombok.Data;

@Data
public class RegistrationResponseDto {

    private Long tournamentId;

    private Long teamId;
    private String teamName;

    private Long playerId;
    private String playerName;

    private String status;

    private String registeredAt;

    private String category; // Single, double, mix
}
