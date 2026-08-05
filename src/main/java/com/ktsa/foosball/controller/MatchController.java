package com.ktsa.foosball.controller;

import com.ktsa.foosball.dto.ApiResponse;
import com.ktsa.foosball.dto.MatchRequestDto;
import com.ktsa.foosball.dto.MatchUpdateDto;
import com.ktsa.foosball.service.ChallongeService;
import com.ktsa.foosball.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final ChallongeService challongeService;



    @PostMapping("/{tournamentId}")
    public ResponseEntity<ApiResponse<?>> createMatch(
            @PathVariable Long tournamentId,
            @RequestBody MatchRequestDto request) {

        return ResponseEntity.ok(
                ApiResponse.success(200, "Match created successfully",matchService.createMatch(tournamentId, request)));
    }

@GetMapping("/{tournamentId}")
    public ResponseEntity<ApiResponse<?>> getAllMatches(@PathVariable Long tournamentId) {

        return ResponseEntity.ok(
                ApiResponse.success(200, "Matches fetched successfully", matchService.getAllMatches(tournamentId)));
    }

    @PutMapping("/{matchId}")
    public ResponseEntity<ApiResponse<?>> updateMatch(
            @PathVariable Long matchId,
            @RequestBody MatchUpdateDto request) {

        return ResponseEntity.ok(
                ApiResponse.success(200, "Match updated successfully", matchService.updateMatch(matchId, request)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Challonge sync
    // POST /api/matches/{tournamentId}/sync-challonge
    //
    // Fetches participants + matches from Challonge and creates / updates local
    // Match records. The tournament must have challongeUrl set.
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/{tournamentId}/sync-challonge")
    public ResponseEntity<ApiResponse<?>> syncFromChallonge(
            @PathVariable Long tournamentId) {

        return ResponseEntity.ok(
                ApiResponse.success(200, "Challonge sync completed",
                        challongeService.syncMatches(tournamentId)));
    }

}