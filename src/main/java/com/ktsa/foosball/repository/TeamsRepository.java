package com.ktsa.foosball.repository;

import com.ktsa.foosball.model.Teams;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TeamsRepository extends JpaRepository<Teams, Long> {
    Collection<Object> findByPlayerOneId(Long id);

    Optional<Teams> findByPlayerOneIdAndPlayerTwoId(Long id, Long id1);

    boolean existsByTeamName(String teamName);

    List<Teams> findByTeamNameContainingIgnoreCase(String query);

    Optional<Teams> findByChallongeTeamName(String challongeTeamName);

    @Query("""
SELECT t FROM Teams t
WHERE
(t.playerOne.id = :playerOneId AND t.playerTwo.id = :playerTwoId)
OR
(t.playerOne.id = :playerTwoId AND t.playerTwo.id = :playerOneId)
""")
    Optional<Teams> findTeamByPlayers(
            Long playerOneId,
            Long playerTwoId);
}
