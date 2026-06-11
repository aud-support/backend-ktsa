package com.ktsa.foosball.repository;

import com.ktsa.foosball.model.Teams;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface TeamsRepository extends JpaRepository<Teams, Long> {
    Collection<Object> findByPlayerOneId(Long id);

    Optional<Teams> findByPlayerOneIdAndPlayerTwoId(Long id, Long id1);
}
