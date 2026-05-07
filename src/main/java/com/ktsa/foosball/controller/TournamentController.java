package com.ktsa.foosball.controller;

import com.ktsa.foosball.dto.TournamentRequestDTO;
import com.ktsa.foosball.service.TournamentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    public ResponseEntity<?> createUser(@Valid @RequestBody TournamentRequestDTO dto) {
        try {
            return ResponseEntity.ok(tournamentService.createTournament(dto));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    // ---------------------------------------------------------
    // FETCH ALL TOURNAMENT
    // ---------------------------------------------------------
    @GetMapping
    public ResponseEntity<?> getAllTournaments(){
        try {
            return ResponseEntity.ok(tournamentService.getAllTournaments());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Unable to fetch Tournament list"
            ));
        }
    }
}
