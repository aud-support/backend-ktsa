package com.ktsa.foosball.repository;

import com.ktsa.foosball.model.Registration;
import com.ktsa.foosball.model.Teams;
import com.ktsa.foosball.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    @Query("SELECT u FROM Registration r JOIN r.player u " +
           "WHERE r.tournamentId = :tournamentId " +
           "AND LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Users> searchPlayersByTournament(
            @Param("tournamentId") Long tournamentId,
            @Param("query") String query
    );

    /**
     * Checks whether a player is registered for a given tournament (any category).
     */
    @Query("SELECT COUNT(r) > 0 FROM Registration r WHERE r.tournamentId = :tournamentId AND r.player.id = :playerId")
    boolean existsByTournamentIdAndPlayerId(
            @Param("tournamentId") Long tournamentId,
            @Param("playerId") Long playerId
    );

    /**
     * Duplicate check for singles: player + tournament + category.
     * Returns the first match — a player should only have one record per category.
     */
    @Query("SELECT r FROM Registration r WHERE r.tournamentId = :tournamentId AND r.player = :player AND r.category = :category ORDER BY r.id ASC")
    List<Registration> findAllByTournamentIdAndPlayerAndCategory(
            @Param("tournamentId") Long tournamentId,
            @Param("player") Users player,
            @Param("category") String category
    );

    /**
     * Duplicate check: team + tournament + category.
     * A team should only register once per category, but returns list to be safe.
     */
    @Query("SELECT r FROM Registration r WHERE r.tournamentId = :tournamentId AND r.team = :team AND r.category = :category ORDER BY r.id ASC")
    List<Registration> findAllByTournamentIdAndTeamAndCategory(
            @Param("tournamentId") Long tournamentId,
            @Param("team") Teams team,
            @Param("category") String category
    );

    /**
     * Fetch every registration row for a given tournament (for export).
     */
    @Query("SELECT r FROM Registration r LEFT JOIN FETCH r.player LEFT JOIN FETCH r.team WHERE r.tournamentId = :tournamentId ORDER BY r.registeredAt ASC")
    List<Registration> findAllByTournamentId(@Param("tournamentId") Long tournamentId);

    /** Count total registrations for a tournament — used for capacity check. */
    long countByTournamentId(Long tournamentId);

    /**
     * Find all registrations for a team in a tournament (any category).
     * A team should only be in one doubles category per tournament.
     */
    @Query("SELECT r FROM Registration r WHERE r.tournamentId = :tournamentId AND r.team = :team ORDER BY r.id ASC")
    List<Registration> findAllByTournamentIdAndTeam(
            @Param("tournamentId") Long tournamentId,
            @Param("team") Teams team
    );

    /**
     * Finds all doubles registrations for a player in a tournament (any team).
     * Used to enforce: a player can only be in ONE doubles team per tournament.
     */
    @Query("""
        SELECT r
        FROM Registration r
        JOIN r.team t
        WHERE r.tournamentId = :tournamentId
          AND r.team IS NOT NULL
          AND (
                t.playerOne.id = :playerId
             OR t.playerTwo.id = :playerId
          )
        ORDER BY r.id ASC
    """)
    List<Registration> findAllDoublesRegistrationsByPlayer(
            @Param("tournamentId") Long tournamentId,
            @Param("playerId") Long playerId
    );

    /**
     * Finds all doubles registrations for a player, excluding a specific team.
     * Used when checking if a player is already in a DIFFERENT doubles team.
     *
     * @param excludeTeamId the ID of the team to skip (pass -1L to skip nothing)
     */
    @Query("""
        SELECT r
        FROM Registration r
        JOIN r.team t
        WHERE r.tournamentId = :tournamentId
          AND r.team IS NOT NULL
          AND t.teamId <> :excludeTeamId
          AND (
                t.playerOne.id = :playerId
             OR t.playerTwo.id = :playerId
          )
        ORDER BY r.id ASC
    """)
    List<Registration> findAllDoublesRegistrationsByPlayerExcludingTeam(
            @Param("tournamentId") Long tournamentId,
            @Param("playerId") Long playerId,
            @Param("excludeTeamId") Long excludeTeamId
    );
}
