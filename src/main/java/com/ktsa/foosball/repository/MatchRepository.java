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

    java.util.Optional<Matches> findByTournamentIdAndChallongeMatchId(Long tournamentId, Long challongeMatchId);

    /** All completed matches (singles and doubles) */
    @Query("SELECT m FROM Matches m WHERE m.status = 'COMPLETED'")
    List<Matches> findAllCompletedMatches();

    /**
     * All matches where the given user is playerOne, playerTwo,
     * or a member of teamOne or teamTwo.
     */
    @Query("""
        SELECT DISTINCT m FROM Matches m
        WHERE m.playerOne.id = :userId
           OR m.playerTwo.id = :userId
           OR m.teamOne.playerOne.id = :userId
           OR m.teamOne.playerTwo.id = :userId
           OR m.teamTwo.playerOne.id = :userId
           OR m.teamTwo.playerTwo.id = :userId
        ORDER BY m.scheduledAt DESC NULLS LAST
        """)
    List<Matches> findAllMatchesForUser(@org.springframework.data.repository.query.Param("userId") Long userId);
}
