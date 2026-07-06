package com.ktsa.foosball.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerSearchDto {

    private Long id;
    private String name;
    private String email;
}
