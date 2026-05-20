package com.ktsa.foosball.dto;

import com.ktsa.foosball.model.Format;
import com.ktsa.foosball.model.TournamentStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TournamentRequestDTO {

    private String tournamentName;
    private String description;
    private Format format;
    private TournamentStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    @NotBlank
    private String venue;
    private Double pricePool;
    private Integer maxParticipants;
}
