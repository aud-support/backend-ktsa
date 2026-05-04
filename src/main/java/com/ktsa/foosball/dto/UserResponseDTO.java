package com.ktsa.foosball.dto;

import com.ktsa.foosball.model.Role;
import lombok.Data;

@Data
public class UserResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
}
