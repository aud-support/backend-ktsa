package com.ktsa.foosball.repository;

import com.ktsa.foosball.model.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MediaRepository extends JpaRepository<Media, Long> {
    Optional<Media> findByTournamentIdAndLabel(Long id, String banner);
}
