package com.ktsa.foosball.service;

import com.ktsa.foosball.model.Tournaments;
import com.ktsa.foosball.model.Users;
import com.ktsa.foosball.repository.TournamentRepository;
import com.ktsa.foosball.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RegistrationService {

    private TournamentRepository tournamentRepository;
    private UserRepository userRepository;

    public String registerPlayer(Long playerId, Long tournamentId) {

        Tournaments tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + tournamentId));

        Users player = userRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found with id: " + playerId));

        // Check if player is already registered
        if (tournament.getPlayers().contains(player)) {
            throw new RuntimeException("Player is already registered in this tournament");
        }

        // Check max participants
        if (tournament.getMaxParticipants() != null &&
                tournament.getPlayers().size() >= tournament.getMaxParticipants()) {
            throw new RuntimeException("Tournament is full. Max participants: " + tournament.getMaxParticipants());
        }

        tournament.getPlayers().add(player);
        tournamentRepository.save(tournament);

        return "Player registered successfully";
    }
}
