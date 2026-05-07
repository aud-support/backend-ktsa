package com.ktsa.foosball.mapper;

import com.ktsa.foosball.dto.TournamentRequestDTO;
import com.ktsa.foosball.dto.TournamentResponseDTO;
import com.ktsa.foosball.model.Tournaments;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Component
public class TournamentMapper {

    public Tournaments toEntity(TournamentRequestDTO dto) {
        Tournaments tournament = new Tournaments();
        tournament.setTournamentName(dto.getTournamentName());
        tournament.setDescription(dto.getDescription());
        tournament.setFormat(dto.getFormat());
        tournament.setStatus(dto.getStatus());
        tournament.setStartDate(dto.getStartDate());
        tournament.setEndDate(dto.getEndDate());
        tournament.setVenue(dto.getVenue());
        tournament.setPricePool(dto.getPricePool());
        tournament.setMaxParticipants(dto.getMaxParticipants());
        tournament.setCreatedAt(LocalDateTime.now());
        return tournament;
    }

    public TournamentResponseDTO toDTO(Tournaments tournament) {
        TournamentResponseDTO dto = new TournamentResponseDTO();
        dto.setTournamentName(tournament.getTournamentName());
        dto.setFormat(tournament.getFormat());
        dto.setDescription(tournament.getDescription());
        dto.setVenue(tournament.getVenue());
        dto.setStatus(tournament.getStatus());
        dto.setPricePool(tournament.getPricePool());
        dto.setStartDate(LocalDate.now());
        return dto;
    }
}
