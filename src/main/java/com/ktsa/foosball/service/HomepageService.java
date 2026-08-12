package com.ktsa.foosball.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktsa.foosball.dto.ArticleDto;
import com.ktsa.foosball.dto.HomepageContentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HomepageService {

    private final S3Service s3Service;
    private final ObjectMapper objectMapper;

    @Value("${aws.s3.buckets.documents}")
    private String bucket;

    private static final String HOMEPAGE_KEY  = "homepage/content.json";
    private static final String ARTICLES_KEY  = "homepage/articles.json";

    // ──────────────────────────────────────────────────────────────
    // Homepage content (hero / videos)
    // ──────────────────────────────────────────────────────────────

    public void saveHomepageContent(HomepageContentDto dto, MultipartFile image) {

        if (image != null && !image.isEmpty()) {
            String imageUrl = s3Service.uploadFile(image, bucket, "homepage");
            dto.setHeroBannerUrl(imageUrl);
        } else {
            Map<String, Object> existing = getHomepageContent();
            if (existing != null && existing.get("heroBannerUrl") != null) {
                dto.setHeroBannerUrl(existing.get("heroBannerUrl").toString());
            }
        }

        s3Service.uploadJson(dto, bucket, HOMEPAGE_KEY);
    }

    public Map<String, Object> getHomepageContent() {
        return s3Service.readJson(bucket, HOMEPAGE_KEY);
    }

    // ──────────────────────────────────────────────────────────────
    // Articles
    // ──────────────────────────────────────────────────────────────

    /** Read all articles from S3; returns empty list if none saved yet. */
    public List<ArticleDto> getArticles() {
        try {
            Map<String, Object> raw = s3Service.readJson(bucket, ARTICLES_KEY);
            // The JSON is stored as {"articles": [...]}
            Object list = raw.get("articles");
            if (list == null) return new ArrayList<>();
            String json = objectMapper.writeValueAsString(list);
            return objectMapper.readValue(json, new TypeReference<List<ArticleDto>>() {});
        } catch (NoSuchKeyException e) {
            // File doesn't exist yet — first run
            return new ArrayList<>();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read articles from S3", e);
        }
    }

    /** Persist the full article list to S3. */
    private void saveArticles(List<ArticleDto> articles) {
        Map<String, Object> wrapper = Map.of("articles", articles);
        s3Service.uploadJson(wrapper, bucket, ARTICLES_KEY);
    }

    public ArticleDto createArticle(ArticleDto dto) {
        dto.setId(UUID.randomUUID().toString());
        List<ArticleDto> articles = getArticles();
        articles.add(dto);
        saveArticles(articles);
        return dto;
    }

    public ArticleDto updateArticle(String id, ArticleDto dto) {
        List<ArticleDto> articles = getArticles();
        boolean found = false;
        for (int i = 0; i < articles.size(); i++) {
            if (articles.get(i).getId().equals(id)) {
                dto.setId(id); // keep the same id
                articles.set(i, dto);
                found = true;
                break;
            }
        }
        if (!found) {
            throw new RuntimeException("Article not found: " + id);
        }
        saveArticles(articles);
        return dto;
    }

    public void deleteArticle(String id) {
        List<ArticleDto> articles = getArticles();
        boolean removed = articles.removeIf(a -> a.getId().equals(id));
        if (!removed) {
            throw new RuntimeException("Article not found: " + id);
        }
        saveArticles(articles);
    }
}
