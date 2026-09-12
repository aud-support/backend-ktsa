package com.ktsa.foosball.dto;

import com.ktsa.foosball.model.Format;
import com.ktsa.foosball.model.TournamentStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TournamentRequestDTO {

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
    private String openSingleChallongeUrl;

    private Boolean openDoubleEnabled;
    private Double openDoubleFee;
    private String openDoubleChallongeUrl;

    private Boolean mixedDoubleEnabled;
    private Double mixedDoubleFee;
    private String mixedDoubleChallongeUrl;

    private Boolean womenSingleEnabled;
    private Double womenSingleFee;
    private String womenSingleChallongeUrl;

    private Boolean mensSingleEnabled;
    private Double mensSingleFee;
    private String mensSingleChallongeUrl;

    private Boolean underSixteenEnabled;
    private Double underSixteenFee;
    private String underSixteenChallongeUrl;

    private Boolean aboveSixteenEnabled;
    private Double aboveSixteenFee;
    private String aboveSixteenChallongeUrl;

    private Boolean registrationClosed;

    private String challongeUrl;

}
