package com.ktsa.foosball.service;

import com.ktsa.foosball.dto.UserResponseDTO;
import com.ktsa.foosball.mapper.UserMapper;
import com.ktsa.foosball.model.Users;
import com.ktsa.foosball.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import  com.ktsa.foosball.dto.UserRequestDTO;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EmailService emailService;

    public UserResponseDTO createUser(UserRequestDTO dto) {
        Users saved = userRepository.save(userMapper.toEntity(dto));

        //send a welcome mail to new user
        emailService.sendEmail(
                saved.getEmail(),
                "Welcome "+saved.getFirstName() +" " +saved.getLastName(),
                "Welcome to KTSA " + saved.getFirstName() + ", discover foosball with us."
        );

        return userMapper.toDTO(saved);
    }
}
