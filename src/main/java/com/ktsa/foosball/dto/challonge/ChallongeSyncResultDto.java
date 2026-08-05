package com.ktsa.foosball.dto.challonge;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Summary returned after a Challonge sync operation.
 */
@Data
@AllArgsConstructor
public class ChallongeSyncResultDto {

    /** Total matches received from Challonge */
    private int totalFromChallonge;

    /** Matches created in the local database (new) */
    private int created;

    /** Matches updated in the local database (score / winner / status changed) */
    private int updated;

    /** Challonge participant names that had no matching local user (by username) */
    private List<String> unmatchedParticipants;
}
