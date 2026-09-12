package com.ktsa.foosball.dto;

import lombok.Data;
import java.util.List;

@Data
public class ArticleDto {
    private String id;
    private String title;
    private String excerpt;
    private String content;
    private String author;
    private String publishedDate;
    private String imageUrl;
    private String category;
    private boolean featured;

    /** Named hyperlinks attached to the article. Each entry: {label, url} */
    private List<ArticleLink> links;

    @Data
    public static class ArticleLink {
        private String label;
        private String url;
    }
}
