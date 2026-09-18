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

    private Boolean beginnerDoubleEnabled;
    private Double beginnerDoubleFee;
    private String beginnerDoubleChallongeUrl;

    private Boolean womensDoubleEnabled;
    private Double womensDoubleFee;
    private String womensDoubleChallongeUrl;

    private Boolean mensDoubleEnabled;
    private Double mensDoubleFee;
    private String mensDoubleChallongeUrl;

    private Boolean juniorU16DoubleEnabled;
    private Double juniorU16DoubleFee;
    private String juniorU16DoubleChallongeUrl;

    private Boolean juniorAbove16SingleEnabled;
    private Double juniorAbove16SingleFee;
    private String juniorAbove16SingleChallongeUrl;

    private Boolean juniorAbove16DoubleEnabled;
    private Double juniorAbove16DoubleFee;
    private String juniorAbove16DoubleChallongeUrl;

    private Boolean seniorDoubleEnabled;
    private Double seniorDoubleFee;
    private String seniorDoubleChallongeUrl;

    private Boolean disabledSingleEnabled;
    private Double disabledSingleFee;
    private String disabledSingleChallongeUrl;

    private Boolean disabledDoubleEnabled;
    private Double disabledDoubleFee;
    private String disabledDoubleChallongeUrl;

    private Boolean disabledMixedEnabled;
    private Double disabledMixedFee;
    private String disabledMixedChallongeUrl;

    private Boolean monsterDypEnabled;
    private Double monsterDypFee;
    private String monsterDypChallongeUrl;

    private Boolean teamEventEnabled;
    private Double teamEventFee;
    private String teamEventChallongeUrl;

    private Boolean registrationClosed;

    private String challongeUrl;

}
