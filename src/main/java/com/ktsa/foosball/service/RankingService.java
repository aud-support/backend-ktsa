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

    public static final String MENS_SINGLES          = "MENS_SINGLES";
    public static final String WOMENS_SINGLES        = "WOMENS_SINGLES";
    public static final String OPEN_SINGLES          = "OPEN_SINGLES";
    public static final String UNDER_16              = "UNDER_16";
    public static final String ABOVE_16              = "ABOVE_16";
    public static final String OPEN_DOUBLES          = "OPEN_DOUBLES";
    public static final String MIXED_DOUBLES         = "MIXED_DOUBLES";
    public static final String BEGINNER_DOUBLES      = "BEGINNER_DOUBLES";
    public static final String WOMENS_DOUBLES        = "WOMENS_DOUBLES";
    public static final String MENS_DOUBLES          = "MENS_DOUBLES";
    public static final String JUNIOR_U16_DOUBLES    = "JUNIOR_U16_DOUBLES";
    public static final String JUNIOR_ABOVE16_SINGLES = "JUNIOR_ABOVE16_SINGLES";
    public static final String JUNIOR_ABOVE16_DOUBLES = "JUNIOR_ABOVE16_DOUBLES";
    public static final String SENIOR_DOUBLES        = "SENIOR_DOUBLES";
    public static final String DISABLED_SINGLES      = "DISABLED_SINGLES";
    public static final String DISABLED_DOUBLES      = "DISABLED_DOUBLES";
    public static final String DISABLED_MIXED        = "DISABLED_MIXED";
    public static final String MONSTER_DYP           = "MONSTER_DYP";
    public static final String TEAM_EVENT            = "TEAM_EVENT";

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
        result.addAll(buildSinglesRankings(completed, OPEN_SINGLES));
        result.addAll(buildSinglesRankings(completed, UNDER_16));
        result.addAll(buildSinglesRankings(completed, ABOVE_16));
        result.addAll(buildSinglesRankings(completed, JUNIOR_ABOVE16_SINGLES));
        result.addAll(buildSinglesRankings(completed, DISABLED_SINGLES));
        result.addAll(buildDoublesRankings(completed, OPEN_DOUBLES));
        result.addAll(buildDoublesRankings(completed, MIXED_DOUBLES));
        result.addAll(buildDoublesRankings(completed, BEGINNER_DOUBLES));
        result.addAll(buildDoublesRankings(completed, WOMENS_DOUBLES));
        result.addAll(buildDoublesRankings(completed, MENS_DOUBLES));
        result.addAll(buildDoublesRankings(completed, JUNIOR_U16_DOUBLES));
        result.addAll(buildDoublesRankings(completed, JUNIOR_ABOVE16_DOUBLES));
        result.addAll(buildDoublesRankings(completed, SENIOR_DOUBLES));
        result.addAll(buildDoublesRankings(completed, DISABLED_DOUBLES));
        result.addAll(buildDoublesRankings(completed, DISABLED_MIXED));
        result.addAll(buildDoublesRankings(completed, MONSTER_DYP));
        result.addAll(buildDoublesRankings(completed, TEAM_EVENT));
        return result;
    }

    /**
     * Returns the list of all ranking categories with display labels.
     * Used by the frontend to build dynamic category tabs.
     */
    public List<Map<String, String>> getRankingCategories() {
        List<Map<String, String>> cats = new ArrayList<>();
        cats.add(Map.of("key", MENS_SINGLES,            "label", "Men's Singles"));
        cats.add(Map.of("key", WOMENS_SINGLES,          "label", "Women's Singles"));
        cats.add(Map.of("key", OPEN_SINGLES,            "label", "Open Singles"));
        cats.add(Map.of("key", UNDER_16,                "label", "Junior U16 Singles"));
        cats.add(Map.of("key", ABOVE_16,                "label", "Junior Above 16 Singles"));
        cats.add(Map.of("key", JUNIOR_ABOVE16_SINGLES,  "label", "Junior Above 16 Singles"));
        cats.add(Map.of("key", DISABLED_SINGLES,        "label", "Disabled Singles"));
        cats.add(Map.of("key", OPEN_DOUBLES,            "label", "Open Doubles"));
        cats.add(Map.of("key", MIXED_DOUBLES,           "label", "Mixed Doubles"));
        cats.add(Map.of("key", BEGINNER_DOUBLES,        "label", "Beginner Doubles"));
        cats.add(Map.of("key", WOMENS_DOUBLES,          "label", "Women's Doubles"));
        cats.add(Map.of("key", MENS_DOUBLES,            "label", "Men's Doubles"));
        cats.add(Map.of("key", JUNIOR_U16_DOUBLES,      "label", "Junior U16 Doubles"));
        cats.add(Map.of("key", JUNIOR_ABOVE16_DOUBLES,  "label", "Junior Above 16 Doubles"));
        cats.add(Map.of("key", SENIOR_DOUBLES,          "label", "Senior Doubles"));
        cats.add(Map.of("key", DISABLED_DOUBLES,        "label", "Disabled Doubles"));
        cats.add(Map.of("key", DISABLED_MIXED,          "label", "Disabled Mixed"));
        cats.add(Map.of("key", MONSTER_DYP,             "label", "Monster - DYP"));
        cats.add(Map.of("key", TEAM_EVENT,              "label", "Team Event"));
        return cats;
    }

    /**
     * Returns the top 3 players/teams for the homepage spotlight:
     *   slot 0 — #1 Men's Singles
     *   slot 1 — #1 Women's Singles
     *   slot 2 — #1 Open Doubles
     * Only entries with at least 1 match played are included.
     */
    public List<RankingResponseDTO> getTopSpotlightPlayers() {
        List<Matches> completed = matchRepository.findAllCompletedMatches();
        List<RankingResponseDTO> spotlight = new ArrayList<>();

        buildSinglesRankings(completed, MENS_SINGLES).stream()
                .filter(r -> r.getMatches() > 0).findFirst().ifPresent(spotlight::add);
        buildSinglesRankings(completed, WOMENS_SINGLES).stream()
                .filter(r -> r.getMatches() > 0).findFirst().ifPresent(spotlight::add);
        buildDoublesRankings(completed, OPEN_DOUBLES).stream()
                .filter(r -> r.getMatches() > 0).findFirst().ifPresent(spotlight::add);

        // Enrich singles entries with profile picture URL
        spotlight.forEach(dto -> {
            if (dto.getEmail() != null) {
                userRepository.findByEmail(dto.getEmail())
                        .ifPresent(u -> dto.setProfilePictureUrl(u.getProfilePictureUrl()));
            }
        });
        return spotlight;
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
    // CATEGORY NORMALISATION
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Normalises any stored category string to a canonical ranking key.
     * Handles values like "Men's Singles", "Open Singles", "MENS_SINGLES", etc.
     */
    static String normalizeCategory(String category) {
        if (category == null) return null;
        String key = category.trim()
                .toUpperCase()
                .replace("'", "")
                .replace("\u2019", "")  // right single quotation mark
                .replace(" ", "_")
                .replace("-", "_");
        return switch (key) {
            case "MENS_SINGLES",   "MEN_SINGLES",   "MENS_SINGLE",
                 "MALE_SINGLES"                                -> "MENS_SINGLES";
            case "WOMENS_SINGLES", "WOMEN_SINGLES", "WOMENS_SINGLE",
                 "FEMALE_SINGLES"                             -> "WOMENS_SINGLES";
            case "OPEN_SINGLES",   "OPEN_SINGLE"              -> "OPEN_SINGLES";
            case "UNDER_16",  "UNDER16", "U16", "U_16",
                 "UNDER_SIXTEEN", "UNDERSIXTEEN",
                 "JUNIOR_U16_SINGLES", "JUNIOR_U16_SINGLE"   -> "UNDER_16";
            case "ABOVE_16",  "ABOVE16", "A16", "A_16",
                 "ABOVE_SIXTEEN", "ABOVESIXTEEN"              -> "ABOVE_16";
            case "OPEN_DOUBLES",   "OPEN_DOUBLE"              -> "OPEN_DOUBLES";
            case "MIXED_DOUBLES",  "MIXED",
                 "MIXED_DOUBLE"                               -> "MIXED_DOUBLES";
            case "BEGINNER_DOUBLES", "BEGINNER_DOUBLE",
                 "BEGINNER"                                   -> "BEGINNER_DOUBLES";
            case "WOMENS_DOUBLES", "WOMEN_DOUBLES",
                 "WOMENS_DOUBLE", "WOMEN_DOUBLE"              -> "WOMENS_DOUBLES";
            case "MENS_DOUBLES", "MEN_DOUBLES",
                 "MENS_DOUBLE", "MEN_DOUBLE"                  -> "MENS_DOUBLES";
            case "JUNIOR_U16_DOUBLES", "JUNIOR_U16_DOUBLE"    -> "JUNIOR_U16_DOUBLES";
            case "JUNIOR_ABOVE16_SINGLES", "JUNIOR_ABOVE16_SINGLE",
                 "JUNIOR_ABOVE_16_SINGLES"                    -> "JUNIOR_ABOVE16_SINGLES";
            case "JUNIOR_ABOVE16_DOUBLES", "JUNIOR_ABOVE16_DOUBLE",
                 "JUNIOR_ABOVE_16_DOUBLES"                    -> "JUNIOR_ABOVE16_DOUBLES";
            case "SENIOR_DOUBLES", "SENIOR_DOUBLE"            -> "SENIOR_DOUBLES";
            case "DISABLED_SINGLES", "DISABLED_SINGLE"        -> "DISABLED_SINGLES";
            case "DISABLED_DOUBLES", "DISABLED_DOUBLE"        -> "DISABLED_DOUBLES";
            case "DISABLED_MIXED"                             -> "DISABLED_MIXED";
            case "MONSTER_DYP", "MONSTER"                    -> "MONSTER_DYP";
            case "TEAM_EVENT", "TEAM"                         -> "TEAM_EVENT";
            default -> key;
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SINGLES
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * points = sum of actual match scores the player scored across every
     *          completed match in every tournament.
     *
     * Category filter: explicit match.category first, then gender inference
     * for backwards compatibility with older matches (only for MENS/WOMENS).
     */
    private List<RankingResponseDTO> buildSinglesRankings(
            List<Matches> completedMatches, String category) {

        // Gender-based fallback only applies to gendered singles categories
        Gender targetGender = null;
        if (category.equals(MENS_SINGLES))   targetGender = Gender.MALE;
        if (category.equals(WOMENS_SINGLES)) targetGender = Gender.FEMALE;
        final Gender finalTargetGender = targetGender;

        // userId -> [wins, losses, totalPoints]
        Map<Long, int[]> statsMap = new HashMap<>();
        Map<Long, Users> seenUsers = new HashMap<>();

        for (Matches m : completedMatches) {
            if (!isSinglesMatch(m)) continue;

            // prefer explicit category; fall back to gender inference for legacy MENS/WOMENS only
            if (m.getCategory() != null) {
                if (!category.equals(normalizeCategory(m.getCategory()))) continue;
            } else {
                if (finalTargetGender != null) {
                    if (m.getPlayerOne().getGender() != finalTargetGender
                            || m.getPlayerTwo().getGender() != finalTargetGender) continue;
                } else {
                    // OPEN_SINGLES / UNDER_16 / ABOVE_16 — skip matches without explicit category
                    continue;
                }
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

        // Only include players who actually played matches in this specific category.
        // For gender-restricted categories (MENS_SINGLES, WOMENS_SINGLES), also enforce
        // the gender filter on seenUsers — guards against legacy data where Open Singles
        // matches may have been incorrectly stored under a gendered category key.
        Map<Long, Users> registry = new LinkedHashMap<>();
        for (Map.Entry<Long, Users> entry : seenUsers.entrySet()) {
            Users u = entry.getValue();
            if (finalTargetGender != null && u.getGender() != finalTargetGender) continue;
            registry.put(entry.getKey(), u);
        }

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
                            .profilePictureUrl(user.getProfilePictureUrl())
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
                if (!category.equals(normalizeCategory(m.getCategory()))) continue;
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
