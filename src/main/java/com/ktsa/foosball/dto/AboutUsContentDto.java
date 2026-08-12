package com.ktsa.foosball.dto;

import lombok.Data;

import java.util.List;

@Data
public class AboutUsContentDto {

    // The Story Behind KTSA
    private String storyContent;
    private String rulebookUrl;

    // Stats
    private String totalPlayers;
    private String tournaments;
    private String activePlayers;
    private String clubs;

    // Founder card
    private String founderName;
    private String founderRole;
    private String founderQuote;
    private String founderStory;

    // Who We Are
    private String whoWeAreContent;
    private String whoWeAreImageUrl;

    // Vision & Mission
    private String visionText;
    private String missionText;

    // Journey Timeline
    private List<TimelineEntry> timeline;

    // What KTSA Provides
    private List<ProvidesItem> provides;

    // Achievements
    private List<Achievement> achievements;

    // ── Nested types ──────────────────────────────────────────────────────────

    @Data
    public static class TimelineEntry {
        private String year;
        private String title;
        private String description;
    }

    @Data
    public static class ProvidesItem {
        private String title;
        private String desc;
    }

    @Data
    public static class Achievement {
        private String title;
        private String description;
    }
}
