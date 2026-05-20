package com.ktsa.foosball.controller;

import com.ktsa.foosball.dto.ApiResponse;
import com.ktsa.foosball.dto.MyprofileUpdateDTO;
import com.ktsa.foosball.service.UserService;
import com.ktsa.foosball.dto.UserRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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


    @GetMapping("/{email}")
    public ResponseEntity<ApiResponse<?>> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(
                ApiResponse.success(200, "User fetched successfully", userService.getUserByEmail(email))
        );
    }

}
