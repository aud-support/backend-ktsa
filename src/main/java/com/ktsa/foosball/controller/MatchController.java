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
    public ResponseEntity<ApiResponse<?>> getAllMatches(
            @PathVariable Long tournamentId,
            @RequestParam(required = false) String category) {

        return ResponseEntity.ok(
                ApiResponse.success(200, "Matches fetched successfully",
                        matchService.getAllMatches(tournamentId, category)));
    }

    /**
     * GET /api/matches/user/{userId}
     * Returns all matches for the given user (singles + doubles).
     * Used by the "My Matches" modal on the player profile.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<?>> getMatchesForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(
                ApiResponse.success(200, "Matches fetched successfully",
                        matchService.getMatchesForUser(userId)));
    }

    @PutMapping("/{matchId}")
    public ResponseEntity<ApiResponse<?>> updateMatch(
            @PathVariable Long matchId,
            @RequestBody MatchUpdateDto request) {

        return ResponseEntity.ok(
                ApiResponse.success(200, "Match updated successfully", matchService.updateMatch(matchId, request)));
    }

    @PostMapping("/{tournamentId}/sync-challonge")
    public ResponseEntity<ApiResponse<?>> syncFromChallonge(
            @PathVariable Long tournamentId) {

        return ResponseEntity.ok(
                ApiResponse.success(200, "Challonge sync completed",
                        challongeService.syncMatches(tournamentId)));
    }

    /**
     * Sync a single category using its own Challonge URL.
     * POST /api/matches/{tournamentId}/sync-challonge/category
     * Body: { "challongeUrl": "ktsa_open_singles", "category": "Open Singles" }
     */
    @PostMapping("/{tournamentId}/sync-challonge/category")
    public ResponseEntity<ApiResponse<?>> syncCategoryFromChallonge(
            @PathVariable Long tournamentId,
            @RequestBody java.util.Map<String, String> body) {

        String challongeUrl = body.get("challongeUrl");
        String category     = body.get("category");

        if (challongeUrl == null || challongeUrl.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "challongeUrl is required", null));
        }

        return ResponseEntity.ok(
                ApiResponse.success(200, "Challonge sync completed",
                        challongeService.syncMatchesWithUrl(tournamentId, challongeUrl, category)));
    }

}