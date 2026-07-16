package com.ktsa.foosball.dto;

import com.ktsa.foosball.model.Format;
import com.ktsa.foosball.model.TournamentStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TournamentResponseDTO {

    private Long id;
    private String tournamentName;
    private String description;
    private Format format;
    private TournamentStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String bannerUrl;
    @NotBlank
    private String venue;
    private Double pricePool;
    private Integer maxParticipants;

    private Boolean openSingleEnabled;
    private Double openSingleFee;

    private Boolean openDoubleEnabled;
    private Double openDoubleFee;

    private Boolean mixedDoubleEnabled;
    private Double mixedDoubleFee;

    private Boolean womenSingleEnabled;
    private Double womenSingleFee;

    private Boolean registrationClosed;
}
