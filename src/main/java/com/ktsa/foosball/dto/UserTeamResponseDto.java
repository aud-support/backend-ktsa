package com.ktsa.foosball.dto;

import lombok.Data;

import java.util.List;

/**
 * Team summary for a specific user's profile view.
 * Includes partner info and list of tournaments the team is registered for.
 */
@Data
public class UserTeamResponseDto {

    private Long id;
    private String teamName;

    /** The user's role in the team — "playerOne" or "playerTwo" */
    private String userRole;

    /** Partner details (null for solo registrations) */
    private PartnerDto partner;

    /** All tournament registrations this team is in */
    private List<TeamTournamentDto> tournaments;

    @Data
    public static class PartnerDto {
        private Long id;
        private String name;
        private String email;
    }

    @Data
    public static class TeamTournamentDto {
        private Long tournamentId;
        private String tournamentName;
        private String category;
        /** Derived from tournament status: "Upcoming", "Live", or "Completed" */
        private String status;
        private String startDate;
    }
}
