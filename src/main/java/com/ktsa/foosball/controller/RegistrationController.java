package com.ktsa.foosball.controller;

import com.ktsa.foosball.dto.ApiResponse;
import com.ktsa.foosball.dto.RegistrationRequestDto;
import com.ktsa.foosball.service.RegistrationService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/registration")
@CrossOrigin
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/single/{tournamentId}")
    public ResponseEntity<ApiResponse<?>> registerPlayer(
            @RequestBody RegistrationRequestDto registrationRequestDto,
            @PathVariable Long tournamentId) {
        return ResponseEntity.ok(
                ApiResponse.success(200, registrationService.registerPlayer(registrationRequestDto, tournamentId), null)
        );
    }


    @PostMapping("/double/{tournamentId}")
    public ResponseEntity<ApiResponse<?>> registerTeam(
            @RequestBody RegistrationRequestDto registrationRequestDto,
            @PathVariable Long tournamentId) {
        return ResponseEntity.ok(
                ApiResponse.success(200, registrationService.registerTeam(registrationRequestDto, tournamentId), null));

    }


//    @PostMapping("/double/find-partner/{tournamentId}")
//    public ResponseEntity<ApiResponse<?>> registerTeam(){
//
//    }

}
