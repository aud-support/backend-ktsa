package com.ktsa.foosball.service;

import com.ktsa.foosball.dto.MatchRequestDto;
import com.ktsa.foosball.dto.MatchResponseDto;
import com.ktsa.foosball.dto.MatchUpdateDto;
import com.ktsa.foosball.dto.UserMatchResponseDto;
import com.ktsa.foosball.exception.BadRequestException;
import com.ktsa.foosball.model.Matches;
import com.ktsa.foosball.model.Teams;
import com.ktsa.foosball.model.Tournaments;
import com.ktsa.foosball.model.Users;
import com.ktsa.foosball.repository.MatchRepository;
import com.ktsa.foosball.repository.RegistrationRepository;
import com.ktsa.foosball.repository.TeamsRepository;
import com.ktsa.foosball.repository.TournamentRepository;
import com.ktsa.foosball.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class MatchService {


    private final TournamentRepository tournamentRepository;
    private final ModelMapper modelMapper;
    private final MatchRepository matchRepository;
    private final TeamsRepository teamsRepository;
    private final UserRepository userRepository;
    private final RegistrationRepository registrationRepository;



    public MatchResponseDto createMatch(Long tournamentId, MatchRequestDto request) {

        Tournaments tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));

        Matches match = new Matches();
        match.setTournament(tournament);

        /*
         * SINGLES MATCH
         */
        if (request.getPlayerOne() != null && request.getPlayerTwo() != null) {

            if (request.getPlayerOne().equals(request.getPlayerTwo())) {
                throw new BadRequestException("Both slots refer to the same player. Please select two different players.");
            }

            // Validate registrations first so we can surface player names in error messages
            boolean p1Registered = registrationRepository
                    .existsByTournamentIdAndPlayerId(tournamentId, request.getPlayerOne());
            if (!p1Registered) {
                throw new BadRequestException("Player One (ID: " + request.getPlayerOne() + ") is not registered in this tournament.");
            }

            boolean p2Registered = registrationRepository
                    .existsByTournamentIdAndPlayerId(tournamentId, request.getPlayerTwo());
            if (!p2Registered) {
                throw new BadRequestException("Player Two (ID: " + request.getPlayerTwo() + ") is not registered in this tournament.");
            }

            Users playerOne = userRepository.findById(request.getPlayerOne())
                    .orElseThrow(() -> new RuntimeException("Player One not found"));

            Users playerTwo = userRepository.findById(request.getPlayerTwo())
                    .orElseThrow(() -> new RuntimeException("Player Two not found"));

            boolean exists =
                    matchRepository.existsByTournamentIdAndPlayerOneIdAndPlayerTwoId(
                            tournamentId,
                            request.getPlayerOne(),
                            request.getPlayerTwo())
                            ||
                            matchRepository.existsByTournamentIdAndPlayerOneIdAndPlayerTwoId(
                                    tournamentId,
                                    request.getPlayerTwo(),
                                    request.getPlayerOne());

            if (exists) {
                throw new BadRequestException(
                        "A match between " + playerOne.getName() + " and " + playerTwo.getName()
                        + " already exists in this tournament."
                );
            }

            match.setPlayerOne(playerOne);
            match.setPlayerTwo(playerTwo);
        }

        /*
         * TEAM MATCH
         */
        else if (request.getTeamOne() != null && request.getTeamTwo() != null) {

            if (request.getTeamOne().equals(request.getTeamTwo())) {
                throw new BadRequestException("Both slots refer to the same team. Please select two different teams.");
            }

            Teams teamOne = teamsRepository.findById(request.getTeamOne())
                    .orElseThrow(() -> new RuntimeException("Team One not found"));

            Teams teamTwo = teamsRepository.findById(request.getTeamTwo())
                    .orElseThrow(() -> new RuntimeException("Team Two not found"));

            boolean exists =
                    matchRepository.existsByTournamentIdAndTeamOneTeamIdAndTeamTwoTeamId(
                            tournamentId,
                            request.getTeamOne(),
                            request.getTeamTwo())
                            ||
                            matchRepository.existsByTournamentIdAndTeamOneTeamIdAndTeamTwoTeamId(
                                    tournamentId,
                                    request.getTeamTwo(),
                                    request.getTeamOne());

            if (exists) {
                throw new BadRequestException(
                        "A match between " + teamOne.getTeamName() + " and " + teamTwo.getTeamName()
                        + " already exists in this tournament."
                );
            }

            match.setTeamOne(teamOne);
            match.setTeamTwo(teamTwo);
        }

        else {
            throw new BadRequestException("Either players or teams must be selected.");
        }

        match.setStage(request.getStage());
        match.setStatus(normalizeStatus(request.getStatus()));
        match.setScheduledAt(request.getScheduledAt());
        match.setRoundNumber(request.getRoundNumber());
        if (request.getCategory() != null) {
            match.setCategory(normalizeCategory(request.getCategory()));
        }

        Matches saved = matchRepository.save(match);

        return toResponseDto(saved);
    }

    public List<MatchResponseDto> getAllMatches(Long tournamentId) {
        return getAllMatches(tournamentId, null);
    }

    public List<MatchResponseDto> getAllMatches(Long tournamentId, String category) {

        tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));

        List<Matches> matches = (category != null && !category.isBlank())
                ? matchRepository.findByTournamentIdAndCategoryIgnoreCase(tournamentId, normalizeCategory(category))
                : matchRepository.findByTournamentId(tournamentId);

        return matches.stream().map(this::toResponseDto).toList();
    }

    public MatchResponseDto updateMatch(Long matchId, MatchUpdateDto request) {

        Matches match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found: " + matchId));

        if (request.getStatus() != null) {
            match.setStatus(normalizeStatus(request.getStatus()));
        }
        if (request.getTeamOneScore() != null) {
            match.setTeamOneScore(request.getTeamOneScore());
        }
        if (request.getTeamTwoScore() != null) {
            match.setTeamTwoScore(request.getTeamTwoScore());
        }

        // clearWinner takes priority — wipes out any existing winner
        if (Boolean.TRUE.equals(request.getClearWinner())) {
            match.setWinnerTeam(null);
            match.setWinnerPlayer(null);
        } else {
            if (request.getWinnerTeam() != null) {
                Teams winner = teamsRepository.findById(request.getWinnerTeam())
                        .orElseThrow(() -> new RuntimeException("Winner team not found"));
                match.setWinnerTeam(winner);
            }
            if (request.getWinnerPlayer() != null) {
                Users winner = userRepository.findById(request.getWinnerPlayer())
                        .orElseThrow(() -> new RuntimeException("Winner player not found"));
                match.setWinnerPlayer(winner);
            }
        }

        return toResponseDto(matchRepository.save(match));
    }

    // ── helper ───────────────────────────────────────────────────────────────

    /**
     * Returns all matches for a user shaped for the profile "My Matches" view.
     * Resolves opponent name, result (win/loss), score, and upcoming/past status.
     */
    public List<UserMatchResponseDto> getMatchesForUser(Long userId) {
        return matchRepository.findAllMatchesForUser(userId)
                .stream()
                .map(m -> toUserMatchDto(m, userId))
                .toList();
    }

    private UserMatchResponseDto toUserMatchDto(Matches m, Long userId) {
        UserMatchResponseDto dto = new UserMatchResponseDto();
        dto.setId(m.getId());
        dto.setScheduledAt(m.getScheduledAt());
        dto.setCategory(m.getCategory());

        if (m.getTournament() != null) {
            dto.setTournamentId(m.getTournament().getId());
            dto.setTournamentName(m.getTournament().getTournamentName());
            dto.setLocation(m.getTournament().getVenue());
        }

        // "past" if COMPLETED, otherwise "upcoming"
        String rawStatus = m.getStatus() != null ? m.getStatus().toUpperCase() : "";
        dto.setStatus(rawStatus.equals("COMPLETED") || rawStatus.equals("DONE") ? "past" : "upcoming");

        boolean isSingles = m.getPlayerOne() != null || m.getPlayerTwo() != null;

        if (isSingles) {
            boolean isPlayerOne = m.getPlayerOne() != null && userId.equals(m.getPlayerOne().getId());
            dto.setOpponent(isPlayerOne
                    ? (m.getPlayerTwo() != null ? m.getPlayerTwo().getName() : "TBD")
                    : (m.getPlayerOne() != null ? m.getPlayerOne().getName() : "TBD"));

            if ("past".equals(dto.getStatus())) {
                Long myScore    = isPlayerOne ? m.getTeamOneScore() : m.getTeamTwoScore();
                Long theirScore = isPlayerOne ? m.getTeamTwoScore() : m.getTeamOneScore();
                if (myScore != null && theirScore != null) dto.setScore(myScore + "-" + theirScore);
                if (m.getWinnerPlayer() != null)
                    dto.setResult(userId.equals(m.getWinnerPlayer().getId()) ? "win" : "loss");
            }
        } else {
            // Doubles
            boolean isTeamOne = m.getTeamOne() != null && isUserInTeam(userId, m.getTeamOne());
            Teams myTeam  = isTeamOne ? m.getTeamOne() : m.getTeamTwo();
            Teams oppTeam = isTeamOne ? m.getTeamTwo() : m.getTeamOne();
            dto.setOpponent(oppTeam != null ? oppTeam.getTeamName() : "TBD");

            if ("past".equals(dto.getStatus())) {
                Long myScore    = isTeamOne ? m.getTeamOneScore() : m.getTeamTwoScore();
                Long theirScore = isTeamOne ? m.getTeamTwoScore() : m.getTeamOneScore();
                if (myScore != null && theirScore != null) dto.setScore(myScore + "-" + theirScore);
                if (m.getWinnerTeam() != null && myTeam != null)
                    dto.setResult(myTeam.getTeamId().equals(m.getWinnerTeam().getTeamId()) ? "win" : "loss");
            }
        }
        return dto;
    }

    private boolean isUserInTeam(Long userId, Teams team) {
        return (team.getPlayerOne() != null && userId.equals(team.getPlayerOne().getId()))
            || (team.getPlayerTwo() != null && userId.equals(team.getPlayerTwo().getId()));
    }

    private MatchResponseDto toResponseDto(Matches match) {
        MatchResponseDto dto = modelMapper.map(match, MatchResponseDto.class);
        dto.setTournamentId(match.getTournament().getId());

        if (match.getPlayerOne() != null) {
            dto.setPlayerOneName(match.getPlayerOne().getName());
        }
        if (match.getPlayerTwo() != null) {
            dto.setPlayerTwoName(match.getPlayerTwo().getName());
        }
        if (match.getTeamOne() != null) {
            dto.setTeamOneName(match.getTeamOne().getTeamName());
            dto.setTeamOneChallongeName(match.getTeamOne().getChallongeTeamName());
        }
        if (match.getTeamTwo() != null) {
            dto.setTeamTwoName(match.getTeamTwo().getTeamName());
            dto.setTeamTwoChallongeName(match.getTeamTwo().getChallongeTeamName());
        }
        if (match.getWinnerTeam() != null) {
            dto.setWinnerTeam(match.getWinnerTeam().getTeamName());
        }
        if (match.getWinnerPlayer() != null) {
            dto.setWinnerPlayer(match.getWinnerPlayer().getName());
        }

        dto.setTeamOneScore(match.getTeamOneScore());
        dto.setTeamTwoScore(match.getTeamTwoScore());
        dto.setCategory(match.getCategory());

        // Tournament name for "My Matches" display
        if (match.getTournament() != null) {
            dto.setTournamentName(match.getTournament().getTournamentName());
            dto.setVenue(match.getTournament().getVenue());
        }

        return dto;
    }

    /**
     * Re-normalises the `category` field on every match in the database so that
     * ranking queries can match them against the canonical enum strings.
     * Returns the number of rows actually updated.
     */
    public int normalizeAllCategories() {
        List<com.ktsa.foosball.model.Matches> all = matchRepository.findAll();
        int count = 0;
        for (com.ktsa.foosball.model.Matches m : all) {
            if (m.getCategory() == null) continue;
            String normalized = normalizeCategory(m.getCategory());
            if (!normalized.equals(m.getCategory())) {
                m.setCategory(normalized);
                matchRepository.save(m);
                count++;
            }
        }
        return count;
    }

    /**
     * Normalises match status so values like "complete", "DONE", "done"
     * are stored consistently as "COMPLETED".
     */
    private String normalizeStatus(String status) {
        if (status == null) return null;
        return switch (status.trim().toUpperCase()) {
            case "COMPLETE", "COMPLETED", "DONE", "FINISHED" -> "COMPLETED";
            case "IN_PROGRESS", "INPROGRESS", "OPEN", "STARTED" -> "IN_PROGRESS";
            default -> status.trim().toUpperCase();
        };
    }

    /**
     * Normalises match category to one of the canonical ranking keys:
     * MENS_SINGLES, WOMENS_SINGLES, OPEN_DOUBLES, MIXED_DOUBLES.
     * Falls back to the uppercased raw value if no alias matches.
     */
    private String normalizeCategory(String category) {
        if (category == null) return null;
        // strip apostrophes/quotes, replace spaces and dashes with underscore, uppercase
        String key = category.trim()
                .toUpperCase()
                .replace("'", "")
                .replace("'", "")   // right single quotation mark U+2019
                .replace(" ", "_")
                .replace("-", "_");
        return switch (key) {
            case "MENS_SINGLES",   "MEN_SINGLES",   "MENS_SINGLE",
                 "OPEN_SINGLES",   "MALE_SINGLES"              -> "MENS_SINGLES";
            case "WOMENS_SINGLES", "WOMEN_SINGLES", "WOMENS_SINGLE",
                 "FEMALE_SINGLES"                              -> "WOMENS_SINGLES";
            case "OPEN_DOUBLES",   "MENS_DOUBLES",  "MEN_DOUBLES",
                 "MALE_DOUBLES"                                -> "OPEN_DOUBLES";
            case "MIXED_DOUBLES",  "MIXED"                    -> "MIXED_DOUBLES";
            default -> key;
        };
    }
}
