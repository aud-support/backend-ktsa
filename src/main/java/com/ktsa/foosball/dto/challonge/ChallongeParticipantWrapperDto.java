package com.ktsa.foosball.dto.challonge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Top-level wrapper that Challonge returns for each element in the participants array.
 * [ { "participant": { ... } }, { "participant": { ... } } ]
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChallongeParticipantWrapperDto {

    @JsonProperty("participant")
    private ChallongeParticipantDto participant;
}
