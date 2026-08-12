package com.ktsa.foosball.dto;

import lombok.Data;
import java.util.List;

@Data
public class ServiceDto {
    private String id;
    private String name;
    private String description;
    private String icon;
    private List<String> features;
    private String pricing;
    private String imageUrl;
    private int displayOrder;
}
