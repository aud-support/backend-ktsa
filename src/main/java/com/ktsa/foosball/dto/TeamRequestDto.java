package com.ktsa.foosball.dto;

import lombok.Data;
@Data
public class TeamRequestDto {

    private String emailPlayerOne;
    private String emailPlayerTwo;
    private String teamName; // Optional custom name; auto-generated if not provided
}
