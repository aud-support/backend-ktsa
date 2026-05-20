package com.ktsa.foosball.service;

import com.ktsa.foosball.dto.ApiResponse;
import com.ktsa.foosball.dto.TournamentRequestDTO;
import com.ktsa.foosball.dto.TournamentResponseDTO;

import com.ktsa.foosball.exception.ResourceNotFoundException;
import com.ktsa.foosball.model.Teams;
import com.ktsa.foosball.model.Tournaments;
import com.ktsa.foosball.repository.TournamentRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final ModelMapper modelMapper;


    public ResponseEntity<ApiResponse<?>> createTournament(TournamentRequestDTO dto) {

        Tournaments tournament = modelMapper.map(dto, Tournaments.class);
        Tournaments createdTournament = tournamentRepository.save(tournament);
        return ResponseEntity.ok(
                ApiResponse.success(200, "Team created successfully", modelMapper.map(createdTournament, TournamentResponseDTO.class)));

    }

//    public List<TournamentResponseDTO> getAllTournaments() {
//        return tournamentRepository.findAll().stream()
//                .map(tournamentMapper::toDTO)
//                .collect(Collectors.toList());
//    }
//
//    public Tournaments getTournamentById(Long tournamentId) {
//        return tournamentRepository.findById(tournamentId)
//                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id: " + tournamentId));
//    }
}
