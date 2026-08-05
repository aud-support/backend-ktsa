package com.ktsa.foosball.dto.challonge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Top-level wrapper that Challonge returns for each element in the matches array.
 * [ { "match": { ... } }, { "match": { ... } } ]
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChallongeMatchWrapperDto {

    @JsonProperty("match")
    private ChallongeMatchDto match;
}
