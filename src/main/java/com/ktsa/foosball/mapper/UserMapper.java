package com.ktsa.foosball.mapper;

import com.ktsa.foosball.model.Role;
import com.ktsa.foosball.model.UserStatus;
import com.ktsa.foosball.model.Users;
import com.ktsa.foosball.dto.UserRequestDTO;
import com.ktsa.foosball.dto.UserResponseDTO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {

    final private BCryptPasswordEncoder encoder= new BCryptPasswordEncoder(10);

    public Users toEntity(UserRequestDTO dto) {
        Users user = new Users();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setPassword(encoder.encode(dto.getPassword()));//bcrypting the password and saving in the entity
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
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setName(user.getName());
        dto.setGender(user.getGender());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setCity(user.getCity());
        dto.setState(user.getState());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
