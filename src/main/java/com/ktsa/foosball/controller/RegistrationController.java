package com.ktsa.foosball.controller;

import com.ktsa.foosball.dto.ApiResponse;
import com.ktsa.foosball.dto.BatchRegistrationRequestDto;
import com.ktsa.foosball.dto.RegistrationRequestDto;
import com.ktsa.foosball.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/registration")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    /**
     * Batch registration — validates ALL selected categories first.
     * If ANY category fails validation, NOTHING is saved and all errors are returned.
     * Only when every category passes does the backend save all registrations atomically.
     *
     * POST /api/registration/batch/{tournamentId}
     */
    @PostMapping("/batch/{tournamentId}")
    public ResponseEntity<ApiResponse<?>> registerBatch(
            @RequestBody BatchRegistrationRequestDto dto,
            @PathVariable Long tournamentId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(201, registrationService.validateAndRegisterAll(dto, tournamentId), null)
        );
    }

    // ── Individual endpoints kept for backward compatibility ──

    @PostMapping("/single/{tournamentId}")
    public ResponseEntity<ApiResponse<?>> registerPlayer(
            @RequestBody RegistrationRequestDto registrationRequestDto,
            @PathVariable Long tournamentId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(201, registrationService.registerPlayer(registrationRequestDto, tournamentId), null)
        );
    }

    @PostMapping("/double/{tournamentId}")
    public ResponseEntity<ApiResponse<?>> registerTeam(
            @RequestBody RegistrationRequestDto registrationRequestDto,
            @PathVariable Long tournamentId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(201, registrationService.registerTeam(registrationRequestDto, tournamentId), null));
    }

    @PostMapping("/double/existing-team/{tournamentId}")
    public ResponseEntity<ApiResponse<?>> registerWithExistingTeam(
            @RequestBody RegistrationRequestDto registrationRequestDto,
            @PathVariable Long tournamentId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(201, registrationService.registerWithExistingTeam(registrationRequestDto, tournamentId), null));
    }

    @PostMapping("/double/need-partner/{tournamentId}")
    public ResponseEntity<ApiResponse<?>> registerNeedPartner(
            @RequestBody RegistrationRequestDto registrationRequestDto,
            @PathVariable Long tournamentId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(201, registrationService.registerNeedPartner(registrationRequestDto, tournamentId), null));
    }
}
