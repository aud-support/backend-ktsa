package com.ktsa.foosball.dto;

import com.ktsa.foosball.model.Teams;
import lombok.Data;
@Data
public class MatchRequestDto {


    private String stage;
    private String scheduledAt;
    private String status;
    private Long playerOne;
    private Long playerTwo;
    private Long teamOne;
    private Long teamTwo;

    private Integer roundNumber;
//    private Long tournamentId;
}
