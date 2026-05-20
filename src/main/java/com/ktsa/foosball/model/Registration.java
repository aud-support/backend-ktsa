package com.ktsa.foosball.model;


import jakarta.persistence.*;
import lombok.*;

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
    @OneToOne
    private Teams team;
    @OneToOne
    private Users player;
    private String status;
    private String registeredAt;


}
