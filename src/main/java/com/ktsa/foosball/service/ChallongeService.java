package com.ktsa.foosball.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktsa.foosball.dto.challonge.*;
import com.ktsa.foosball.exception.BadRequestException;
import com.ktsa.foosball.exception.ResourceNotFoundException;
import com.ktsa.foosball.model.Matches;
import com.ktsa.foosball.model.Teams;
import com.ktsa.foosball.model.Tournaments;
import com.ktsa.foosball.model.Users;
import com.ktsa.foosball.repository.MatchRepository;
import com.ktsa.foosball.repository.TeamsRepository;
import com.ktsa.foosball.repository.TournamentRepository;
import com.ktsa.foosball.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Fetches bracket data from the Challonge REST API and syncs it into
 * the local Match database.
 *
 * Singles tournament:
 *   Challonge participant name = one userName  →  resolved to a Users record
 *   → match is stored with playerOne / playerTwo
 *
 * Doubles / team tournament:
 *   Challonge participant name = "player1_username & player2_username"
 *   → each half is resolved to a Users record
 *   → looked up (or created) in the Teams table
 *   → match is stored with teamOne / teamTwo
 *
 * Detection: if ANY participant name in a tournament contains " & ",
 * the whole sync is treated as a team tournament.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChallongeService {

    private static final String CHALLONGE_BASE = "https://api.challonge.com/v1/tournaments";
    private static final String TEAM_SEPARATOR = " & ";

    @Value("${challonge.username}")
    private String challongeUsername;

    @Value("${challonge.api-key}")
    private String challongeApiKey;

    private final TournamentRepository tournamentRepository;
    private final MatchRepository       matchRepository;
    private final UserRepository        userRepository;
    private final TeamsRepository       teamsRepository;
    private final ObjectMapper          objectMapper;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC ENTRY POINT
    // ─────────────────────────────────────────────────────────────────────────

    public ChallongeSyncResultDto syncMatches(Long tournamentId) {

        Tournaments tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tournament not found with id: " + tournamentId));

        String challongeUrl = tournament.getChallongeUrl();
        if (challongeUrl == null || challongeUrl.isBlank()) {
            throw new BadRequestException(
                    "Tournament " + tournamentId + " has no challongeUrl configured.");
        }

        // 1. Fetch raw participant names from Challonge
        Map<Long, String> participantNames = fetchParticipantNames(challongeUrl);
        log.info("[Challonge] Fetched {} participants for '{}'", participantNames.size(), challongeUrl);

        // 2. Auto-detect whether this is a team tournament
        boolean isTeamTournament = participantNames.values().stream()
                .anyMatch(name -> name.contains(TEAM_SEPARATOR));
        log.info("[Challonge] Detected mode: {}", isTeamTournament ? "TEAMS" : "SINGLES");

        List<String> unmatchedNames = new ArrayList<>();
        List<ChallongeMatchDto> challongeMatches = fetchMatches(challongeUrl);
        log.info("[Challonge] Fetched {} matches", challongeMatches.size());

        int created = 0;
        int updated = 0;

        if (isTeamTournament) {
            // ── TEAM path ──────────────────────────────────────────────────
            // participantId → Teams entity
            Map<Long, Teams> participantTeams = resolveTeams(participantNames, unmatchedNames);

            for (ChallongeMatchDto cm : challongeMatches) {
                if (cm.getPlayer1Id() == null || cm.getPlayer2Id() == null) continue;

                Teams team1 = participantTeams.get(cm.getPlayer1Id());
                Teams team2 = participantTeams.get(cm.getPlayer2Id());

                if (team1 == null || team2 == null) {
                    log.warn("[Challonge] Skipping match {} — could not resolve teams " +
                                    "(player1_id={}, player2_id={})",
                            cm.getId(), cm.getPlayer1Id(), cm.getPlayer2Id());
                    continue;
                }

                Optional<Matches> existingOpt = matchRepository
                        .findByTournamentIdAndChallongeMatchId(tournamentId, cm.getId());

                if (existingOpt.isPresent()) {
                    applyTeamData(existingOpt.get(), cm, team1, team2, participantTeams);
                    matchRepository.save(existingOpt.get());
                    updated++;
                } else {
                    Optional<Matches> manualMatch = findManualTeamMatch(
                            tournamentId, team1, team2, cm.getRound());

                    Matches match = manualMatch.orElseGet(Matches::new);
                    match.setTournament(tournament);
                    match.setTeamOne(team1);
                    match.setTeamTwo(team2);
                    match.setRoundNumber(cm.getRound());
                    match.setChallongeMatchId(cm.getId());
                    applyTeamData(match, cm, team1, team2, participantTeams);
                    matchRepository.save(match);

                    if (manualMatch.isPresent()) updated++;
                    else created++;
                }
            }

        } else {
            // ── SINGLES path ───────────────────────────────────────────────
            Map<Long, Users> participantUsers = resolveUsers(participantNames, unmatchedNames);

            for (ChallongeMatchDto cm : challongeMatches) {
                if (cm.getPlayer1Id() == null || cm.getPlayer2Id() == null) continue;

                Users player1 = participantUsers.get(cm.getPlayer1Id());
                Users player2 = participantUsers.get(cm.getPlayer2Id());

                if (player1 == null || player2 == null) {
                    log.warn("[Challonge] Skipping match {} — could not resolve players " +
                                    "(player1_id={}, player2_id={})",
                            cm.getId(), cm.getPlayer1Id(), cm.getPlayer2Id());
                    continue;
                }

                Optional<Matches> existingOpt = matchRepository
                        .findByTournamentIdAndChallongeMatchId(tournamentId, cm.getId());

                if (existingOpt.isPresent()) {
                    applyPlayerData(existingOpt.get(), cm, participantUsers);
                    matchRepository.save(existingOpt.get());
                    updated++;
                } else {
                    Optional<Matches> manualMatch = findManualPlayerMatch(
                            tournamentId, player1, player2, cm.getRound());

                    Matches match = manualMatch.orElseGet(Matches::new);
                    match.setTournament(tournament);
                    match.setPlayerOne(player1);
                    match.setPlayerTwo(player2);
                    match.setRoundNumber(cm.getRound());
                    match.setChallongeMatchId(cm.getId());
                    applyPlayerData(match, cm, participantUsers);
                    matchRepository.save(match);

                    if (manualMatch.isPresent()) updated++;
                    else created++;
                }
            }
        }

        return new ChallongeSyncResultDto(
                challongeMatches.size(), created, updated, unmatchedNames);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RESOLUTION — SINGLES
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Resolves each participant name to a local Users entity.
     * Unresolvable names are added to the unmatchedNames list.
     */
    private Map<Long, Users> resolveUsers(Map<Long, String> participantNames,
                                          List<String> unmatchedNames) {
        Map<Long, Users> result = new HashMap<>();
        for (Map.Entry<Long, String> entry : participantNames.entrySet()) {
            String name = entry.getValue();
            Optional<Users> user = userRepository.findByUserName(name);
            if (user.isEmpty()) {
                List<Users> candidates = userRepository.findByNameContainingIgnoreCase(name);
                if (candidates.size() == 1) {
                    user = Optional.of(candidates.get(0));
                } else if (candidates.size() > 1) {
                    user = candidates.stream()
                            .filter(u -> name.equalsIgnoreCase(u.getName()))
                            .findFirst();
                }
            }
            if (user.isPresent()) {
                result.put(entry.getKey(), user.get());
            } else {
                unmatchedNames.add(name);
                log.warn("[Challonge] Could not resolve user for participant name '{}'", name);
            }
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RESOLUTION — TEAMS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Resolves each participant name (format: "userName1 & userName2") to a
     * local Teams entity using challongeTeamName as the primary key.
     *
     * Resolution strategy (in order):
     *   1. Exact match on Teams.challongeTeamName (most reliable — always userName1 & userName2)
     *   2. Resolve both usernames separately → look up team by player pair
     *   3. Fallback to teamName for plain (non-& ) names
     */
    private Map<Long, Teams> resolveTeams(Map<Long, String> participantNames,
                                          List<String> unmatchedNames) {
        Map<Long, Teams> result = new HashMap<>();

        for (Map.Entry<Long, String> entry : participantNames.entrySet()) {
            Long challongeId = entry.getKey();
            String fullName  = entry.getValue();

            if (!fullName.contains(TEAM_SEPARATOR)) {
                // No "&" — try as challongeTeamName first, then teamName
                Optional<Teams> byChallongeName = teamsRepository.findByChallongeTeamName(fullName);
                if (byChallongeName.isPresent()) {
                    result.put(challongeId, byChallongeName.get());
                    continue;
                }
                List<Teams> byName = teamsRepository.findByTeamNameContainingIgnoreCase(fullName);
                if (byName.size() == 1) {
                    result.put(challongeId, byName.get(0));
                } else {
                    unmatchedNames.add(fullName);
                    log.warn("[Challonge] Cannot resolve team for name '{}'", fullName);
                }
                continue;
            }

            // Strategy 1 — exact challongeTeamName match ("userName1 & userName2")
            Optional<Teams> byChallongeName = teamsRepository.findByChallongeTeamName(fullName);
            if (byChallongeName.isPresent()) {
                result.put(challongeId, byChallongeName.get());
                log.debug("[Challonge] Resolved team '{}' by challongeTeamName", fullName);
                continue;
            }

            // Strategy 2 — try reversed order ("userName2 & userName1")
            String[] parts = fullName.split(" & ", 2);
            String username1 = parts[0].trim();
            String username2 = parts[1].trim();
            String reversed = username2 + " & " + username1;
            Optional<Teams> byReversed = teamsRepository.findByChallongeTeamName(reversed);
            if (byReversed.isPresent()) {
                result.put(challongeId, byReversed.get());
                log.debug("[Challonge] Resolved team '{}' by reversed challongeTeamName", fullName);
                continue;
            }

            // Strategy 3 — resolve both usernames then look up by player pair
            Optional<Users> u1 = resolveOneUser(username1);
            Optional<Users> u2 = resolveOneUser(username2);

            if (u1.isEmpty() || u2.isEmpty()) {
                unmatchedNames.add(fullName);
                log.warn("[Challonge] Cannot resolve users for team '{}' " +
                        "(user1='{}' found={}, user2='{}' found={})",
                        fullName, username1, u1.isPresent(), username2, u2.isPresent());
                continue;
            }

            Optional<Teams> team = teamsRepository.findTeamByPlayers(
                    u1.get().getId(), u2.get().getId());

            if (team.isPresent()) {
                result.put(challongeId, team.get());
                log.debug("[Challonge] Resolved team '{}' by player pair ({} + {})",
                        fullName, username1, username2);
            } else {
                unmatchedNames.add(fullName);
                log.warn("[Challonge] No team found for player pair '{}' + '{}'",
                        username1, username2);
            }
        }

        return result;
    }

    /** Tries to find a single user by userName, then falls back to name search. */
    private Optional<Users> resolveOneUser(String username) {
        Optional<Users> user = userRepository.findByUserName(username);
        if (user.isPresent()) return user;

        List<Users> candidates = userRepository.findByNameContainingIgnoreCase(username);
        if (candidates.size() == 1) return Optional.of(candidates.get(0));
        if (candidates.size() > 1) {
            return candidates.stream()
                    .filter(u -> username.equalsIgnoreCase(u.getName()))
                    .findFirst();
        }
        return Optional.empty();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // APPLY DATA — SINGLES
    // ─────────────────────────────────────────────────────────────────────────

    private void applyPlayerData(Matches match,
                                 ChallongeMatchDto cm,
                                 Map<Long, Users> participantUsers) {
        match.setStatus(cm.toLocalStatus());
        match.setTeamOneScore(cm.parsePlayer1Score());
        match.setTeamTwoScore(cm.parsePlayer2Score());
        match.setChallongeMatchId(cm.getId());

        if (cm.getWinnerId() != null) {
            Users winner = participantUsers.get(cm.getWinnerId());
            if (winner != null) match.setWinnerPlayer(winner);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // APPLY DATA — TEAMS
    // ─────────────────────────────────────────────────────────────────────────

    private void applyTeamData(Matches match,
                               ChallongeMatchDto cm,
                               Teams team1,
                               Teams team2,
                               Map<Long, Teams> participantTeams) {
        match.setStatus(cm.toLocalStatus());
        match.setTeamOneScore(cm.parsePlayer1Score());
        match.setTeamTwoScore(cm.parsePlayer2Score());
        match.setChallongeMatchId(cm.getId());

        if (cm.getWinnerId() != null) {
            Teams winner = participantTeams.get(cm.getWinnerId());
            if (winner != null) match.setWinnerTeam(winner);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MANUAL-MATCH DEDUP — look for pre-existing admin-created rows
    // ─────────────────────────────────────────────────────────────────────────

    private Optional<Matches> findManualPlayerMatch(Long tournamentId,
                                                    Users p1, Users p2,
                                                    Integer round) {
        return matchRepository.findByTournamentId(tournamentId).stream()
                .filter(m -> m.getChallongeMatchId() == null)
                .filter(m -> m.getPlayerOne() != null && m.getPlayerTwo() != null)
                .filter(m -> round != null && round.equals(m.getRoundNumber()))
                .filter(m -> (m.getPlayerOne().getId().equals(p1.getId()) &&
                              m.getPlayerTwo().getId().equals(p2.getId()))
                          || (m.getPlayerOne().getId().equals(p2.getId()) &&
                              m.getPlayerTwo().getId().equals(p1.getId())))
                .findFirst();
    }

    private Optional<Matches> findManualTeamMatch(Long tournamentId,
                                                  Teams t1, Teams t2,
                                                  Integer round) {
        return matchRepository.findByTournamentId(tournamentId).stream()
                .filter(m -> m.getChallongeMatchId() == null)
                .filter(m -> m.getTeamOne() != null && m.getTeamTwo() != null)
                .filter(m -> round != null && round.equals(m.getRoundNumber()))
                .filter(m -> (m.getTeamOne().getTeamId().equals(t1.getTeamId()) &&
                              m.getTeamTwo().getTeamId().equals(t2.getTeamId()))
                          || (m.getTeamOne().getTeamId().equals(t2.getTeamId()) &&
                              m.getTeamTwo().getTeamId().equals(t1.getTeamId())))
                .findFirst();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CHALLONGE API FETCHERS
    // ─────────────────────────────────────────────────────────────────────────

    private Map<Long, String> fetchParticipantNames(String challongeUrl) {
        String body = get(CHALLONGE_BASE + "/" + challongeUrl + "/participants.json");
        try {
            List<ChallongeParticipantWrapperDto> wrappers =
                    objectMapper.readValue(body, new TypeReference<>() {});
            return wrappers.stream()
                    .map(ChallongeParticipantWrapperDto::getParticipant)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(
                            ChallongeParticipantDto::getId,
                            ChallongeParticipantDto::resolvedUsername));
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse Challonge participants response: " + e.getMessage(), e);
        }
    }

    private List<ChallongeMatchDto> fetchMatches(String challongeUrl) {
        String body = get(CHALLONGE_BASE + "/" + challongeUrl + "/matches.json");
        try {
            List<ChallongeMatchWrapperDto> wrappers =
                    objectMapper.readValue(body, new TypeReference<>() {});
            return wrappers.stream()
                    .map(ChallongeMatchWrapperDto::getMatch)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse Challonge matches response: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HTTP — Basic Auth
    // ─────────────────────────────────────────────────────────────────────────

    private String get(String url) {
        String encoded = Base64.getEncoder().encodeToString(
                (challongeUsername + ":" + challongeApiKey)
                        .getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Basic " + encoded)
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "Challonge API returned HTTP " + response.statusCode()
                        + " for URL: " + url + " — body: " + response.body());
            }
            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("Failed to call Challonge API: " + e.getMessage(), e);
        }
    }
}
