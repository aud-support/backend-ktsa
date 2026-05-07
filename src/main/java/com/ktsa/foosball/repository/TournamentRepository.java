package com.ktsa.foosball.repository;

import com.ktsa.foosball.model.Tournaments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentRepository  extends JpaRepository<Tournaments,Long> {

}
