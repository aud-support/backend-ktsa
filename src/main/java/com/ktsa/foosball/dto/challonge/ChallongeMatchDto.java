package com.ktsa.foosball.dto.challonge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Maps the inner "match" object from the Challonge matches API.
 *
 * GET https://api.challonge.com/v1/tournaments/{url}/matches.json
 * Returns: [ { "match": { "id": ..., "state": "complete", "player1_id": ..., ... } }, ... ]
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChallongeMatchDto {

    @JsonProperty("id")
    private Long id;

    /**
     * Match state from Challonge: "complete", "open", "pending".
     * Mapped to local status: COMPLETED / IN_PROGRESS / SCHEDULED.
     */
    @JsonProperty("state")
    private String state;

    @JsonProperty("player1_id")
    private Long player1Id;

    @JsonProperty("player2_id")
    private Long player2Id;

    @JsonProperty("winner_id")
    private Long winnerId;

    /** Round number (1-indexed) */
    @JsonProperty("round")
    private Integer round;

    /**
     * Scores in "p1score-p2score" format (e.g. "10-6").
     * May contain multiple sets separated by commas: "6-4,4-6,7-5".
     * We store only the first set as teamOneScore / teamTwoScore.
     */
    @JsonProperty("scores_csv")
    private String scoresCsv;

    /** Returns "COMPLETED", "IN_PROGRESS", or "SCHEDULED" based on Challonge state */
    public String toLocalStatus() {
        if (state == null) return "SCHEDULED";
        return switch (state.toLowerCase()) {
            case "complete" -> "COMPLETED";
            case "open"     -> "IN_PROGRESS";
            default         -> "SCHEDULED";
        };
    }

    /** Parses the first score pair and returns player-1's score, or null if missing */
    public Long parsePlayer1Score() {
        return parseScore(0);
    }

    /** Parses the first score pair and returns player-2's score, or null if missing */
    public Long parsePlayer2Score() {
        return parseScore(1);
    }

    private Long parseScore(int index) {
        if (scoresCsv == null || scoresCsv.isBlank()) return null;
        try {
            String firstSet = scoresCsv.split(",")[0].trim();
            String[] parts = firstSet.split("-");
            return Long.parseLong(parts[index].trim());
        } catch (Exception e) {
            return null;
        }
    }
}
