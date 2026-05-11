package com.ktsa.foosball.service;

import com.ktsa.foosball.dto.TournamentRequestDTO;
import com.ktsa.foosball.dto.TournamentResponseDTO;

import com.ktsa.foosball.exception.ResourceNotFoundException;
import com.ktsa.foosball.mapper.TournamentMapper;
import com.ktsa.foosball.model.Tournaments;
import com.ktsa.foosball.repository.TournamentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TournamentService {

    private TournamentRepository tournamentRepository;
    private TournamentMapper tournamentMapper;

    public TournamentResponseDTO createTournament(TournamentRequestDTO dto) {
        Tournaments saved = tournamentRepository.save(tournamentMapper.toEntity(dto));
        return tournamentMapper.toDTO(saved);
    }

    public List<TournamentResponseDTO> getAllTournaments() {
        return tournamentRepository.findAll().stream()
                .map(tournamentMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Tournaments getTournamentById(Long tournamentId) {
        return tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id: " + tournamentId));
    }
}
