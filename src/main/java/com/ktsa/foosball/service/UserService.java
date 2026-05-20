package com.ktsa.foosball.service;

import com.ktsa.foosball.dto.MyprofileUpdateDTO;
import com.ktsa.foosball.dto.UserResponseDTO;
import com.ktsa.foosball.exception.ResourceNotFoundException;
import com.ktsa.foosball.mapper.UserMapper;
import com.ktsa.foosball.model.Ranking;
import com.ktsa.foosball.model.Users;
import com.ktsa.foosball.repository.RankingRepository;
import com.ktsa.foosball.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import  com.ktsa.foosball.dto.UserRequestDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RankingRepository rankingRepository;

    public UserResponseDTO createUser(UserRequestDTO dto) {
        Users saved = userRepository.save(userMapper.toEntity(dto));

        Ranking ranking = Ranking.builder()
                .points(0)
                .wins(0)
                .losses(0)
                .user(saved)
                .build();

        rankingRepository.save(ranking);

        return userMapper.toDTO(saved);
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<UserResponseDTO> getUserById(Long id) {

        return userRepository.findById(id).map(userMapper::toDTO);
    }

    public UserResponseDTO updateUser(Long id, MyprofileUpdateDTO dto) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        user.setName(dto.getName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setState(dto.getState());
        user.setCity(dto.getCity());
        user.setUpdatedAt(LocalDateTime.now());

        return userMapper.toDTO(userRepository.save(user));
    }

    public Optional<UserResponseDTO> getUserByEmail(String email) {

        return userRepository.findByEmail(email).map(userMapper::toDTO);
    }




}
