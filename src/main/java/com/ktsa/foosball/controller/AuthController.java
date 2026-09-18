package com.ktsa.foosball.controller;

import com.ktsa.foosball.dto.ApiResponse;
import com.ktsa.foosball.dto.UserDTO;
import com.ktsa.foosball.exception.AccountInactiveException;
import com.ktsa.foosball.exception.InvalidCredentialsException;
import com.ktsa.foosball.exception.ResourceNotFoundException;
import com.ktsa.foosball.model.UserStatus;
import com.ktsa.foosball.model.Users;
import com.ktsa.foosball.repository.UserRepository;
import com.ktsa.foosball.security.JwtUtil;
import com.ktsa.foosball.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody UserDTO request) {

        String identifier = request.getIdentifier();
        String password = request.getPassword();

        // ADMIN LOGIN
        if (identifier.equalsIgnoreCase(adminEmail) && password.equals(adminPassword)) {
            String adminToken = jwtUtil.generateToken(adminEmail, com.ktsa.foosball.model.Role.ADMIN, 0);
            return ResponseEntity.ok(
                    ApiResponse.success(200, "Login successful", Map.of(
                            "token", adminToken,
                            "role", "ADMIN"
                    ))
            );
        }

        // PLAYER LOGIN
        Users user = userRepository.findByEmail(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountInactiveException("Account is inactive or banned");
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtUtil.generateToken(identifier, user.getRole(),
                user.getTokenVersion());

        return ResponseEntity.ok(
                ApiResponse.success(200, "Login successful", Map.of(
                        "token", token,
                        "role", user.getRole().name(),
                        "email", user.getEmail(),
                        "name", user.getName(),
                        "id",user.getId()
                ))
        );
    }

    /**
     * POST /api/auth/reset-password
     * Simple password reset — no email/token required.
     * Verifies the email exists and updates the password directly.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(
            @RequestBody Map<String, String> body) {

        String email       = body.get("email");
        String newPassword = body.get("newPassword");

        if (email == null || email.isBlank() || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Email and new password are required.", null));
        }
        if (newPassword.length() < 8) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Password must be at least 8 characters.", null));
        }

        Users user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with that email address."));

        user.setPassword(passwordEncoder.encode(newPassword));
        // Invalidate all existing sessions
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);

        return ResponseEntity.ok(
                ApiResponse.success(200, "Password updated successfully.", null));
    }
}