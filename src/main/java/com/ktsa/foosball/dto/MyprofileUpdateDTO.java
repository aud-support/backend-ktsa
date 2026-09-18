package com.ktsa.foosball.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class MyprofileUpdateDTO {

    private String name;
    private Long phoneNumber;
    private String state;
    private String city;
    private LocalDate dateOfBirth;
}
