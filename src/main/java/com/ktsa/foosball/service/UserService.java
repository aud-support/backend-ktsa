package com.ktsa.foosball.service;

import com.ktsa.foosball.dto.UserResponseDTO;
import com.ktsa.foosball.mapper.UserMapper;
import com.ktsa.foosball.model.Users;
import com.ktsa.foosball.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import  com.ktsa.foosball.dto.UserRequestDTO;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponseDTO createUser(UserRequestDTO dto) {
        Users saved = userRepository.save(userMapper.toEntity(dto));
        return userMapper.toDTO(saved);
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }
}
