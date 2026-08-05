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

    /** Custom display name — may be anything the admin or user enters. */
    private String teamName;

    /**
     * Internal identifier used for Challonge sync and Excel export.
     * Always auto-generated as "player1_userName & player2_userName".
     * Never editable by the user; always derived from the two players.
     */
    @Column(name = "challonge_team_name")
    private String challongeTeamName;

    @ManyToOne
    private Users playerOne;

    @ManyToOne
    private Users playerTwo;

    /**
     * Rebuilds challongeTeamName from playerOne.userName & playerTwo.userName.
     * Called automatically before every insert and update.
     */
    @PrePersist
    @PreUpdate
    public void syncChallongeTeamName() {
        if (playerOne != null && playerTwo != null
                && playerOne.getUserName() != null && playerTwo.getUserName() != null) {
            this.challongeTeamName = playerOne.getUserName() + " & " + playerTwo.getUserName();
        }
    }
}
