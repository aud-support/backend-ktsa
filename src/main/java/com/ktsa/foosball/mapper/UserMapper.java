package com.ktsa.foosball.mapper;

import com.ktsa.foosball.model.UserStatus;
import com.ktsa.foosball.model.Users;
import com.ktsa.foosball.dto.UserRequestDTO;
import com.ktsa.foosball.dto.UserResponseDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class UserMapper {

    public Users toEntity(UserRequestDTO dto) {
        Users user = new Users();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setUserName(dto.getFirstName());
        user.setEmail(dto.getEmail());
        user.setPassword((dto.getPassword()));
        user.setRole(dto.getRole());
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    public UserResponseDTO toDTO(Users user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        return dto;
    }
}
