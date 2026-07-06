package com.ktsa.foosball.controller;

import com.ktsa.foosball.dto.ApiResponse;
import com.ktsa.foosball.dto.ChangePasswordDTO;
import com.ktsa.foosball.dto.MyprofileUpdateDTO;
import com.ktsa.foosball.service.UserService;
import com.ktsa.foosball.dto.UserRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ---------------------------------------------------------
    // CREATE USER
    // ---------------------------------------------------------
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createUser(@Valid @RequestBody UserRequestDTO dto) {
        return ResponseEntity.ok(
                ApiResponse.success(200, "User created successfully", userService.createUser(dto))
        );
    }

    // ---------------------------------------------------------
    // FETCH ALL USER
    // ---------------------------------------------------------
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllUser() {
        return ResponseEntity.ok(
                ApiResponse.success(200, "Users fetched successfully", userService.getAllUsers())
        );
    }

    // ---------------------------------------------------------
    // FETCH USER byID
    // ---------------------------------------------------------
    @GetMapping("{id}")
    public ResponseEntity<ApiResponse<?>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(200, "User fetched successfully", userService.getUserById(id))
        );
    }

    // ---------------------------------------------------------
    // UPDATE USER BY ID
    // ---------------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody MyprofileUpdateDTO dto
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(200, "User updated successfully", userService.updateUser(id, dto))
        );
    }

    @PutMapping("/{id}/change-password")
    public ResponseEntity<ApiResponse<?>> updateUserPassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordDTO dto
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(200, "Password updated successfully", userService.updateUserPassword(id, dto))
        );
    }
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<?>> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(
                ApiResponse.success(200, "User fetched successfully", userService.getUserByEmail(email))
        );
    }


    @PostMapping("/{id}/avatar")
    public ResponseEntity<ApiResponse<?>> uploadAvatar(
            @PathVariable Long id,
            @RequestParam("avatar") MultipartFile avatar) {

        System.out.println("UPLOAD API HIT");

        String imageUrl = userService.uploadAvatar(id, avatar);

        return ResponseEntity.ok(
                ApiResponse.success(
                        200,
                        "Avatar uploaded successfully",
                        Map.of("profilePictureUrl", imageUrl)
                )
        );
    }
    @GetMapping("/validate-email")
    public ResponseEntity<ApiResponse<?>> validatePlayerEmail(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(
                ApiResponse.success(200, exists ? "Valid player email" : "Email not found",
                        java.util.Map.of("valid", exists))
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<?>> searchUsers(@RequestParam String q) {
        return ResponseEntity.ok(
                ApiResponse.success(200, "Users fetched successfully",
                        userService.searchByName(q)));
    }


}
