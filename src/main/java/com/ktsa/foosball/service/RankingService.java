package com.ktsa.foosball.service;


import com.ktsa.foosball.dto.RankingResponseDTO;
import com.ktsa.foosball.mapper.RankingMapper;
import com.ktsa.foosball.model.Ranking;
import com.ktsa.foosball.repository.RankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RankingRepository rankingRepository;
    private final RankingMapper rankingMapper;

    public List<RankingResponseDTO> getAllRankings() {
        return rankingRepository.findAll().stream()
                .map(rankingMapper::toDTO)
                .collect(Collectors.toList());
    }

    public RankingResponseDTO getRankingByUserId(Long userId) {
        Ranking ranking = rankingRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Ranking not found for user: " + userId));
        return rankingMapper.toDTO(ranking);
    }
}