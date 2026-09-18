package com.ktsa.foosball.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name="tournaments")
public class Tournaments {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String tournamentName;

    private String description;

    @Enumerated(EnumType.STRING)
    private Format format;

    @Enumerated(EnumType.STRING)
    private TournamentStatus status;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private LocalDateTime createdAt;

    private String venue;

    private Double pricePool;

    private Integer maxParticipants;

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Media> media = new ArrayList<>();

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

    // ── New categories ─────────────────────────────────────────────────────
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

    /** Challonge tournament URL slug — used to sync bracket data */
    private String challongeUrl;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
