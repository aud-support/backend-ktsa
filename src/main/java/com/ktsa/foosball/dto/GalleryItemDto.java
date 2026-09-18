package com.ktsa.foosball.dto;

import lombok.Data;

@Data
public class GalleryItemDto {
    private String id;
    private String imageUrl;
    private String caption;
    private String year;
    private int displayOrder;
}
