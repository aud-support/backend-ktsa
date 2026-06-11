package com.ktsa.foosball.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Format {
    SINGLE_ELIMINATION,
    ROUND_ROBIN,
    DOUBLE_ELIMINATION,
    LEAGUE,
    SWISS_SYSTEM;


}
