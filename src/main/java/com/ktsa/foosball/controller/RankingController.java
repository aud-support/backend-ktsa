package com.ktsa.foosball.controller;

import com.ktsa.foosball.dto.RankingResponseDTO;
import com.ktsa.foosball.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping
    public ResponseEntity<List<RankingResponseDTO>> getAllRankings() {
        return ResponseEntity.ok(rankingService.getAllRankings());
    }

    /**
     * GET /api/rankings/top
     * Returns top 3 spotlight players for the homepage:
     *   index 0 — #1 Men's Singles
     *   index 1 — #1 Women's Singles
     *   index 2 — #1 Open Doubles
     */
    @GetMapping("/top")
    public ResponseEntity<List<RankingResponseDTO>> getTopSpotlightPlayers() {
        return ResponseEntity.ok(rankingService.getTopSpotlightPlayers());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<RankingResponseDTO> getRankingByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(rankingService.getRankingByUserId(userId));
    }
}
