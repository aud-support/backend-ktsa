package com.ktsa.foosball.dto;

import lombok.Data;

@Data
public class TeamResponseDto {

    private Long teamId;

    private String teamName;

    /** Always "userName1 & userName2" — used for Challonge sync and Excel export. */
    private String challongeTeamName;

    private String emailPlayerOne;

    private String emailPlayerTwo;

}
