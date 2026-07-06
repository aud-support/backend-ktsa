package com.ktsa.foosball.controller;

import com.ktsa.foosball.dto.ApiResponse;
import com.ktsa.foosball.dto.MatchRequestDto;
import com.ktsa.foosball.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;



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
                ApiResponse.success(200, "Match created successfully",matchService.getAllMatches(tournamentId)));
    }



}