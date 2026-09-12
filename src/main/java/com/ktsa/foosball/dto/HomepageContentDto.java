package com.ktsa.foosball.dto;

import lombok.Data;

import java.util.List;

@Data
public class HomepageContentDto {
    private String heroTitle;
    private String heroSubtitle;
    private String heroDescription;
    private String heroBannerUrl;

    private List<String> videoUrls;
    // add whatever fields your homepage needs
}