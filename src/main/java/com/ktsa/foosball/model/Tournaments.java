package com.ktsa.foosball.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
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
    private LocalDate startDate;

    @NotNull(message = "Date is required")
    private LocalDate endDate;

    private LocalDateTime createdAt;

    @NotNull(message = "Venue is required")
    private String venue;

    private Double pricePool;

    private Integer maxParticipants;

    @OneToMany
    List<Users> players= new ArrayList<>();

    @OneToMany
    List<Teams> teams = new ArrayList<>();
}
