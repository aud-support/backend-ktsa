package com.ktsa.foosball.service;

import com.ktsa.foosball.dto.RankingResponseDTO;
import com.ktsa.foosball.model.Gender;
import com.ktsa.foosball.model.Matches;
import com.ktsa.foosball.model.Ranking;
import com.ktsa.foosball.model.Teams;
import com.ktsa.foosball.model.Users;
import com.ktsa.foosball.repository.MatchRepository;
import com.ktsa.foosball.repository.RankingRepository;
import com.ktsa.foosball.repository.TeamsRepository;
import com.ktsa.foosball.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankingService {

    private static final int POINTS_PER_WIN = 3;

    public static final String MENS_SINGLES   = "MENS_SINGLES";
    public static final String WOMENS_SINGLES = "WOMENS_SINGLES";
    public static final String OPEN_DOUBLES   = "OPEN_DOUBLES";
    public static final String MIXED_DOUBLES  = "MIXED_DOUBLES";

    private final RankingRepository rankingRepository;
    private final MatchRepository   matchRepository;
    private final TeamsRepository   teamsRepository;
    private final UserRepository    userRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────────────────────────────

    public List<RankingResponseDTO> getAllRankings() {
        List<Matches> completed = matchRepository.findAllCompletedMatches();

        List<RankingResponseDTO> result = new ArrayList<>();
        result.addAll(buildSinglesRankings(completed, MENS_SINGLES));
        result.addAll(buildSinglesRankings(completed, WOMENS_SINGLES));
        result.addAll(buildDoublesRankings(completed, OPEN_DOUBLES));
        result.addAll(buildDoublesRankings(completed, MIXED_DOUBLES));
        return result;
    }

    public RankingResponseDTO getRankingByUserId(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        List<Matches> completed = matchRepository.findAllCompletedMatches();
        int wins = 0, losses = 0;

        for (Matches m : completed) {
            if (!isSinglesMatch(m)) continue;
            Long p1 = m.getPlayerOne().getId();
            Long p2 = m.getPlayerTwo().getId();
            if (!userId.equals(p1) && !userId.equals(p2)) continue;

            Long winnerId = resolveWinnerPlayer(m);
            if (winnerId != null) {
                if (userId.equals(winnerId)) wins++;
                else losses++;
            }
        }

        Optional<Ranking> ranking = rankingRepository.findByUserId(userId);
        String category = user.getGender() == Gender.FEMALE ? WOMENS_SINGLES : MENS_SINGLES;

        return RankingResponseDTO.builder()
                .id(ranking.map(Ranking::getId).orElse(null))
                .wins(wins)
                .losses(losses)
                .matches(wins + losses)
                .points(wins * POINTS_PER_WIN)
                .userName(user.getName())
                .email(user.getEmail())
                .gender(user.getGender())
                .category(category)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SINGLES
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds rankings for one singles category.
     *
     * Player registry = union of:
     *   (a) Users who have a Ranking row with matching gender
     *   (b) Users who appear in a completed singles match with matching gender
     *
     * Winner is resolved as:
     *   1. winnerPlayer field (if set)
     *   2. Higher score (if scores are available and not equal)
     */
    private List<RankingResponseDTO> buildSinglesRankings(
            List<Matches> completedMatches, String category) {

        Gender targetGender = category.equals(MENS_SINGLES) ? Gender.MALE : Gender.FEMALE;

        // --- aggregate stats from matches ---
        // userId -> [wins, losses]
        Map<Long, int[]> statsMap = new HashMap<>();
        // collect all users seen in matches of this gender
        Map<Long, Users> seenUsers = new HashMap<>();

        for (Matches m : completedMatches) {
            if (!isSinglesMatch(m)) continue;

            Users p1 = m.getPlayerOne();
            Users p2 = m.getPlayerTwo();

            if (p1.getGender() != targetGender || p2.getGender() != targetGender) continue;

            seenUsers.put(p1.getId(), p1);
            seenUsers.put(p2.getId(), p2);

            statsMap.putIfAbsent(p1.getId(), new int[]{0, 0});
            statsMap.putIfAbsent(p2.getId(), new int[]{0, 0});

            Long winnerId = resolveWinnerPlayer(m);
            if (winnerId != null) {
                Long loserId = winnerId.equals(p1.getId()) ? p2.getId() : p1.getId();
                statsMap.get(winnerId)[0]++; // win
                statsMap.get(loserId)[1]++;  // loss
            }
        }

        // --- build player registry: Ranking rows + anyone seen in matches ---
        Map<Long, Users> registry = new LinkedHashMap<>();

        // first add from Ranking table (preserves existing registered players)
        rankingRepository.findAll().stream()
                .filter(r -> r.getUser() != null)
                .filter(r -> r.getUser().getGender() == targetGender)
                .forEach(r -> registry.put(r.getUser().getId(), r.getUser()));

        // then add anyone who played but may not have a Ranking row yet
        seenUsers.forEach(registry::putIfAbsent);

        // --- build DTOs ---
        return registry.values().stream()
                .map(user -> {
                    int[] s = statsMap.getOrDefault(user.getId(), new int[]{0, 0});
                    return RankingResponseDTO.builder()
                            .wins(s[0])
                            .losses(s[1])
                            .matches(s[0] + s[1])
                            .points(s[0] * POINTS_PER_WIN)
                            .userName(user.getName())
                            .email(user.getEmail())
                            .gender(user.getGender())
                            .category(category)
                            .build();
                })
                .sorted(Comparator.comparingInt(RankingResponseDTO::getPoints).reversed())
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DOUBLES
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds rankings for one doubles category.
     *
     * Team registry = all teams that appeared in at least one completed match
     * of this category.
     *
     * Winner resolved the same way: winnerTeam first, then higher score.
     */
    private List<RankingResponseDTO> buildDoublesRankings(
            List<Matches> completedMatches, String category) {

        // teamId -> [wins, losses]
        Map<Long, int[]> teamStatsMap = new LinkedHashMap<>();
        Map<Long, Teams> teamRegistry = new LinkedHashMap<>();

        for (Matches m : completedMatches) {
            if (!isDoublesMatch(m)) continue;
            if (!matchesDoublesCategory(m, category)) continue;

            Teams t1 = m.getTeamOne();
            Teams t2 = m.getTeamTwo();
            Long t1Id = t1.getTeamId();
            Long t2Id = t2.getTeamId();

            teamRegistry.putIfAbsent(t1Id, t1);
            teamRegistry.putIfAbsent(t2Id, t2);
            teamStatsMap.putIfAbsent(t1Id, new int[]{0, 0});
            teamStatsMap.putIfAbsent(t2Id, new int[]{0, 0});

            Long winnerTeamId = resolveWinnerTeam(m);
            if (winnerTeamId != null) {
                Long loserTeamId = winnerTeamId.equals(t1Id) ? t2Id : t1Id;
                teamStatsMap.get(winnerTeamId)[0]++;
                teamStatsMap.get(loserTeamId)[1]++;
            }
        }

        return teamRegistry.entrySet().stream()
                .map(entry -> {
                    Teams team = entry.getValue();
                    int[] s = teamStatsMap.getOrDefault(entry.getKey(), new int[]{0, 0});
                    return toDoublesDTO(team, s[0], s[1], category);
                })
                .sorted(Comparator.comparingInt(RankingResponseDTO::getPoints).reversed())
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WINNER RESOLUTION
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Resolves the winning player for a singles match.
     * Priority: explicit winnerPlayer field → higher score → null (draw/unknown)
     */
    private Long resolveWinnerPlayer(Matches m) {
        // 1. explicit winner
        if (m.getWinnerPlayer() != null) {
            return m.getWinnerPlayer().getId();
        }
        // 2. infer from scores
        if (m.getTeamOneScore() != null && m.getTeamTwoScore() != null) {
            if (m.getTeamOneScore() > m.getTeamTwoScore()) {
                return m.getPlayerOne() != null ? m.getPlayerOne().getId() : null;
            } else if (m.getTeamTwoScore() > m.getTeamOneScore()) {
                return m.getPlayerTwo() != null ? m.getPlayerTwo().getId() : null;
            }
        }
        return null; // draw or no score data
    }

    /**
     * Resolves the winning team for a doubles match.
     * Priority: explicit winnerTeam field → higher score → null
     */
    private Long resolveWinnerTeam(Matches m) {
        // 1. explicit winner
        if (m.getWinnerTeam() != null) {
            return m.getWinnerTeam().getTeamId();
        }
        // 2. infer from scores
        if (m.getTeamOneScore() != null && m.getTeamTwoScore() != null) {
            if (m.getTeamOneScore() > m.getTeamTwoScore()) {
                return m.getTeamOne() != null ? m.getTeamOne().getTeamId() : null;
            } else if (m.getTeamTwoScore() > m.getTeamOneScore()) {
                return m.getTeamTwo() != null ? m.getTeamTwo().getTeamId() : null;
            }
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private boolean isSinglesMatch(Matches m) {
        return m.getPlayerOne() != null && m.getPlayerTwo() != null
                && m.getTeamOne() == null && m.getTeamTwo() == null;
    }

    private boolean isDoublesMatch(Matches m) {
        return m.getTeamOne() != null && m.getTeamTwo() != null;
    }

    /**
     * Mixed Doubles = at least one team has players of different genders.
     * Open Doubles  = all teams are same-gender.
     */
    private boolean matchesDoublesCategory(Matches m, String category) {
        boolean isMixed = isTeamMixed(m.getTeamOne()) || isTeamMixed(m.getTeamTwo());
        return category.equals(MIXED_DOUBLES) ? isMixed : !isMixed;
    }

    private boolean isTeamMixed(Teams team) {
        if (team == null) return false;
        Gender g1 = team.getPlayerOne() != null ? team.getPlayerOne().getGender() : null;
        Gender g2 = team.getPlayerTwo() != null ? team.getPlayerTwo().getGender() : null;
        return g1 != null && g2 != null && g1 != g2;
    }

    private RankingResponseDTO toDoublesDTO(Teams team, int wins, int losses, String category) {
        String p1Name = team.getPlayerOne() != null ? team.getPlayerOne().getName() : "?";
        String p2Name = team.getPlayerTwo() != null ? team.getPlayerTwo().getName() : "?";
        return RankingResponseDTO.builder()
                .teamId(team.getTeamId())
                .wins(wins)
                .losses(losses)
                .matches(wins + losses)
                .points(wins * POINTS_PER_WIN)
                .userName(p1Name + " & " + p2Name)
                .category(category)
                .build();
    }
}
