package com.ktsa.foosball.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "team")
@Data
@AllArgsConstructor
public class Teams {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long teamId;

    private String teamName;//player1_name & player2_name

    @OneToOne
    private Users playerOne;

    @OneToOne
    private Users PlayerTwo;

    @ManyToOne
    private Tournaments tournament;
}
