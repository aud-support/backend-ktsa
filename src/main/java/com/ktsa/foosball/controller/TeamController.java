package com.ktsa.foosball.controller;


import com.ktsa.foosball.dto.ApiResponse;
import com.ktsa.foosball.dto.TeamRequestDto;
import com.ktsa.foosball.dto.UserRequestDTO;
import com.ktsa.foosball.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/team")
@RequiredArgsConstructor
public class TeamController {


    private final TeamService teamService;

    // ---------------------------------------------------------
    // CREATE Team
    // ---------------------------------------------------------
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createTeam(@Valid @RequestBody TeamRequestDto teamRequestDTO) {
        return ResponseEntity.ok(
                ApiResponse.success(200, "Teams fetched successfully", teamService.createTeam(teamRequestDTO)));

    }

    // ---------------------------------------------------------
    // FETCH ALL Teams
    // ---------------------------------------------------------
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllTeams() {
        return ResponseEntity.ok(
                ApiResponse.success(200, "Teams fetched successfully", teamService.getAllTeams())
        );
    }

    // ---------------------------------------------------------
    // SEARCH Teams by name
    // ---------------------------------------------------------
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<?>> searchTeams(@RequestParam String q) {
        return ResponseEntity.ok(
                ApiResponse.success(200, "Teams fetched successfully", teamService.searchTeams(q))
        );
    }

    // ---------------------------------------------------------
    // FIND Team by two player emails (order-independent)
    // ---------------------------------------------------------
    @GetMapping("/by-players")
    public ResponseEntity<ApiResponse<?>> getTeamByPlayerEmails(
            @RequestParam String p1,
            @RequestParam String p2) {
        return ResponseEntity.ok(
                ApiResponse.success(200, "Team lookup successful", teamService.getTeamByPlayerEmails(p1, p2))
        );
    }

    // ---------------------------------------------------------
    // GET Teams by User ID
    // GET /api/team/user/{userId}
    // ---------------------------------------------------------
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<?>> getTeamsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(
                ApiResponse.success(200, "User teams fetched successfully", teamService.getTeamsByUser(userId))
        );
    }

}
