package com.ktsa.foosball.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "player_seq_gen")
    @SequenceGenerator(
            name = "player_seq_gen",
            sequenceName = "player_seq",
            allocationSize = 1
    )
    private Long id;

    //personal details

    private String userName;

    private String password;

    @Column(nullable = false)
    private String firstName;

    private String lastName;

    @Column(unique = true,nullable = false)
    private String email;

    @Column(unique = true, length = 10)
    private Long phoneNumber;

    @Enumerated(EnumType.STRING)
    private Gender gender;//male,female,other

//    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private String city;

    private String state;

    @Enumerated(EnumType.STRING)
    private UserStatus status;//active,inactive,banned

    @Enumerated(EnumType.STRING)
    private Role role;//admin,referee,player

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
