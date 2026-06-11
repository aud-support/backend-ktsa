package com.ktsa.foosball.controller;

import com.ktsa.foosball.dto.ApiResponse;
import com.ktsa.foosball.dto.TournamentRequestDTO;
import com.ktsa.foosball.service.TournamentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<ApiResponse<?>> createTournament( @RequestPart("data") @Valid TournamentRequestDTO dto,
                                                            @RequestPart(value = "banner", required = false) MultipartFile banner) {
        return  tournamentService.createTournament(dto, banner);
    }

    // ---------------------------------------------------------
    // FETCH ALL TOURNAMENT
    // ---------------------------------------------------------
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllTournaments() {
        return ResponseEntity.ok(
                ApiResponse.success(200, "All Tournaments fetched successfully", tournamentService.getAllTournaments())
        );
    }


    // ---------------------------------------------------------
    // UPDATE Tournament
    // ---------------------------------------------------------
    @PutMapping("/{tournamentId}")
    public ResponseEntity<ApiResponse<?>> updateTournament(  @PathVariable Long tournamentId,    @RequestPart("data") @Valid TournamentRequestDTO dto,
                                                             @RequestPart(value = "banner", required = false) MultipartFile banner) {
        return ResponseEntity.ok(
                        ApiResponse.success(
                                200,
                                "Tournament updated successfully",

                tournamentService.updateTournament( tournamentId, dto, banner))

        );
    }

      // ---------------------------------------------------------
      // FETCH TOURNAMENT BY ID
      // ---------------------------------------------------------
    @GetMapping("/{tournamentId}")
    public ResponseEntity<ApiResponse<?>> getTournamentById(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(
                ApiResponse.success(200, "Tournament fetched successfully", tournamentService.getTournamentById(tournamentId))
        );
    }


    // ---------------------------------------------------------
    // DELETE Tournament
    // ---------------------------------------------------------
    @DeleteMapping("/{tournamentId}")
    public ResponseEntity<ApiResponse<?>> deleteTournament(  @PathVariable Long tournamentId) {

        tournamentService.deleteTournament(tournamentId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "Tournament deleted  successfully",

                       null)

        );
    }


}
