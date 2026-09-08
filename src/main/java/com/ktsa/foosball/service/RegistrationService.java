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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
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
    private static final String CAT_MENS_SINGLE = "Men Single";
    private static final String CAT_UNDER_16 = "Under 16";
    private static final String CAT_ABOVE_16 = "Above 16";
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
        return n != null && (
            n.equals("open single") ||
            n.equals("women single") ||
            n.equals("men single") ||
            n.equals("under 16") ||
            n.equals("above 16")
        );
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
        if ("women single".equals(n)) {
            if (player.getGender() == null) {
                throw new BadRequestException(
                        "Women's Singles is open to female players only. Your account does not have a gender set. "
                        + "Please update your profile with the correct gender before registering."
                );
            }
            if (player.getGender() != Gender.FEMALE) {
                throw new BadRequestException(
                        "Women's Singles is open to female players only. "
                        + "Your account is registered as " + player.getGender().name() + ". "
                        + "Please register for a suitable category such as Men's Singles or Open Singles."
                );
            }
        }
        if ("men single".equals(n)) {
            if (player.getGender() == null) {
                throw new BadRequestException(
                        "Men's Singles is open to male players only. Your account does not have a gender set. "
                        + "Please update your profile with the correct gender before registering."
                );
            }
            if (player.getGender() != Gender.MALE) {
                throw new BadRequestException(
                        "Men's Singles is open to male players only. "
                        + "Your account is registered as " + player.getGender().name() + ". "
                        + "Please register for a suitable category such as Women's Singles or Open Singles."
                );
            }
        }
        // Open Single / Under 16 / Above 16: no gender restriction
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
            if (g1 == null || g2 == null) {
                String whoIsMissing = (g1 == null && g2 == null)
                        ? "Both players have"
                        : (g1 == null ? "\"" + player1.getName() + "\" has" : "\"" + player2.getName() + "\" has");
                throw new BadRequestException(
                        "Mixed Doubles requires one male and one female player. "
                        + whoIsMissing + " no gender set on their profile. "
                        + "Both players must update their profiles before registering."
                );
            }
            boolean oneMaleOneFemale =
                    (g1 == Gender.MALE && g2 == Gender.FEMALE) ||
                            (g1 == Gender.FEMALE && g2 == Gender.MALE);
            if (!oneMaleOneFemale) {
                throw new BadRequestException(
                        "Mixed Doubles requires one male and one female player. "
                        + "\"" + player1.getName() + "\" is " + g1.name() + " and "
                        + "\"" + player2.getName() + "\" is " + g2.name() + ". "
                        + "Please choose a different partner or register for Open Doubles instead."
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
                .orElseThrow(() -> new NotFoundException(
                        "Tournament not found. The tournament you are trying to register for does not exist or may have been removed."));

        Users player = userRepository.findByEmail(dto.getPlayerOneEmail())
                .orElseThrow(() -> new NotFoundException(
                        "No player account found for the email \"" + dto.getPlayerOneEmail() + "\". "
                        + "Please make sure you are using the email address linked to your KTSA account."));

        if (dto.getCategory() == null || dto.getCategory().isBlank()) {
            throw new BadRequestException("Please select a category before submitting your registration.");
        }

        if (isDoubleCategory(dto.getCategory())) {
            throw new BadRequestException(
                    "\"" + dto.getCategory() + "\" is a doubles category and cannot be registered here. "
                    + "Please use the doubles registration option and provide your partner's details."
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
                    "You are already registered for \"" + dto.getCategory() + "\" in this tournament. "
                    + "Each player can only register once per category."
            );
        }

        // Capacity check
        if (tournament.getMaxParticipants() != null) {
            long registered = registrationRepository.countByTournamentId(tournamentId);
            if (registered >= tournament.getMaxParticipants()) {
                throw new BadRequestException(
                        "This tournament has reached its maximum capacity of " + tournament.getMaxParticipants()
                        + " participants and is no longer accepting new registrations."
                );
            }
        }

        Registration registration = new Registration();
        registration.setPlayer(player);
        registration.setTournamentId(tournamentId);
        registration.setCategory(dto.getCategory());
        registration.setStatus("REGISTERED");

        registrationRepository.save(registration);
        return "You have been successfully registered for \"" + dto.getCategory() + "\".";
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

        Registration reg = existing.get(0);
        Teams existingTeam = reg.getTeam();

        Users partner = existingTeam.getPlayerOne().getId().equals(player.getId())
                ? existingTeam.getPlayerTwo()
                : existingTeam.getPlayerOne();

        throw new BadRequestException(
                "\"" + player.getName() + "\" is already registered in the doubles team \""
                        + existingTeam.getTeamName() + "\" (partnered with \"" + partner.getName()
                        + "\") for this tournament. A player can only be part of one doubles team per tournament. "
                        + "Please remove the existing doubles registration first if you wish to change teams."
        );
    }

    private void validatePlayerAlreadyRegisteredInDoubles(Long tournamentId, Users player) {
        validateNotAlreadyInAnotherDoublesTeam(tournamentId, player, -1L);
    }

    public String registerTeam(RegistrationRequestDto dto, Long tournamentId) {
        tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new NotFoundException(
                        "Tournament not found. The tournament you are trying to register for does not exist or may have been removed."));

        Users player1 = userRepository.findByEmail(dto.getPlayerOneEmail())
                .orElseThrow(() -> new NotFoundException(
                        "No player account found for the email \"" + dto.getPlayerOneEmail() + "\". "
                        + "Please make sure you are using the email address linked to your KTSA account."));

        if (dto.getPlayerTwoEmail() == null || dto.getPlayerTwoEmail().isBlank()) {
            throw new BadRequestException(
                    "Your partner's email address is required to complete doubles registration. "
                    + "Please enter your partner's registered KTSA email and try again.");
        }

        if (dto.getPlayerOneEmail().equalsIgnoreCase(dto.getPlayerTwoEmail())) {
            throw new BadRequestException(
                    "You cannot register with yourself as a partner. "
                    + "Please provide your actual partner's email address.");
        }

        Users player2 = userRepository.findByEmail(dto.getPlayerTwoEmail())
                .orElseThrow(() -> new NotFoundException(
                        "No player account found for the partner email \"" + dto.getPlayerTwoEmail() + "\". "
                        + "Your partner must have a registered KTSA account before you can register together."));

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
                    "Your team \"" + team.getTeamName() + "\" is already registered for \"" + existingCat
                    + "\" in this tournament. A team can only enter one doubles category per tournament."
            );
        }

        Registration registration = new Registration();
        registration.setTeam(team);
        registration.setTournamentId(tournamentId);
        registration.setCategory(dto.getCategory() != null ? dto.getCategory() : CAT_OPEN_DOUBLE);
        registration.setStatus("REGISTERED");

        registrationRepository.save(registration);
        return "Your team has been successfully registered for \"" + registration.getCategory() + "\".";
    }

    public String registerNeedPartner(RegistrationRequestDto dto, Long tournamentId) {
        tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new NotFoundException(
                        "Tournament not found. The tournament you are trying to register for does not exist or may have been removed."));

        Users player = userRepository.findByEmail(dto.getPlayerOneEmail())
                .orElseThrow(() -> new NotFoundException(
                        "No player account found for the email \"" + dto.getPlayerOneEmail() + "\". "
                        + "Please make sure you are using the email address linked to your KTSA account."));

        if (dto.getPartnerPreference() == null || dto.getPartnerPreference().isBlank()) {
            throw new BadRequestException(
                    "Please select your preferred playing role (e.g. Defender, Attacker, or All-rounder) "
                    + "so we can match you with a suitable partner.");
        }

        Registration registration = new Registration();
        registration.setPlayer(player);
        registration.setTournamentId(tournamentId);
        registration.setCategory(dto.getCategory() != null ? dto.getCategory() : CAT_OPEN_DOUBLE);
        registration.setPartnerPreference(dto.getPartnerPreference());
        registration.setStatus("NEEDS_PARTNER");

        registrationRepository.save(registration);
        return "You have been added to the partner matching pool for \"" + registration.getCategory()
                + "\". We will notify you once a suitable partner is found.";
    }

    public String registerWithExistingTeam(RegistrationRequestDto dto, Long tournamentId) {
        tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new NotFoundException(
                        "Tournament not found. The tournament you are trying to register for does not exist or may have been removed."));

        if (dto.getExistingTeamId() == null) {
            throw new BadRequestException(
                    "No team was selected. Please choose an existing team from your saved teams to proceed.");
        }

        Teams team = teamService.getTeamById(dto.getExistingTeamId());

        Users player1 = team.getPlayerOne();
        Users player2 = team.getPlayerTwo();

        // Exclude the team itself so it doesn't block re-registration for a different category
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
                    "Your team \"" + team.getTeamName() + "\" is already registered for \"" + existingCat
                    + "\" in this tournament. A team can only enter one doubles category per tournament."
            );
        }

        Registration registration = new Registration();
        registration.setTeam(team);
        registration.setTournamentId(tournamentId);
        registration.setCategory(dto.getCategory() != null ? dto.getCategory() : CAT_OPEN_DOUBLE);
        registration.setStatus("REGISTERED");

        registrationRepository.save(registration);
        return "Your team \"" + team.getTeamName() + "\" has been successfully registered for \""
                + registration.getCategory() + "\".";
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
                .orElseThrow(() -> new NotFoundException(
                        "Tournament not found. The tournament you are trying to register for does not exist or may have been removed."));

        if (dto.getCategories() == null || dto.getCategories().isEmpty()) {
            throw new BadRequestException("Please select at least one category to register for.");
        }

        // Resolve main player once
        Users playerOne = userRepository.findByEmail(dto.getPlayerOneEmail())
                .orElseThrow(() -> new NotFoundException(
                        "No player account found for the email \"" + dto.getPlayerOneEmail() + "\". "
                        + "Please make sure you are using the email address linked to your KTSA account."));

        // Resolve partner once (if any doubles category uses WITH_PARTNER)
        Users playerTwo = null;
        boolean needsPartner = dto.getCategories().stream()
                .anyMatch(c -> "WITH_PARTNER".equalsIgnoreCase(c.getDoublesMode()));
        if (needsPartner) {
            if (dto.getPlayerTwoEmail() == null || dto.getPlayerTwoEmail().isBlank()) {
                throw new BadRequestException(
                        "Your partner's email address is required for doubles registration. "
                        + "Please enter your partner's registered KTSA email and try again.");
            }
            if (dto.getPlayerOneEmail().equalsIgnoreCase(dto.getPlayerTwoEmail())) {
                throw new BadRequestException(
                        "You cannot register with yourself as a partner. "
                        + "Please provide your actual partner's email address.");
            }
            playerTwo = userRepository.findByEmail(dto.getPlayerTwoEmail())
                    .orElseThrow(() -> new NotFoundException(
                            "No player account found for the partner email \"" + dto.getPlayerTwoEmail() + "\". "
                            + "Your partner must have a registered KTSA account before you can register together."));
        }

        // Resolve existing team once (if any doubles category uses EXISTING_TEAM)
        Teams existingTeam = null;
        boolean usesExistingTeam = dto.getCategories().stream()
                .anyMatch(c -> "EXISTING_TEAM".equalsIgnoreCase(c.getDoublesMode()));
        if (usesExistingTeam) {
            if (dto.getExistingTeamId() == null) {
                throw new BadRequestException(
                        "No team was selected. Please choose an existing team from your saved teams to proceed.");
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
                                        "Your team is already registered for \"" + existingCat
                                        + "\" in this tournament. A team can only enter one doubles category per tournament.");
                            }
                        }
                    } else if ("EXISTING_TEAM".equalsIgnoreCase(mode)) {
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
                                    "Your team \"" + existingTeam.getTeamName() + "\" is already registered for \""
                                    + existingCat + "\" in this tournament. A team can only enter one doubles category per tournament.");
                        }
                    } else if ("NEED_PARTNER".equalsIgnoreCase(mode)) {
                        if (dto.getPartnerPreference() == null || dto.getPartnerPreference().isBlank()) {
                            throw new BadRequestException(
                                    "Please select your preferred playing role (e.g. Defender, Attacker, or All-rounder) "
                                    + "so we can match you with a suitable partner.");
                        }
                    } else {
                        throw new BadRequestException(
                                "\"" + cat + "\" is a doubles category but no doubles mode was specified. "
                                + "Please choose one of: Register with a Partner, Use an Existing Team, or Find a Partner.");
                    }
                } else {
                    // Singles
                    validateSingleGender(playerOne, cat);
                    boolean dupSingle = !registrationRepository
                            .findAllByTournamentIdAndPlayerAndCategory(tournamentId, playerOne, cat)
                            .isEmpty();
                    if (dupSingle) {
                        throw new BadRequestException(
                                "You are already registered for \"" + cat + "\" in this tournament. "
                                + "Each player can only register once per category.");
                    }
                }

                // Capacity check (applies to all)
                if (tournament.getMaxParticipants() != null) {
                    long registered = registrationRepository.countByTournamentId(tournamentId);
                    if (registered >= tournament.getMaxParticipants()) {
                        throw new BadRequestException(
                                "This tournament has reached its maximum capacity of " + tournament.getMaxParticipants()
                                + " participants and is no longer accepting new registrations.");
                    }
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
                    reg.setUtrNumber(dto.getUtrNumber());
                    registrationRepository.save(reg);

                } else if ("EXISTING_TEAM".equalsIgnoreCase(mode)) {
                    Registration reg = new Registration();
                    reg.setTeam(existingTeam);
                    reg.setTournamentId(tournamentId);
                    reg.setCategory(cat);
                    reg.setStatus("REGISTERED");
                    reg.setUtrNumber(dto.getUtrNumber());
                    registrationRepository.save(reg);

                } else if ("NEED_PARTNER".equalsIgnoreCase(mode)) {
                    Registration reg = new Registration();
                    reg.setPlayer(playerOne);
                    reg.setTournamentId(tournamentId);
                    reg.setCategory(cat);
                    reg.setPartnerPreference(dto.getPartnerPreference());
                    reg.setStatus("NEEDS_PARTNER");
                    reg.setUtrNumber(dto.getUtrNumber());
                    registrationRepository.save(reg);
                }
            } else {
                Registration reg = new Registration();
                reg.setPlayer(playerOne);
                reg.setTournamentId(tournamentId);
                reg.setCategory(cat);
                reg.setStatus("REGISTERED");
                reg.setUtrNumber(dto.getUtrNumber());
                registrationRepository.save(reg);
            }
        }

        return "Successfully registered for all selected categories.";
    }

    // ---------------------------------------------------------------
    // Export registrations as Excel (.xlsx) for a tournament
    // ---------------------------------------------------------------

    /**
     * Validates that registration is closed for the tournament, then builds and
     * returns an Excel workbook byte array.
     *
     * One sheet per enabled category — each sheet has the same columns:
     * #, Player Username, Player Name, Team Name, Partner Username, Partner Name,
     * Status, UTR Number, Registration Date
     */
    public byte[] exportRegistrationsAsExcel(Long tournamentId) {
        Tournaments tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new NotFoundException("Tournament not found"));

        if (tournament.getRegistrationClosed() == null || !tournament.getRegistrationClosed()) {
            throw new BadRequestException(
                    "Registration for \"" + tournament.getTournamentName() + "\" is still open. "
                    + "Please close registration before generating the export."
            );
        }

        List<Registration> all = registrationRepository.findAllByTournamentId(tournamentId);

        // ── Build ordered map of category → registrations (no duplicates) ──
        java.util.LinkedHashMap<String, List<Registration>> byCategory = new java.util.LinkedHashMap<>();

        // Preferred display order
        java.util.List<String> preferredOrder = java.util.Arrays.asList(
                "Open Singles", "Open Single",
                "Women's Singles", "Women Single",
                "Men's Singles", "Men Single",
                "Under 16", "Above 16",
                "Open Doubles", "Open Double",
                "Mixed Doubles", "Mixed Double"
        );

        // Group all registrations by their category string (case-insensitive dedup via preferredOrder)
        java.util.Map<String, List<Registration>> rawGroups = all.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getCategory() != null ? r.getCategory() : "Uncategorised",
                        java.util.LinkedHashMap::new,
                        Collectors.toList()
                ));

        // First: insert in preferred order (skip categories not in data)
        for (String preferred : preferredOrder) {
            for (java.util.Map.Entry<String, List<Registration>> e : rawGroups.entrySet()) {
                if (preferred.equalsIgnoreCase(e.getKey()) && !byCategory.containsKey(e.getKey())) {
                    byCategory.put(e.getKey(), e.getValue());
                }
            }
        }

        // Then: append any remaining categories not covered by preferred order
        for (java.util.Map.Entry<String, List<Registration>> e : rawGroups.entrySet()) {
            byCategory.putIfAbsent(e.getKey(), e.getValue());
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            // ── Shared styles ──
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            String[] headers = {
                "#", "Player UserName", "Player Name",
                "Challonge TeamName", "Partner Username", "Partner Name",
                "Status", "UTR Number", "Registration Date"
            };

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            // ── One sheet per category ──
            for (java.util.Map.Entry<String, List<Registration>> entry : byCategory.entrySet()) {
                String categoryName = entry.getKey();
                List<Registration> regs = entry.getValue();

                // Excel sheet names max 31 chars, no special chars
                String sheetName = categoryName.length() > 31
                        ? categoryName.substring(0, 31)
                        : categoryName;

                Sheet sheet = workbook.createSheet(sheetName);

                // Header row
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Data rows
                int rowNum = 1;
                for (Registration reg : regs) {
                    Row row = sheet.createRow(rowNum++);

                    row.createCell(0).setCellValue(rowNum - 1);

                    Teams team = reg.getTeam();
                    Users player = reg.getPlayer();

                    if (team != null) {
                        Users p1 = team.getPlayerOne();
                        Users p2 = team.getPlayerTwo();

                        row.createCell(1).setCellValue(p1 != null ? nvl(p1.getUserName()) : "");
                        row.createCell(2).setCellValue(p1 != null ? nvl(p1.getName()) : "");

                        String exportName = (team.getChallongeTeamName() != null && !team.getChallongeTeamName().isBlank())
                                ? team.getChallongeTeamName()
                                : team.getTeamName();
                        row.createCell(3).setCellValue(nvl(exportName));

                        row.createCell(4).setCellValue(p2 != null ? nvl(p2.getUserName()) : "");
                        row.createCell(5).setCellValue(p2 != null ? nvl(p2.getName()) : "");
                    } else {
                        row.createCell(1).setCellValue(player != null ? nvl(player.getUserName()) : "");
                        row.createCell(2).setCellValue(player != null ? nvl(player.getName()) : "");
                        row.createCell(3).setCellValue("");
                        row.createCell(4).setCellValue("");
                        row.createCell(5).setCellValue("");
                    }

                    row.createCell(6).setCellValue(nvl(reg.getStatus()));
                    row.createCell(7).setCellValue(nvl(reg.getUtrNumber()));
                    row.createCell(8).setCellValue(
                            reg.getRegisteredAt() != null ? reg.getRegisteredAt().format(formatter) : "");
                }

                // Auto-size all columns
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            // If no registrations at all, add a placeholder sheet
            if (byCategory.isEmpty()) {
                workbook.createSheet("No Registrations");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel export", e);
        }
    }

    /** Null-safe string helper. */
    private String nvl(String value) {
        return value != null ? value : "";
    }
}
