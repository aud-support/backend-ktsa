package com.ktsa.foosball.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name="matches")
public class Matches {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String stage;
    private LocalDateTime scheduledAt;
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

    /** Challonge match ID — used to avoid duplicate sync inserts */
    @Column(name = "challonge_match_id")
    private Long challongeMatchId;

    /**
     * Match category — one of: MENS_SINGLES, WOMENS_SINGLES, OPEN_DOUBLES, MIXED_DOUBLES.
     * Set explicitly when a match is created so rankings filter by category directly
     * instead of inferring from player/team gender.
     */
    private String category;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
