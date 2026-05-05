package com.ktsa.foosball.mapper;

import com.ktsa.foosball.model.Role;
import com.ktsa.foosball.model.UserStatus;
import com.ktsa.foosball.model.Users;
import com.ktsa.foosball.dto.UserRequestDTO;
import com.ktsa.foosball.dto.UserResponseDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {

    public Users toEntity(UserRequestDTO dto) {
        Users user = new Users();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setPassword((dto.getPassword()));
        user.setDateOfBirth(dto.getDateOfBirth());
        user.setRole(Role.PLAYER);
        user.setCity(dto.getCity());
        user.setState(dto.getState());
        user.setStatus(UserStatus.ACTIVE);
        user.setGender(dto.getGender());
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    public UserResponseDTO toDTO(Users user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        return dto;
    }
}
