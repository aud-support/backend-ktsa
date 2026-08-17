package com.ktsa.foosball.dto;

import lombok.Data;

@Data
public class SponsorDto {
    private String id;       // UUID assigned on create
    private String name;     // Sponsor / partner name
    private String imageUrl; // S3 URL of the logo image
}
