package com.ktsa.foosball.dto.challonge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Maps the inner "participant" object from the Challonge participants API.
 *
 * GET https://api.challonge.com/v1/tournaments/{url}/participants.json
 * Returns: [ { "participant": { "id": ..., "name": ..., "challonge_username": ... } }, ... ]
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChallongeParticipantDto {

    /** Challonge participant ID — used to resolve player1_id / player2_id in matches */
    @JsonProperty("id")
    private Long id;

    /**
     * Display name entered when adding the participant.
     * We try to match this against Users.userName in our database.
     */
    @JsonProperty("name")
    private String name;

    /** Challonge account username, when participant is a registered Challonge user */
    @JsonProperty("challonge_username")
    private String challongeUsername;

    /** Convenience: returns challongeUsername if present, otherwise falls back to name */
    public String resolvedUsername() {
        return (challongeUsername != null && !challongeUsername.isBlank())
                ? challongeUsername
                : name;
    }
}
