package com.ktsa.foosball.controller;

import com.ktsa.foosball.dto.ApiResponse;
import com.ktsa.foosball.service.RegistrationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/registration")
@CrossOrigin
@AllArgsConstructor
public class RegistrationController {
    private RegistrationService registrationService;

    @PostMapping("/{playerId}/{tournamentId}")
    public ResponseEntity<ApiResponse<?>> registerPlayer(
            @PathVariable Long playerId,
            @PathVariable Long tournamentId) {
        return ResponseEntity.ok(
                ApiResponse.success(200, registrationService.registerPlayer(playerId, tournamentId), null)
        );
    }

}
