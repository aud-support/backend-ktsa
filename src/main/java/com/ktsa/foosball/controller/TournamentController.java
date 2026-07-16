package com.ktsa.foosball.controller;

import com.ktsa.foosball.dto.ApiResponse;
import com.ktsa.foosball.dto.TournamentRequestDTO;
import com.ktsa.foosball.service.RegistrationService;
import com.ktsa.foosball.service.TournamentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/tournament")
@AllArgsConstructor
public class TournamentController {

    private TournamentService tournamentService;
    private RegistrationService registrationService;

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
    public ResponseEntity<ApiResponse<?>> getAllTournaments(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "9") int size) {

//        Pageable pageable = PageRequest.of(
//                page,
//                size,
//                Sort.by("startDate").descending()
//        );
        return ResponseEntity.ok(
                ApiResponse.success(200, "All Tournaments fetched successfully", tournamentService.getAllTournaments(page, size))
        );
    }

    // ---------------------------------------------------------
    // FILTER TOURNAMENTS by month and/or year
    // GET /api/tournament/filter?month=7&year=2026
    // ---------------------------------------------------------
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<?>> getTournamentsByFilter(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("startDate").descending()
        );
        return ResponseEntity.ok(
                ApiResponse.success(200, "Tournaments fetched successfully",
                        tournamentService.getTournamentsByFilter(month, year,pageable))
        );
    }

    // ---------------------------------------------------------
    // GET AVAILABLE YEARS (dynamic — from actual tournament data)
    // GET /api/tournament/available-years
    // ---------------------------------------------------------
    @GetMapping("/available-years")
    public ResponseEntity<ApiResponse<?>> getAvailableYears() {
        return ResponseEntity.ok(
                ApiResponse.success(200, "Available years fetched successfully",
                        tournamentService.getAvailableYears())
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
    // CLOSE REGISTRATION manually
    // PATCH /api/tournament/{tournamentId}/close-registration
    // ---------------------------------------------------------
    @PatchMapping("/{tournamentId}/close-registration")
    public ResponseEntity<ApiResponse<?>> closeRegistration(
            @PathVariable Long tournamentId,
            @RequestParam(defaultValue = "true") boolean closed) {
        return ResponseEntity.ok(
                ApiResponse.success(200,
                        closed ? "Registration closed successfully" : "Registration reopened successfully",
                        tournamentService.setRegistrationClosed(tournamentId, closed))
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


    @GetMapping("/{tournamentId}/players/search")
    public ResponseEntity<ApiResponse<?>> searchRegisteredPlayers(
            @PathVariable Long tournamentId,
            @RequestParam(name = "q", defaultValue = "") String query) {
        return ResponseEntity.ok(
                ApiResponse.success(200, "Players fetched successfully",
                        registrationService.searchRegisteredPlayers(tournamentId, query))
        );
    }


}
