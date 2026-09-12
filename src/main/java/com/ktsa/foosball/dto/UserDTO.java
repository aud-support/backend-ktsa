package com.ktsa.foosball.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDTO {
    // Can be email OR empId
    @NotBlank
    private String identifier;

    @NotBlank
    private String password;
}
