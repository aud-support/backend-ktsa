package com.ktsa.foosball.scheduler;

import com.ktsa.foosball.model.Tournaments;
import com.ktsa.foosball.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Automatically closes tournament registration once the tournament's
 * start date/time has been reached.
 *
 * Runs every minute. Only updates rows that are not yet closed,
 * so it is a no-op most of the time.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegistrationAutoCloseScheduler {

    private final TournamentRepository tournamentRepository;

    /**
     * Cron: every minute at second 0.
     * Finds tournaments where startDate <= now AND registrationClosed is not true,
     * then flips registrationClosed = true for each one.
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void autoCloseExpiredRegistrations() {
        LocalDateTime now = LocalDateTime.now();
        List<Tournaments> toClose = tournamentRepository.findTournamentsToAutoClose(now);

        if (toClose.isEmpty()) return;

        for (Tournaments tournament : toClose) {
            tournament.setRegistrationClosed(true);
            log.info("[AutoClose] Registration closed for tournament '{}' (id={}) — startDate was {}",
                    tournament.getTournamentName(), tournament.getId(), tournament.getStartDate());
        }

        tournamentRepository.saveAll(toClose);
        log.info("[AutoClose] Auto-closed {} tournament(s) at {}", toClose.size(), now);
    }
}
