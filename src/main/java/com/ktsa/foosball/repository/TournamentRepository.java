package com.ktsa.foosball.repository;

import com.ktsa.foosball.model.Tournaments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TournamentRepository extends JpaRepository<Tournaments, Long> {

    /**
     * Filter by a date range — portable JPQL, works on PostgreSQL.
     * The service builds: start of month → start of next month,
     * so no DB-specific date functions are needed.
     *
     * June 2026  → from=2026-06-01T00:00, to=2026-07-01T00:00
     * Year 2026  → from=2026-01-01T00:00, to=2027-01-01T00:00
     */
    @Query("SELECT t FROM Tournaments t WHERE t.startDate >= :from AND t.startDate < :to ORDER BY t.startDate ASC")
    Page<Tournaments> findByDateRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    /**
     * Returns distinct years from start_date.
     * Uses native PostgreSQL SQL — EXTRACT returns double precision in PG,
     * so we use DATE_PART which also returns float8, then cast to int.
     */
    @Query(
        value = "SELECT DISTINCT CAST(DATE_PART('year', start_date) AS INTEGER) AS yr " +
                "FROM tournaments " +
                "WHERE start_date IS NOT NULL " +
                "ORDER BY yr ASC",
        nativeQuery = true
    )
    List<Integer> findDistinctYears();


    @Query("""
    SELECT t
    FROM Tournaments t
    WHERE t.startDate >= CURRENT_TIMESTAMP
    ORDER BY t.startDate ASC
""")
    Page<Tournaments> findUpcomingTournaments(Pageable pageable);



    Page<Tournaments> findByStartDateGreaterThanEqual(
            LocalDateTime startDate,
            Pageable pageable
    );





    @Query("""
SELECT t
FROM Tournaments t
ORDER BY
    CASE
        WHEN t.status = 'ACTIVE' THEN 0
        WHEN t.status = 'UPCOMING' THEN 1
        WHEN t.status = 'COMPLETED' THEN 2
        ELSE 3
    END,
    CASE
        WHEN t.status = 'UPCOMING' THEN t.startDate
    END ASC,
    CASE
        WHEN t.status = 'COMPLETED' THEN t.endDate
    END DESC
""")
    Page<Tournaments> findAllOrdered(Pageable pageable);
}
