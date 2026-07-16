package com.ktsa.foosball.mapper;

import com.ktsa.foosball.dto.UserRequestDTO;
import com.ktsa.foosball.dto.UserResponseDTO;
import com.ktsa.foosball.model.Role;
import com.ktsa.foosball.model.UserStatus;
import com.ktsa.foosball.model.Users;
import com.ktsa.foosball.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Locale;

@Component
public class UserMapper {

    final private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
    private final UserRepository userRepository;

    public UserMapper(UserRepository userRepository) {
        this.userRepository = userRepository;

    }

    /**
     * Generates a unique userName from the player's name + sequential suffix.
     * Format: lowercase(name with spaces replaced by _) + "_" + id-like counter
     * Example: "John Doe" → "john_doe_1", "john_doe_2", ...
     */
    public String generateUserName(String name, Long id) {
        // Sanitize: lowercase, replace spaces with underscore, remove non-alphanumeric except underscore
        String base = name.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-z0-9_]", "");

        if (base.isEmpty()) {
            base = "player";
        }

//        // If the plain base isn't taken, use it directly
//        if (!userRepository.existsByUserName(base)) {
//            return base;
//        }
//
//        // Otherwise append an incrementing number until unique
//        long count = userRepository.countByUserNameStartingWith(base + "_");
//        long suffix = count + 1;
//        String candidate = base + "_" + suffix;
//
//        // Guard against unlikely collision when count gaps exist
//        while (userRepository.existsByUserName(candidate)) {
//            suffix++;
//            candidate = base + "_" + suffix;
//        }

        return  base + "_" + id;
    }

    public Users toEntity(UserRequestDTO dto) {
        Users user = new Users();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setDateOfBirth(dto.getDateOfBirth());
        user.setRole(Role.PLAYER);
        user.setCity(dto.getCity());
        user.setState(dto.getState());
        user.setStatus(UserStatus.ACTIVE);
        user.setGender(dto.getGender());
        user.setCreatedAt(LocalDateTime.now());
        // Auto-generate unique userName
//        user.setUserName(generateUserName(dto.getName(), user.getId()));
        return user;
    }

    public UserResponseDTO toDTO(Users user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setUserName(user.getUserName());
        dto.setRole(user.getRole());
        dto.setName(user.getName());
        dto.setGender(user.getGender());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setCity(user.getCity());
        dto.setState(user.getState());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setProfilePictureUrl(user.getProfilePictureUrl());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
