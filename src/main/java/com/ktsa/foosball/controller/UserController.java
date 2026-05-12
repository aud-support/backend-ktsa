package com.ktsa.foosball.controller;

import com.ktsa.foosball.dto.ApiResponse;
import com.ktsa.foosball.service.UserService;
import com.ktsa.foosball.dto.UserRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                ApiResponse.success(200, "Users fetched successfully", userService.getUserById(id))
        );
    }

}
