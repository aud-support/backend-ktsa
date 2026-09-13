package com.ktsa.foosball.service;

import com.ktsa.foosball.dto.BulkImportResultDto;
import com.ktsa.foosball.dto.ChangePasswordDTO;
import com.ktsa.foosball.dto.MyprofileUpdateDTO;
import com.ktsa.foosball.dto.UserResponseDTO;
import com.ktsa.foosball.exception.ResourceNotFoundException;
import com.ktsa.foosball.mapper.UserMapper;
import com.ktsa.foosball.model.Gender;
import com.ktsa.foosball.model.Ranking;
import com.ktsa.foosball.model.Role;
import com.ktsa.foosball.model.UserStatus;
import com.ktsa.foosball.model.Users;
import com.ktsa.foosball.repository.RankingRepository;
import com.ktsa.foosball.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.ktsa.foosball.dto.UserRequestDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        // Check for duplicate email before attempting to save
        if (dto.getEmail() != null && userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new com.ktsa.foosball.exception.BadRequestException(
                    "A player already exists with this email address: " + dto.getEmail());
        }

        Users savedUser = userRepository.save(userMapper.toEntity(dto));

        // Generate username using the ID
        savedUser.setUserName(userMapper.generateUserName(savedUser.getName(), savedUser.getId()));

        // Save again with the username
        savedUser = userRepository.save(savedUser);

        Ranking ranking = Ranking.builder()
                .points(0)
                .wins(0)
                .losses(0)
                .user(savedUser)
                .build();

        rankingRepository.save(ranking);

        return userMapper.toDTO(savedUser);
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

    /**
     * Bulk import users from an Excel (.xlsx) file.
     *
     * Expected columns (row 1 = header, skipped):
     *   A: name (required)
     *   B: email (required)
     *   C: gender (optional — MALE / FEMALE)
     *
     * Default password: Ktsa@1234
     * Rows with duplicate email or missing name/email are skipped.
     */
    public BulkImportResultDto bulkImportUsers(MultipartFile file) {
        final String DEFAULT_PASSWORD = "Ktsa@1234";

        int totalRows = 0;
        int created = 0;
        List<String> skippedEmails = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // skip header row 0
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String name  = getCellValue(row, 0);
                String email = getCellValue(row, 1);
                String genderStr = getCellValue(row, 2);

                // skip empty rows
                if (name.isBlank() && email.isBlank()) continue;

                totalRows++;

                if (name.isBlank() || email.isBlank()) {
                    skippedEmails.add(email.isBlank() ? "row-" + (i + 1) + "-missing-email" : email);
                    continue;
                }

                // skip duplicate emails
                if (userRepository.existsByEmail(email)) {
                    skippedEmails.add(email);
                    continue;
                }

                // build user
                Users user = new Users();
                user.setName(name.trim());
                user.setEmail(email.trim().toLowerCase());
                user.setPassword(encoder.encode(DEFAULT_PASSWORD));
                user.setRole(Role.PLAYER);
                user.setStatus(UserStatus.ACTIVE);
                user.setCreatedAt(LocalDateTime.now());
                user.setTokenVersion(1);

                // optional gender
                if (!genderStr.isBlank()) {
                    try {
                        user.setGender(Gender.valueOf(genderStr.trim().toUpperCase()));
                    } catch (IllegalArgumentException ignored) {
                        // unrecognised value — leave null
                    }
                }

                Users saved = userRepository.save(user);
                saved.setUserName(userMapper.generateUserName(saved.getName(), saved.getId()));
                userRepository.save(saved);

                // create ranking entry
                Ranking ranking = Ranking.builder()
                        .points(0).wins(0).losses(0).user(saved).build();
                rankingRepository.save(ranking);

                created++;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage(), e);
        }

        return new BulkImportResultDto(totalRows, created, skippedEmails.size(), skippedEmails);
    }

    private String getCellValue(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default      -> "";
        };
    }
}
