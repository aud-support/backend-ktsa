package com.ktsa.foosball.service;

import com.ktsa.foosball.dto.*;
import com.ktsa.foosball.exception.BadRequestException;
import com.ktsa.foosball.exception.NotFoundException;
import com.ktsa.foosball.model.Registration;
import com.ktsa.foosball.model.Teams;
import com.ktsa.foosball.model.Tournaments;
import com.ktsa.foosball.model.Users;
import com.ktsa.foosball.repository.RegistrationRepository;
import com.ktsa.foosball.repository.TournamentRepository;
import com.ktsa.foosball.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
//@RequiredArgsConstructor
public class RegistrationService {

    private TournamentRepository tournamentRepository;
    private UserRepository userRepository;
    private TeamService teamService;
    private RegistrationRepository registrationRepository;

    public String registerPlayer(RegistrationRequestDto registrationRequestDto, Long tournamentId) {

        Tournaments tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new NotFoundException("Tournament not found"));

        Users player = userRepository.findByEmail(registrationRequestDto.getPlayerOneEmail())
                .orElseThrow(() -> new NotFoundException("No account found for email: " + registrationRequestDto.getPlayerOneEmail()));

        if (registrationRequestDto.getCategory() == null || registrationRequestDto.getCategory().isBlank()) {
            throw new BadRequestException("Category is required");
        }

        if (registrationRequestDto.getCategory().toLowerCase().contains("double")) {
            throw new BadRequestException(
                    "Doubles categories must be registered using partner."
            );
        }

        // Check if player is already registered
        Optional<Registration> existing = Optional.ofNullable(registrationRepository
                .findByTournamentIdAndPlayerAndCategory(
                        tournamentId,
                        player,
                        registrationRequestDto.getCategory()
                ));

        if (existing.isPresent()) {
            throw new BadRequestException(
                    "You are already registered for the \"" + registrationRequestDto.getCategory() + "\" category in this tournament"
            );
        }

        // Check max participants
        if (tournament.getMaxParticipants() != null &&
                tournament.getPlayers().size() >= tournament.getMaxParticipants()) {
            throw new BadRequestException("This tournament is full and is no longer accepting registrations");
        }

        // Add player to tournament
//        tournament.getPlayers().add(player);
//
//        // Save tournament
//        tournamentRepository.save(tournament);

        // Create registration entry
        Registration registration = new Registration();
        registration.setPlayer(player);
        registration.setTournamentId(tournamentId);
        registration.setCategory(registrationRequestDto.getCategory());
        registration.setStatus("REGISTERED");
//        registration.setRegisteredAt(String.valueOf(System.currentTimeMillis()));

        registrationRepository.save(registration);

        return "Player registered successfully";
    }

    public String registerTeam(RegistrationRequestDto registrationRequestDto, Long tournamentId) {
        tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new NotFoundException("Tournament not found"));

        Users player1 = userRepository.findByEmail(registrationRequestDto.getPlayerOneEmail())
                .orElseThrow(() -> new NotFoundException("No account found for email: " + registrationRequestDto.getPlayerOneEmail()));

        if (registrationRequestDto.getPlayerTwoEmail() == null || registrationRequestDto.getPlayerTwoEmail().isBlank()) {
            throw new BadRequestException("Partner email is required for doubles registration");
        }

        if (registrationRequestDto.getPlayerOneEmail().equalsIgnoreCase(registrationRequestDto.getPlayerTwoEmail())) {
            throw new BadRequestException("You cannot register with yourself as a partner");
        }

        Users player2 = userRepository.findByEmail(registrationRequestDto.getPlayerTwoEmail())
                .orElseThrow(() -> new NotFoundException("No account found for partner email: " + registrationRequestDto.getPlayerTwoEmail()));

        // Find existing team for this player pair (order-independent) or create one
        Teams team = teamService.findOrCreateTeam(player1, player2, registrationRequestDto.getTeamName());

        // Enforce: a team can only register for one doubles category per tournament
        Registration existing = registrationRepository.findByTournamentIdAndTeam(tournamentId, team);
        if (existing != null) {
            throw new BadRequestException(
                    "Your team is already registered for \"" + existing.getCategory() +
                    "\" in this tournament. A team can only enter one doubles category per tournament."
            );
        }

        Registration registration = new Registration();
        registration.setTeam(team);
        registration.setTournamentId(tournamentId);
        registration.setCategory(registrationRequestDto.getCategory() != null ? registrationRequestDto.getCategory() : "Double");
        registration.setStatus("REGISTERED");

        registrationRepository.save(registration);

        return "Team registered successfully for \"" + registration.getCategory() + "\"";
    }

    public List<PlayerSearchDto> searchRegisteredPlayers(Long tournamentId, String query) {
        List<Users> players = registrationRepository.searchPlayersByTournament(tournamentId, query);
        return players.stream()
                .map(user -> new PlayerSearchDto(user.getId(), user.getName(), user.getEmail()))
                .collect(Collectors.toList());
    }

    /**
     * Free-agent registration: player needs a partner.
     * Saves the player individually with their preferred role.
     * No team is created — they'll be matched later.
     */
    public String registerNeedPartner(RegistrationRequestDto dto, Long tournamentId) {
        tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new NotFoundException("Tournament not found"));

        Users player = userRepository.findByEmail(dto.getPlayerOneEmail())
                .orElseThrow(() -> new NotFoundException("No account found for email: " + dto.getPlayerOneEmail()));

        if (dto.getPartnerPreference() == null || dto.getPartnerPreference().isBlank()) {
            throw new BadRequestException("Role preference is required when registering as a free agent");
        }

        Registration registration = new Registration();
        registration.setPlayer(player);
        registration.setTournamentId(tournamentId);
        registration.setCategory(dto.getCategory() != null ? dto.getCategory() : "Double");
        registration.setPartnerPreference(dto.getPartnerPreference()); // Defender / Attacker / All-rounder
        registration.setStatus("NEEDS_PARTNER");

        registrationRepository.save(registration);

        return "Registered successfully. You will be matched with a partner.";
    }

    public String registerWithExistingTeam(RegistrationRequestDto registrationRequestDto, Long tournamentId) {
        tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new NotFoundException("Tournament not found"));

        if (registrationRequestDto.getExistingTeamId() == null) {
            throw new BadRequestException("Team ID is required");
        }

        Teams team = teamService.getTeamById(registrationRequestDto.getExistingTeamId());

        // Enforce: a team can only register for one doubles category per tournament
        Registration existing = registrationRepository.findByTournamentIdAndTeam(tournamentId, team);
        if (existing != null) {
            throw new BadRequestException(
                    "This team is already registered for \"" + existing.getCategory() +
                    "\" in this tournament. A team can only enter one doubles category per tournament."
            );
        }

        Registration registration = new Registration();
        registration.setTeam(team);
        registration.setTournamentId(tournamentId);
        registration.setCategory(registrationRequestDto.getCategory() != null ? registrationRequestDto.getCategory() : "Double");
        registration.setStatus("REGISTERED");

        registrationRepository.save(registration);

        return "Team registered successfully for \"" + registration.getCategory() + "\"";
    }

//    public ResponseEntity<ApiResponse<?>> registerTeam(RegistrationRequestDto dto, Long tournamentId) {
//
//        Tournaments tournament = tournamentRepository.findById(tournamentId)
//                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + tournamentId));
//
//        Teams team = teamsRepository.findById(dto.getTeamId())
//                .orElseThrow(() -> new RuntimeException("team not found with id: " + dto.getTeamId()));
//
//        // Check if team is already registered
//        if (tournament.getTeams().contains(team)) {
//            throw new RuntimeException("Team is already registered in this tournament");
//        }
//
//        // Check max participants
//        if (tournament.getMaxParticipants() != null &&
//                tournament.getPlayers().size() >= tournament.getMaxParticipants()) {
//            throw new RuntimeException("Tournament is full. Max participants: " + tournament.getMaxParticipants());
//        }
//
//        tournament.getTeams().add(team);
//        tournamentRepository.save(tournament);
//
//        Registration registration= modelMapper.map(dto, Registration.class);
//        registration.setTournamentId(tournamentId);
//
//        Registration registeredPlayerOrTeam = registrationRepository.save(registration);
//        return ResponseEntity.ok(
//                ApiResponse.success(200, "Team registered successfully", modelMapper.map(registeredPlayerOrTeam, RegistrationResponseDto.class))
//        );
//
//    }
}
