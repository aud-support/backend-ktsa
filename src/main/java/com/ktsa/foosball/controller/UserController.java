package com.ktsa.foosball.controller;

import com.ktsa.foosball.service.UserService;
import com.ktsa.foosball.dto.UserRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ---------------------------------------------------------
    // CREATE USER
    // ---------------------------------------------------------
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserRequestDTO dto) {
        try {
            return ResponseEntity.ok(userService.createUser(dto));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
}
