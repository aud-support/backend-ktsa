package com.ktsa.foosball.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name="matches")
public class Matches {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String stage;
    private String scheduledAt;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime createdAt;
    @ManyToOne
    private Users playerOne;
    @ManyToOne
    private Users playerTwo;

    @ManyToOne
    private Teams teamOne;
    @ManyToOne
    private Teams teamTwo;

    private Long teamOneScore;
    private Long teamTwoScore;
    @ManyToOne
    private Teams winnerTeam;
    @ManyToOne
    private Users winnerPlayer;

    @ManyToOne
    private Tournaments tournament;
    private Integer roundNumber;
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
