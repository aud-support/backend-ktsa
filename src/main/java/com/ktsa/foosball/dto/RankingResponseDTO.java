package com.ktsa.foosball.dto;

import com.ktsa.foosball.model.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RankingResponseDTO {
    private Long id;
    private int points;
    private int wins;
    private int losses;
    private int matches;
    private String userName;
    private String email;
    private Gender gender;

    /**
     * One of: "MENS_SINGLES", "WOMENS_SINGLES", "OPEN_DOUBLES", "MIXED_DOUBLES"
     * For doubles entries, userName contains "Player1 & Player2" and teamId is populated.
     */
    private String category;

    /**
     * Only set for doubles entries — the team identifier
     */
    private Long teamId;

    /**
     * Profile picture URL — set for spotlight / top-player display
     */
    private String profilePictureUrl;
}