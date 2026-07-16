package com.ktsa.foosball.scheduler;


import com.ktsa.foosball.model.Tournaments;
import com.ktsa.foosball.model.TournamentStatus;
import com.ktsa.foosball.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TournamentStatusScheduler {

    private final TournamentRepository tournamentRepository;

    @Scheduled(fixedRate = 60000) // Every minute
    public void updateTournamentStatus() {

        LocalDateTime now = LocalDateTime.now();

        List<Tournaments> tournaments = tournamentRepository.findAll();

        boolean updated = false;

        for (Tournaments tournament : tournaments) {

            TournamentStatus newStatus;

            if (now.isBefore(tournament.getStartDate())) {

                newStatus = TournamentStatus.UPCOMING;

            } else if (now.isAfter(tournament.getEndDate())) {

                newStatus = TournamentStatus.COMPLETED;

            } else {

                newStatus = TournamentStatus.ACTIVE;
            }

            if (tournament.getStatus() != newStatus) {

                tournament.setStatus(newStatus);
                updated = true;
            }
        }

        if (updated) {
            tournamentRepository.saveAll(tournaments);
        }
    }
}