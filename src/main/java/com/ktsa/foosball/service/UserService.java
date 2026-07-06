package com.ktsa.foosball.service;

import com.ktsa.foosball.dto.ChangePasswordDTO;
import com.ktsa.foosball.dto.MyprofileUpdateDTO;
import com.ktsa.foosball.dto.UserResponseDTO;
import com.ktsa.foosball.exception.ResourceNotFoundException;
import com.ktsa.foosball.mapper.UserMapper;
import com.ktsa.foosball.model.Ranking;
import com.ktsa.foosball.model.Users;
import com.ktsa.foosball.repository.RankingRepository;
import com.ktsa.foosball.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import  com.ktsa.foosball.dto.UserRequestDTO;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RankingRepository rankingRepository;
    private final S3Service s3Service;

    @Value("${aws.s3.buckets.players}")
    private String usersBucket;
    final private BCryptPasswordEncoder encoder= new BCryptPasswordEncoder(10);


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


    public String uploadAvatar(Long id, MultipartFile avatar) {
        System.out.println("USERS BUCKET = " + usersBucket);

        Users user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found: " + id));

        String avatarUrl =
                s3Service.uploadFile(avatar, usersBucket, "avatars");

        user.setProfilePictureUrl(avatarUrl);

        userRepository.save(user);

        return avatarUrl;
    }

    public String updateUserPassword(Long id, ChangePasswordDTO dto) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));


        if (!encoder.matches(
                dto.getCurrentPassword(),
                user.getPassword())) {

            throw new RuntimeException("Current password is incorrect");
        }

        if (dto.getCurrentPassword().equals(dto.getNewPassword())) {
            throw new RuntimeException(
                    "New password cannot be the same as current password");
        }

        user.setPassword(encoder.encode(dto.getNewPassword()));

        // invalidate all active sessions
        user.setTokenVersion(
                user.getTokenVersion() + 1
        );

userRepository.save(user);

return "password changed successfully";

    }

    public List<Users> searchByName(String query) {
        return userRepository.findByNameContainingIgnoreCase(query);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
