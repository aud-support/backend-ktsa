package com.ktsa.foosball.mapper;

import com.ktsa.foosball.dto.RankingResponseDTO;
import com.ktsa.foosball.model.Ranking;
import org.springframework.stereotype.Component;

@Component
public class RankingMapper {

    public RankingResponseDTO toDTO(Ranking ranking) {
        return RankingResponseDTO.builder()
                .id(ranking.getId())
                .points(ranking.getPoints())
                .wins(ranking.getWins())
                .losses(ranking.getLosses())
                .userName(ranking.getUser().getName())
                .email(ranking.getUser().getEmail())
                .gender(ranking.getUser().getGender())
                .build();
    }
}
