package com.ktsa.foosball.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "registrations")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Registration {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "player_seq_gen")
    @SequenceGenerator(
            name = "player_seq_gen",
            sequenceName = "player_seq",
            allocationSize = 1
    )
    private Long id;

    private Long tournamentId;
    private String category; // Single, double, mix
    @ManyToOne
    @JoinColumn(name = "team_id")
    private Teams team;
    @ManyToOne
    @JoinColumn(name = "player_id")
    private Users player;
    private String status;
    private LocalDateTime registeredAt;

    private String partnerPreference; // Defender, Attacker, All-rounder — set when player needs a partner

    @PrePersist
    public void prePersist() {
        this.registeredAt = LocalDateTime.now();
    }


}
