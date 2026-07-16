package com.ktsa.foosball.dto;

import com.ktsa.foosball.model.Gender;
import com.ktsa.foosball.model.Role;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserResponseDTO {
    private Long id;
    private String name;
    private String userName;
    private String email;
    private Role role;
    private Gender gender;
    private String profilePictureUrl;
    private LocalDate dateOfBirth;
    private String city;
    private String state;
    private Long phoneNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
