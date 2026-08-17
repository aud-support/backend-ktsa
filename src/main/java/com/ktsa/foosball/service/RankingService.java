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

        // [wins, losses, totalPoints]
        int[] stats = new int[]{0, 0, 0};

        for (Matches m : completed) {
            if (!isSinglesMatch(m)) continue;
            Long p1Id = m.getPlayerOne().getId();
            Long p2Id = m.getPlayerTwo().getId();
            if (!userId.equals(p1Id) && !userId.equals(p2Id)) continue;

            boolean isPlayerOne = userId.equals(p1Id);
            Long myScore = isPlayerOne ? m.getTeamOneScore() : m.getTeamTwoScore();

            if (myScore != null) stats[2] += (int) (long) myScore;

            Long winnerId = resolveWinnerPlayer(m);
            if (winnerId != null) {
                if (userId.equals(winnerId)) stats[0]++;
                else stats[1]++;
            }
        }

        Optional<Ranking> ranking = rankingRepository.findByUserId(userId);
        String category = user.getGender() == Gender.FEMALE ? WOMENS_SINGLES : MENS_SINGLES;

        return RankingResponseDTO.builder()
                .id(ranking.map(Ranking::getId).orElse(null))
                .wins(stats[0])
                .losses(stats[1])
                .matches(stats[0] + stats[1])
                .points(stats[2])
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
     * points = sum of actual match scores the player scored across every
     *          completed match in every tournament.
     *
     * Category filter: explicit match.category first, then gender inference
     * for backwards compatibility with older matches.
     */
    private List<RankingResponseDTO> buildSinglesRankings(
            List<Matches> completedMatches, String category) {

        Gender targetGender = category.equals(MENS_SINGLES) ? Gender.MALE : Gender.FEMALE;

        // userId -> [wins, losses, totalPoints]
        Map<Long, int[]> statsMap = new HashMap<>();
        Map<Long, Users> seenUsers = new HashMap<>();

        for (Matches m : completedMatches) {
            if (!isSinglesMatch(m)) continue;

            // prefer explicit category; fall back to gender inference
            if (m.getCategory() != null) {
                if (!category.equals(m.getCategory())) continue;
            } else {
                if (m.getPlayerOne().getGender() != targetGender
                        || m.getPlayerTwo().getGender() != targetGender) continue;
            }

            Users p1 = m.getPlayerOne();
            Users p2 = m.getPlayerTwo();

            seenUsers.put(p1.getId(), p1);
            seenUsers.put(p2.getId(), p2);
            statsMap.putIfAbsent(p1.getId(), new int[]{0, 0, 0});
            statsMap.putIfAbsent(p2.getId(), new int[]{0, 0, 0});

            // accumulate actual scores across ALL tournaments
            int p1Score = m.getTeamOneScore() != null ? (int) (long) m.getTeamOneScore() : 0;
            int p2Score = m.getTeamTwoScore() != null ? (int) (long) m.getTeamTwoScore() : 0;
            statsMap.get(p1.getId())[2] += p1Score;
            statsMap.get(p2.getId())[2] += p2Score;

            Long winnerId = resolveWinnerPlayer(m);
            if (winnerId != null) {
                Long loserId = winnerId.equals(p1.getId()) ? p2.getId() : p1.getId();
                statsMap.get(winnerId)[0]++;
                statsMap.get(loserId)[1]++;
            }
        }

        Map<Long, Users> registry = new LinkedHashMap<>();
        rankingRepository.findAll().stream()
                .filter(r -> r.getUser() != null)
                .filter(r -> r.getUser().getGender() == targetGender)
                .forEach(r -> registry.put(r.getUser().getId(), r.getUser()));
        seenUsers.forEach(registry::putIfAbsent);

        return registry.values().stream()
                .map(user -> {
                    int[] s = statsMap.getOrDefault(user.getId(), new int[]{0, 0, 0});
                    return RankingResponseDTO.builder()
                            .wins(s[0])
                            .losses(s[1])
                            .matches(s[0] + s[1])
                            .points(s[2])
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
     * points = sum of actual match scores the team scored across every
     *          completed match in every tournament.
     *
     * Category filter: explicit match.category first, then composition inference.
     */
    private List<RankingResponseDTO> buildDoublesRankings(
            List<Matches> completedMatches, String category) {

        // teamId -> [wins, losses, totalPoints]
        Map<Long, int[]> teamStatsMap = new LinkedHashMap<>();
        Map<Long, Teams> teamRegistry = new LinkedHashMap<>();

        for (Matches m : completedMatches) {
            if (!isDoublesMatch(m)) continue;

            if (m.getCategory() != null) {
                if (!category.equals(m.getCategory())) continue;
            } else {
                if (!matchesDoublesCategory(m, category)) continue;
            }

            Teams t1 = m.getTeamOne();
            Teams t2 = m.getTeamTwo();
            Long t1Id = t1.getTeamId();
            Long t2Id = t2.getTeamId();

            teamRegistry.putIfAbsent(t1Id, t1);
            teamRegistry.putIfAbsent(t2Id, t2);
            teamStatsMap.putIfAbsent(t1Id, new int[]{0, 0, 0});
            teamStatsMap.putIfAbsent(t2Id, new int[]{0, 0, 0});

            // accumulate actual scores across ALL tournaments
            int t1Score = m.getTeamOneScore() != null ? (int) (long) m.getTeamOneScore() : 0;
            int t2Score = m.getTeamTwoScore() != null ? (int) (long) m.getTeamTwoScore() : 0;
            teamStatsMap.get(t1Id)[2] += t1Score;
            teamStatsMap.get(t2Id)[2] += t2Score;

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
                    int[] s = teamStatsMap.getOrDefault(entry.getKey(), new int[]{0, 0, 0});
                    return toDoublesDTO(team, s[0], s[1], s[2], category);
                })
                .sorted(Comparator.comparingInt(RankingResponseDTO::getPoints).reversed())
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WINNER RESOLUTION
    // ─────────────────────────────────────────────────────────────────────────

    private Long resolveWinnerPlayer(Matches m) {
        if (m.getWinnerPlayer() != null) {
            return m.getWinnerPlayer().getId();
        }
        if (m.getTeamOneScore() != null && m.getTeamTwoScore() != null) {
            if (m.getTeamOneScore() > m.getTeamTwoScore())
                return m.getPlayerOne() != null ? m.getPlayerOne().getId() : null;
            if (m.getTeamTwoScore() > m.getTeamOneScore())
                return m.getPlayerTwo() != null ? m.getPlayerTwo().getId() : null;
        }
        return null;
    }

    private Long resolveWinnerTeam(Matches m) {
        if (m.getWinnerTeam() != null) {
            return m.getWinnerTeam().getTeamId();
        }
        if (m.getTeamOneScore() != null && m.getTeamTwoScore() != null) {
            if (m.getTeamOneScore() > m.getTeamTwoScore())
                return m.getTeamOne() != null ? m.getTeamOne().getTeamId() : null;
            if (m.getTeamTwoScore() > m.getTeamOneScore())
                return m.getTeamTwo() != null ? m.getTeamTwo().getTeamId() : null;
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

    private RankingResponseDTO toDoublesDTO(Teams team, int wins, int losses,
                                            int totalPoints, String category) {
        String p1Name = team.getPlayerOne() != null ? team.getPlayerOne().getName() : "?";
        String p2Name = team.getPlayerTwo() != null ? team.getPlayerTwo().getName() : "?";
        return RankingResponseDTO.builder()
                .teamId(team.getTeamId())
                .wins(wins)
                .losses(losses)
                .matches(wins + losses)
                .points(totalPoints)
                .userName(p1Name + " & " + p2Name)
                .category(category)
                .build();
    }
}
