package com.ktsa.foosball.mapper;

import com.ktsa.foosball.dto.RankingResponseDTO;
import com.ktsa.foosball.model.Ranking;
import org.springframework.stereotype.Component;

@Component
public class RankingMapper {

    public RankingResponseDTO toDTO(Ranking ranking) {
        boolean hasUser = ranking.getUser() != null;
        return RankingResponseDTO.builder()
                .id(ranking.getId())
                .points(ranking.getPoints())
                .wins(ranking.getWins())
                .losses(ranking.getLosses())
                .matches(ranking.getWins() + ranking.getLosses())
                .userName(hasUser ? ranking.getUser().getName() : "Unknown")
                .email(hasUser ? ranking.getUser().getEmail() : null)
                .gender(hasUser ? ranking.getUser().getGender() : null)
                .build();
    }
}
