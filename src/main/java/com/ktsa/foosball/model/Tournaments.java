package com.ktsa.foosball.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
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

    @NotNull(message = "Date is required")
    private LocalDateTime startDate;

    @NotNull(message = "Date is required")
    private LocalDateTime endDate;
    private LocalDateTime createdAt;

    @NotNull(message = "Venue is required")
    private String venue;

    private Double pricePool;

    private Integer maxParticipants;

    // Add inside Tournaments.java
    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Media> media = new ArrayList<>();



    private Boolean openSingleEnabled;
    private Double openSingleFee;

    private Boolean openDoubleEnabled;
    private Double openDoubleFee;

    private Boolean mixedDoubleEnabled;
    private Double mixedDoubleFee;

    private Boolean womenSingleEnabled;
    private Double womenSingleFee;



    @OneToMany
    List<Users> players= new ArrayList<>();

    @OneToMany
    List<Teams> teams = new ArrayList<>();


    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
