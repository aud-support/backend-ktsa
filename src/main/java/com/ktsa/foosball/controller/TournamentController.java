package com.ktsa.foosball.controller;

import com.ktsa.foosball.dto.ApiResponse;
import com.ktsa.foosball.dto.TournamentRequestDTO;
import com.ktsa.foosball.service.TournamentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tournament")
@CrossOrigin
@AllArgsConstructor
public class TournamentController {

    private TournamentService tournamentService;

    // ---------------------------------------------------------
    // CREATE Tournament
    // ---------------------------------------------------------
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createTournament(@Valid @RequestBody TournamentRequestDTO dto) {
        return  tournamentService.createTournament(dto);
    }

    // ---------------------------------------------------------
    // FETCH ALL TOURNAMENT
    // ---------------------------------------------------------
//    @GetMapping
//    public ResponseEntity<ApiResponse<?>> getAllTournaments() {
//        return ResponseEntity.ok(
//                ApiResponse.success(200, "Tournaments fetched successfully", tournamentService.getAllTournaments())
//        );
//    }
//
//    // ---------------------------------------------------------
//    // FETCH TOURNAMENT BY ID
//    // ---------------------------------------------------------
//    @GetMapping("{tournamentId}")
//    public ResponseEntity<ApiResponse<?>> getTournamentById(@PathVariable Long tournamentId) {
//        return ResponseEntity.ok(
//                ApiResponse.success(200, "Tournament fetched successfully", tournamentService.getTournamentById(tournamentId))
//        );
//    }
}
