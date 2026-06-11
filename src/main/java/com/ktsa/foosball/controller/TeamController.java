package com.ktsa.foosball.controller;


import com.ktsa.foosball.dto.ApiResponse;
import com.ktsa.foosball.dto.TeamRequestDto;
import com.ktsa.foosball.dto.UserRequestDTO;
import com.ktsa.foosball.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/team")
@CrossOrigin
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

}
