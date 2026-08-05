package com.ktsa.foosball.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MatchRequestDto {

    private String stage;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime scheduledAt;

    private String status;
    private Long playerOne;
    private Long playerTwo;
    private Long teamOne;
    private Long teamTwo;
    private Integer roundNumber;
}
