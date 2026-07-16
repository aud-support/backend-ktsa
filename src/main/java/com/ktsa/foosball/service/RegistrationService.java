package com.ktsa.foosball.service;

import com.ktsa.foosball.dto.*;
import com.ktsa.foosball.exception.BadRequestException;
import com.ktsa.foosball.exception.NotFoundException;
import com.ktsa.foosball.model.Gender;
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
public class RegistrationService {

    private TournamentRepository tournamentRepository;
    private UserRepository userRepository;
    private TeamService teamService;
    private RegistrationRepository registrationRepository;

    // ---------------------------------------------------------------
    // Category constants — must match what the frontend sends
    // ---------------------------------------------------------------
    private static final String CAT_OPEN_SINGLE = "Open Single";
    private static final String CAT_WOMEN_SINGLE = "Women Single";
    private static final String CAT_OPEN_DOUBLE = "Open Double";
    private static final String CAT_MIXED_DOUBLE = "Mixed Double";

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    /**
     * Normalise category string to a canonical form for comparison.
     */
    private String normaliseCategory(String raw) {
        if (raw == null) return null;
        return raw.trim()
                .toLowerCase()
                .replace("'s", "")      // "Women's" → "Women"
                .replace("'", "")       // any stray apostrophes
                .replace("doubles", "double")
                .replace("singles", "single")
                .replaceAll("\\s+", " ") // collapse multiple spaces
                .trim();
    }

    private boolean isSingleCategory(String category) {
        String n = normaliseCategory(category);
        return n != null && (n.equals("open single") || n.equals("women single"));
    }

    private boolean isDoubleCategory(String category) {
        String n = normaliseCategory(category);
        return n != null && (n.equals("open double") || n.equals("mixed double"));
    }

    /**
     * Validate gender constraints for singles categories.
     * Women Single → player must be FEMALE.
     * Open Single  → any gender.
     */
    private void validateSingleGender(Users player, String category) {
        String n = normaliseCategory(category);
        System.out.println("[RegistrationService] validateSingleGender: raw='" + category + "' normalised='" + n + "' gender=" + player.getGender());
        if ("women single".equals(n)) {
            if (player.getGender() == null || player.getGender() != Gender.FEMALE) {
                throw new BadRequestException(
                        "Women's Singles is restricted to female players only. Your account gender is: "
                                + (player.getGender() != null ? player.getGender().name() : "not set") + "."
                );
            }
        }
        // Open Single: no restriction
    }

    /**
     * Validate gender constraints for doubles categories.
     * Mixed Double  → exactly one MALE and one FEMALE.
     * Open Double   → any combination.
     */
    private void validateDoubleGender(Users player1, Users player2, String category) {
        String n = normaliseCategory(category);
        if ("mixed double".equals(n)) {
            Gender g1 = player1.getGender();
            Gender g2 = player2.getGender();
            boolean oneMaleOneFemale =
                    (g1 == Gender.MALE && g2 == Gender.FEMALE) ||
                            (g1 == Gender.FEMALE && g2 == Gender.MALE);
            if (!oneMaleOneFemale) {
                throw new BadRequestException(
                        "Mixed Doubles requires exactly one male and one female player in the pair."
                );
            }
        }
        // Open Double: any combination — no restriction
    }

    // ---------------------------------------------------------------
    // Public service methods
    // ---------------------------------------------------------------

    public String registerPlayer(RegistrationRequestDto dto, Long tournamentId) {

        Tournaments tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new NotFoundException("Tournament not found"));

        Users player = userRepository.findByEmail(dto.getPlayerOneEmail())
                .orElseThrow(() -> new NotFoundException("No account found for email: " + dto.getPlayerOneEmail()));

        if (dto.getCategory() == null || dto.getCategory().isBlank()) {
            throw new BadRequestException("Category is required");
        }

        if (isDoubleCategory(dto.getCategory())) {
            throw new BadRequestException(
                    "Doubles categories must be registered using the partner registration endpoint."
            );
        }

        // Gender validation for singles
        validateSingleGender(player, dto.getCategory());

        // Duplicate check
        boolean alreadyRegistered = !registrationRepository
                .findAllByTournamentIdAndPlayerAndCategory(tournamentId, player, dto.getCategory())
                .isEmpty();
        if (alreadyRegistered) {
            throw new BadRequestException(
                    "You are already registered for the \"" + dto.getCategory() + "\" category in this tournament."
            );
        }

        // Capacity check
        if (tournament.getMaxParticipants() != null &&
                tournament.getPlayers().size() >= tournament.getMaxParticipants()) {
            throw new BadRequestException("This tournament is full and is no longer accepting registrations.");
        }

        Registration registration = new Registration();
        registration.setPlayer(player);
        registration.setTournamentId(tournamentId);
        registration.setCategory(dto.getCategory());
        registration.setStatus("REGISTERED");

        registrationRepository.save(registration);
        return "Player registered successfully";
    }

    /**
     * Checks if a player already has a doubles team registration in this tournament
     * with a DIFFERENT team (excludeTeamId = -1 means no exclusion).
     * <p>
     * Throws BadRequestException with a clear message if violation found.
     */
    private void validateNotAlreadyInAnotherDoublesTeam(Long tournamentId, Users player, Long excludeTeamId) {
        List<Registration> existing = (excludeTeamId != null && excludeTeamId > 0)
                ? registrationRepository.findAllDoublesRegistrationsByPlayerExcludingTeam(
                tournamentId, player.getId(), excludeTeamId)
                : registrationRepository.findAllDoublesRegistrationsByPlayer(
                tournamentId, player.getId());

        if (existing.isEmpty()) return;

        // Take the first registration to build the error message
        Registration reg = existing.get(0);
        Teams existingTeam = reg.getTeam();

        Users partner = existingTeam.getPlayerOne().getId().equals(player.getId())
                ? existingTeam.getPlayerTwo()
                : existingTeam.getPlayerOne();

        throw new BadRequestException(
                "\"" + player.getName() + "\" is already registered as part of the doubles team \""
                        + existingTeam.getTeamName() + "\" (with \"" + partner.getName()
                        + "\") in this tournament. A player can only be in one doubles team per tournament."
        );
    }

    private void validatePlayerAlreadyRegisteredInDoubles(Long tournamentId, Users player) {
        validateNotAlreadyInAnotherDoublesTeam(tournamentId, player, -1L);
    }

    public String registerTeam(RegistrationRequestDto dto, Long tournamentId) {
        tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new NotFoundException("Tournament not found"));

        Users player1 = userRepository.findByEmail(dto.getPlayerOneEmail())
                .orElseThrow(() -> new NotFoundException("No account found for email: " + dto.getPlayerOneEmail()));

        if (dto.getPlayerTwoEmail() == null || dto.getPlayerTwoEmail().isBlank()) {
            throw new BadRequestException("Partner email is required for doubles registration.");
        }

        if (dto.getPlayerOneEmail().equalsIgnoreCase(dto.getPlayerTwoEmail())) {
            throw new BadRequestException("You cannot register with yourself as a partner.");
        }

        Users player2 = userRepository.findByEmail(dto.getPlayerTwoEmail())
                .orElseThrow(() -> new NotFoundException("No account found for partner email: " + dto.getPlayerTwoEmail()));

// Check if either player is already in another doubles team
        validatePlayerAlreadyRegisteredInDoubles(tournamentId, player1);
        validatePlayerAlreadyRegisteredInDoubles(tournamentId, player2);

        // Gender validation for doubles
        validateDoubleGender(player1, player2, dto.getCategory());

        Teams team = teamService.findOrCreateTeam(player1, player2, dto.getTeamName());

        boolean teamAlreadyRegistered = !registrationRepository
                .findAllByTournamentIdAndTeam(tournamentId, team)
                .isEmpty();
        if (teamAlreadyRegistered) {
            String existingCat = registrationRepository.findAllByTournamentIdAndTeam(tournamentId, team)
                    .get(0).getCategory();
            throw new BadRequestException(
                    "Your team is already registered for \"" + existingCat +
                            "\" in this tournament. A team can only enter one doubles category per tournament."
            );
        }

        Registration registration = new Registration();
        registration.setTeam(team);
        registration.setTournamentId(tournamentId);
        registration.setCategory(dto.getCategory() != null ? dto.getCategory() : CAT_OPEN_DOUBLE);
        registration.setStatus("REGISTERED");

        registrationRepository.save(registration);
        return "Team registered successfully for \"" + registration.getCategory() + "\"";
    }

    public String registerNeedPartner(RegistrationRequestDto dto, Long tournamentId) {
        tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new NotFoundException("Tournament not found"));

        Users player = userRepository.findByEmail(dto.getPlayerOneEmail())
                .orElseThrow(() -> new NotFoundException("No account found for email: " + dto.getPlayerOneEmail()));

        if (dto.getPartnerPreference() == null || dto.getPartnerPreference().isBlank()) {
            throw new BadRequestException("Role preference is required when registering as a free agent.");
        }

        Registration registration = new Registration();
        registration.setPlayer(player);
        registration.setTournamentId(tournamentId);
        registration.setCategory(dto.getCategory() != null ? dto.getCategory() : CAT_OPEN_DOUBLE);
        registration.setPartnerPreference(dto.getPartnerPreference());
        registration.setStatus("NEEDS_PARTNER");

        registrationRepository.save(registration);
        return "Registered successfully. You will be matched with a partner.";
    }

    public String registerWithExistingTeam(RegistrationRequestDto dto, Long tournamentId) {
        tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new NotFoundException("Tournament not found"));

        if (dto.getExistingTeamId() == null) {
            throw new BadRequestException("Team ID is required.");
        }

        Teams team = teamService.getTeamById(dto.getExistingTeamId());

        // Gender validation: load both players from the team
        Users player1 = team.getPlayerOne();
        Users player2 = team.getPlayerTwo();

        // Exclude the team itself so it doesn't block re-registration for different category
        validateNotAlreadyInAnotherDoublesTeam(tournamentId, player1, team.getTeamId());
        validateNotAlreadyInAnotherDoublesTeam(tournamentId, player2, team.getTeamId());

        validateDoubleGender(player1, player2, dto.getCategory());

        boolean teamAlreadyRegistered = !registrationRepository
                .findAllByTournamentIdAndTeam(tournamentId, team)
                .isEmpty();
        if (teamAlreadyRegistered) {
            String existingCat = registrationRepository.findAllByTournamentIdAndTeam(tournamentId, team)
                    .get(0).getCategory();
            throw new BadRequestException(
                    "This team is already registered for \"" + existingCat +
                            "\" in this tournament. A team can only enter one doubles category per tournament."
            );
        }

        Registration registration = new Registration();
        registration.setTeam(team);
        registration.setTournamentId(tournamentId);
        registration.setCategory(dto.getCategory() != null ? dto.getCategory() : CAT_OPEN_DOUBLE);
        registration.setStatus("REGISTERED");

        registrationRepository.save(registration);
        return "Team registered successfully for \"" + registration.getCategory() + "\"";
    }

    public List<PlayerSearchDto> searchRegisteredPlayers(Long tournamentId, String query) {
        List<Users> players = registrationRepository.searchPlayersByTournament(tournamentId, query);
        return players.stream()
                .map(user -> new PlayerSearchDto(user.getId(), user.getName(), user.getUserName(), user.getEmail()))
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // Batch registration — validate ALL categories first, then save
    // ---------------------------------------------------------------

    /**
     * Validates every requested category without saving anything.
     * If ALL pass, saves all registrations in one go.
     * If ANY fails, throws BadRequestException with a combined message — nothing is saved.
     */
    @org.springframework.transaction.annotation.Transactional
    public String validateAndRegisterAll(BatchRegistrationRequestDto dto, Long tournamentId) {

        Tournaments tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new NotFoundException("Tournament not found"));

        if (dto.getCategories() == null || dto.getCategories().isEmpty()) {
            throw new BadRequestException("At least one category must be selected.");
        }

        // Resolve main player once
        Users playerOne = userRepository.findByEmail(dto.getPlayerOneEmail())
                .orElseThrow(() -> new NotFoundException("No account found for email: " + dto.getPlayerOneEmail()));

        // Resolve partner once (if any doubles category uses WITH_PARTNER)
        Users playerTwo = null;
        boolean needsPartner = dto.getCategories().stream()
                .anyMatch(c -> "WITH_PARTNER".equalsIgnoreCase(c.getDoublesMode()));
        if (needsPartner) {
            if (dto.getPlayerTwoEmail() == null || dto.getPlayerTwoEmail().isBlank()) {
                throw new BadRequestException("Partner email is required for doubles registration.");
            }
            if (dto.getPlayerOneEmail().equalsIgnoreCase(dto.getPlayerTwoEmail())) {
                throw new BadRequestException("You cannot register with yourself as a partner.");
            }
            playerTwo = userRepository.findByEmail(dto.getPlayerTwoEmail())
                    .orElseThrow(() -> new NotFoundException("No account found for partner email: " + dto.getPlayerTwoEmail()));
        }

        // Resolve existing team once (if any doubles category uses EXISTING_TEAM)
        Teams existingTeam = null;
        boolean usesExistingTeam = dto.getCategories().stream()
                .anyMatch(c -> "EXISTING_TEAM".equalsIgnoreCase(c.getDoublesMode()));
        if (usesExistingTeam) {
            if (dto.getExistingTeamId() == null) {
                throw new BadRequestException("Team ID is required when registering with an existing team.");
            }
            existingTeam = teamService.getTeamById(dto.getExistingTeamId());
        }

        // ── PHASE 1: Validate every category — collect ALL errors, save NOTHING ──
        java.util.List<String> errors = new java.util.ArrayList<>();

        for (BatchRegistrationRequestDto.CategoryEntry entry : dto.getCategories()) {
            String cat = entry.getCategory();
            String mode = entry.getDoublesMode();

            try {
                if (isDoubleCategory(cat)) {
                    if ("WITH_PARTNER".equalsIgnoreCase(mode)) {


                        // New validation
                        validatePlayerAlreadyRegisteredInDoubles(tournamentId, playerOne);
                        validatePlayerAlreadyRegisteredInDoubles(tournamentId, playerTwo);
                        // Gender check for doubles
                        validateDoubleGender(playerOne, playerTwo, cat);
                        // Duplicate team check
                        Teams tentativeTeam = teamService.getTeamByPlayers(playerOne.getId(), playerTwo.getId());
                        if (tentativeTeam != null) {
                            boolean dup = !registrationRepository
                                    .findAllByTournamentIdAndTeam(tournamentId, tentativeTeam)
                                    .isEmpty();
                            if (dup) {
                                String existingCat = registrationRepository
                                        .findAllByTournamentIdAndTeam(tournamentId, tentativeTeam)
                                        .get(0).getCategory();
                                throw new BadRequestException(
                                        "Your team is already registered for \"" + existingCat + "\" in this tournament.");
                            }
                        }
                    } else if ("EXISTING_TEAM".equalsIgnoreCase(mode)) {
                        // Exclude the existing team itself — it's allowed to register again
                        // only if it's for a different doubles category
                        validateNotAlreadyInAnotherDoublesTeam(
                                tournamentId,
                                existingTeam.getPlayerOne(),
                                existingTeam.getTeamId());
                        validateNotAlreadyInAnotherDoublesTeam(
                                tournamentId,
                                existingTeam.getPlayerTwo(),
                                existingTeam.getTeamId());
                        validateDoubleGender(existingTeam.getPlayerOne(), existingTeam.getPlayerTwo(), cat);
                        boolean dupTeam = !registrationRepository
                                .findAllByTournamentIdAndTeam(tournamentId, existingTeam)
                                .isEmpty();
                        if (dupTeam) {
                            String existingCat = registrationRepository
                                    .findAllByTournamentIdAndTeam(tournamentId, existingTeam)
                                    .get(0).getCategory();
                            throw new BadRequestException(
                                    "This team is already registered for \"" + existingCat + "\" in this tournament.");
                        }
                    } else if ("NEED_PARTNER".equalsIgnoreCase(mode)) {
                        if (dto.getPartnerPreference() == null || dto.getPartnerPreference().isBlank()) {
                            throw new BadRequestException("Role preference is required when registering as a free agent.");
                        }
                    } else {
                        throw new BadRequestException("Doubles mode is required for doubles categories.");
                    }
                } else {
                    // Singles
                    validateSingleGender(playerOne, cat);
                    boolean dupSingle = !registrationRepository
                            .findAllByTournamentIdAndPlayerAndCategory(tournamentId, playerOne, cat)
                            .isEmpty();
                    if (dupSingle) {
                        throw new BadRequestException(
                                "You are already registered for \"" + cat + "\" in this tournament.");
                    }
                }

                // Capacity check (applies to all)
                if (tournament.getMaxParticipants() != null &&
                        tournament.getPlayers().size() >= tournament.getMaxParticipants()) {
                    throw new BadRequestException("This tournament is full and is no longer accepting registrations.");
                }

            } catch (BadRequestException | NotFoundException ex) {
                errors.add("[" + cat + "]: " + ex.getMessage());
            }
        }

        // If any category failed validation → return all errors, save nothing
        if (!errors.isEmpty()) {
            throw new BadRequestException(String.join(" | ", errors));
        }

        // ── PHASE 2: All validations passed — save everything ──
        for (BatchRegistrationRequestDto.CategoryEntry entry : dto.getCategories()) {
            String cat = entry.getCategory();
            String mode = entry.getDoublesMode();

            if (isDoubleCategory(cat)) {
                if ("WITH_PARTNER".equalsIgnoreCase(mode)) {
                    Teams team = teamService.findOrCreateTeam(playerOne, playerTwo, dto.getTeamName());
                    Registration reg = new Registration();
                    reg.setTeam(team);
                    reg.setTournamentId(tournamentId);
                    reg.setCategory(cat);
                    reg.setStatus("REGISTERED");
                    registrationRepository.save(reg);

                } else if ("EXISTING_TEAM".equalsIgnoreCase(mode)) {
                    Registration reg = new Registration();
                    reg.setTeam(existingTeam);
                    reg.setTournamentId(tournamentId);
                    reg.setCategory(cat);
                    reg.setStatus("REGISTERED");
                    registrationRepository.save(reg);

                } else if ("NEED_PARTNER".equalsIgnoreCase(mode)) {
                    Registration reg = new Registration();
                    reg.setPlayer(playerOne);
                    reg.setTournamentId(tournamentId);
                    reg.setCategory(cat);
                    reg.setPartnerPreference(dto.getPartnerPreference());
                    reg.setStatus("NEEDS_PARTNER");
                    registrationRepository.save(reg);
                }
            } else {
                Registration reg = new Registration();
                reg.setPlayer(playerOne);
                reg.setTournamentId(tournamentId);
                reg.setCategory(cat);
                reg.setStatus("REGISTERED");
                registrationRepository.save(reg);
            }
        }

        return "Successfully registered for all selected categories.";
    }
}
