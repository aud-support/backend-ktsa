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

    java.util.Optional<Matches> findByTournamentIdAndChallongeMatchId(Long tournamentId, Long challongeMatchId);

    /** All completed matches (singles and doubles) */
    @Query("SELECT m FROM Matches m WHERE m.status = 'COMPLETED'")
    List<Matches> findAllCompletedMatches();
}
