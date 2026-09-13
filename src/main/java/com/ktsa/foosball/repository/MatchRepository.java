package com.ktsa.foosball.repository;

import com.ktsa.foosball.model.Matches;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MatchRepository extends JpaRepository<Matches, Long> {

    // Player matches
    boolean existsByTournamentIdAndPlayerOneIdAndPlayerTwoId(
            Long tournamentId,
            Long playerOne,
            Long playerTwo
    );

    // ✅ Works — Teams PK is "teamId"
    boolean existsByTournamentIdAndTeamOneTeamIdAndTeamTwoTeamId(
            Long tournamentId,
            Long teamOneTeamId,
            Long teamTwoTeamId
    );

    List<Matches> findByTournamentId(Long tournamentId);

    List<Matches> findByTournamentIdAndCategory(Long tournamentId, String category);

    /**
     * Case-insensitive category match — use this when querying with the
     * canonical key (e.g. "MENS_SINGLES") so it also finds legacy rows
     * stored as "Men's Singles" that haven't been migrated yet.
     */
    @Query("SELECT m FROM Matches m WHERE m.tournament.id = :tournamentId AND UPPER(m.category) = UPPER(:category)")
    List<Matches> findByTournamentIdAndCategoryIgnoreCase(
            @org.springframework.data.repository.query.Param("tournamentId") Long tournamentId,
            @org.springframework.data.repository.query.Param("category") String category);

    java.util.Optional<Matches> findByTournamentIdAndChallongeMatchId(Long tournamentId, Long challongeMatchId);

    void deleteAllByTournamentId(Long tournamentId);

    /** All completed matches (singles and doubles) */
    @Query("SELECT m FROM Matches m WHERE UPPER(m.status) = 'COMPLETED'")
    List<Matches> findAllCompletedMatches();

    /**
     * All matches where the given user is playerOne, playerTwo,
     * or a member of teamOne or teamTwo.
     * Uses LEFT JOIN to safely handle singles matches (where teamOne/teamTwo are null).
     */
    @Query("""
        SELECT DISTINCT m FROM Matches m
        LEFT JOIN m.teamOne t1
        LEFT JOIN m.teamTwo t2
        WHERE m.playerOne.id = :userId
           OR m.playerTwo.id = :userId
           OR t1.playerOne.id = :userId
           OR t1.playerTwo.id = :userId
           OR t2.playerOne.id = :userId
           OR t2.playerTwo.id = :userId
        ORDER BY m.scheduledAt DESC NULLS LAST
        """)
    List<Matches> findAllMatchesForUser(@org.springframework.data.repository.query.Param("userId") Long userId);
}
