package com.ktsa.foosball.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "team")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Teams {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long teamId;

    private String teamName;//player1_name & player2_name

    @ManyToOne
    private Users playerOne;

    @ManyToOne
    private Users playerTwo;

}
