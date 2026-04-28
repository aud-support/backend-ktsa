package com.ktsa.foosball.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "player")
@Data
public class player {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "player_seq_gen")
    @SequenceGenerator(
            name = "player_seq_gen",
            sequenceName = "player_seq",
            allocationSize = 1
    )
    private Long id;

    //personal details

    @Column(nullable = false)
    private String name;

    @Column(unique = true,nullable = false)
    private String email;

    @Column(unique = true, length = 10,nullable = false)
    private Long phoneNumber;

    @Enumerated(EnumType.STRING)
    private Gender gender;//male,female,other

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private String city;

    private String state;

    //player details with respect to game

    private Integer points;

    private Integer wins;

    private LocalDateTime joiningDateTime;

    @Enumerated(EnumType.STRING)
    private PlayerStatus status;//active,inactive,banned
}
