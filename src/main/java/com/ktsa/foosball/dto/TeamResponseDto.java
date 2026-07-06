package com.ktsa.foosball.dto;


import lombok.Data;

@Data
public class TeamResponseDto {

    private Long teamId;

    private String teamName;

    private String emailPlayerOne;

    private String emailPlayerTwo;

}
