package com.ktsa.foosball.dto;

import lombok.Data;

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
}
