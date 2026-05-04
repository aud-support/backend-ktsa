package com.ktsa.foosball.dto;

import com.ktsa.foosball.model.Gender;
import com.ktsa.foosball.model.Role;
import com.ktsa.foosball.model.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserRequestDTO {

    private String password;

    @NotBlank
    private String firstName;

    private String lastName;

    @Email
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@(gmail\\.com)$",
            message = "Email must end with @gmail.com"
    )
    private String email;

    private Role role;

    private Gender gender;
}
