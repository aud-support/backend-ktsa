package com.ktsa.foosball.repository;

import com.ktsa.foosball.model.Registration;
import com.ktsa.foosball.model.Teams;
import com.ktsa.foosball.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    @Query("SELECT u FROM Registration r JOIN r.player u " +
           "WHERE r.tournamentId = :tournamentId " +
           "AND LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Users> searchPlayersByTournament(
            @Param("tournamentId") Long tournamentId,
            @Param("query") String query
    );

    Registration findByTournamentIdAndPlayerAndCategory(Long tournamentId, Users player, String category);

    Registration findByTournamentIdAndTeamAndCategory(Long tournamentId, Teams teamByPlayers, String category);

    Registration findByTournamentIdAndTeam(Long tournamentId, Teams teamByPlayers);
}
