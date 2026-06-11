package com.ktsa.foosball.dto;

import com.ktsa.foosball.model.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RankingResponseDTO {
    private Long id;
    private int points;
    private int wins;
    private int losses;
    private String userName;
    private String email;
    private Gender gender;
}

