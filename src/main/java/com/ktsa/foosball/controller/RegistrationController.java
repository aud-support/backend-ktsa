package com.ktsa.foosball.controller;

import com.ktsa.foosball.service.RegistrationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/registration")
@CrossOrigin
@AllArgsConstructor
public class RegistrationController {
    private RegistrationService registrationService;

    @PostMapping("/{playerId}/{tournamentId}")
    public ResponseEntity<?> registerPlayer(
            @PathVariable Long playerId,
            @PathVariable Long tournamentId) {
        try {
            String message = registrationService.registerPlayer(playerId, tournamentId);
            return ResponseEntity.ok(Map.of("message", message));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
