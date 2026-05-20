package com.ktsa.foosball.dto;


import com.ktsa.foosball.model.Teams;
import com.ktsa.foosball.model.Users;
import lombok.Data;

@Data
public class RegistrationRequestDto {

    private Long tournamentId;

    private String playerOneEmail;

    private String playerTwoEmail;

    private String partnerPreference; // all rounder, defence, offence

    private String category; // Single, double, mix

}
