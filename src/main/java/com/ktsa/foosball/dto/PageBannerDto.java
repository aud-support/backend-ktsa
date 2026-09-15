package com.ktsa.foosball.dto;

import lombok.Data;

@Data
public class PageBannerDto {
    private String page;          // "news" | "services"
    private String heroBannerUrl;
    private String heroTitle;
    private String heroSubtitle;
}
