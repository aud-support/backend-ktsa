package com.ktsa.foosball.dto;

import com.ktsa.foosball.model.Gender;
import com.ktsa.foosball.model.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserRequestDTO {

    private String password;

    @NotBlank
    private String name;

    private Long phoneNumber;

    private LocalDate dateOfBirth;

    private String city;

    private String state;

    @Email
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.com$",
            message = "Email must be a valid .com email address"
    )
    private String email;

    private Role role;

    private Gender gender;
}
