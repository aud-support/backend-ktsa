package com.ktsa.foosball.dto;

import lombok.Data;

import java.util.List;

@Data
public class FooterSocialDto {

    // Organisation description shown in footer
    private String description;

    // Social media links
    private String facebook;
    private String linkedin;
    private String instagram;
    private String youtube;

    // Contact info
    private String email;
    private String phone;
    private String address;

    // Quick links
    private List<QuickLink> quickLinks;

    // Copyright line
    private String copyright;

    // ── Nested type ───────────────────────────────────────────────────────────

    @Data
    public static class QuickLink {
        private String label;
        private String path;
    }
}
