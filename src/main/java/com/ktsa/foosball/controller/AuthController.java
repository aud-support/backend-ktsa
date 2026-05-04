package com.ktsa.foosball.controller;

import com.ktsa.foosball.dto.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/auth")
public class AuthController {
    // ADMIN CREDENTIALS
    private static final String ADMIN_EMAIL = "admin@gmail.com";
    private static final String ADMIN_PASSWORD = "admin123";

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO request) {

        String identifier = request.getIdentifier();
        String password = request.getPassword();

        if (identifier.equalsIgnoreCase(ADMIN_EMAIL) && password.equals(ADMIN_PASSWORD)) {
            return ResponseEntity.ok(
                    Map.of("message", "Login successful", "identifier", identifier)
            );
        }

        return ResponseEntity.status(401).body(
                Map.of("message", "Login unsuccessful", "error", "Invalid credentials")
        );
    }
}
