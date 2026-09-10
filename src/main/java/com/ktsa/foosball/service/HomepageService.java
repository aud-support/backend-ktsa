package com.ktsa.foosball.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktsa.foosball.dto.ArticleDto;
import com.ktsa.foosball.dto.HomepageContentDto;
import com.ktsa.foosball.dto.ServiceDto;
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
    private static final String SERVICES_KEY  = "homepage/services.json";

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
            // Read raw JSON bytes from S3 and deserialize directly into typed list
            // Avoids the Map → List double-hop that can lose inner class type info
            String json = s3Service.readJsonAsString(bucket, ARTICLES_KEY);
            if (json == null || json.isBlank()) return new ArrayList<>();

            // JSON is stored as {"articles": [...]}
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(json);
            com.fasterxml.jackson.databind.JsonNode articlesNode = root.get("articles");
            if (articlesNode == null || articlesNode.isNull()) return new ArrayList<>();

            return objectMapper.readValue(
                    articlesNode.toString(),
                    new TypeReference<List<ArticleDto>>() {}
            );
        } catch (software.amazon.awssdk.services.s3.model.NoSuchKeyException e) {
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

    // ──────────────────────────────────────────────────────────────
    // Services
    // ──────────────────────────────────────────────────────────────

    /** Read all services from S3; returns empty list if none saved yet. */
    public List<ServiceDto> getServices() {
        try {
            String json = s3Service.readJsonAsString(bucket, SERVICES_KEY);
            if (json == null || json.isBlank()) return new ArrayList<>();

            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(json);
            com.fasterxml.jackson.databind.JsonNode servicesNode = root.get("services");
            if (servicesNode == null || servicesNode.isNull()) return new ArrayList<>();

            return objectMapper.readValue(
                    servicesNode.toString(),
                    new TypeReference<List<ServiceDto>>() {}
            );
        } catch (software.amazon.awssdk.services.s3.model.NoSuchKeyException e) {
            return new ArrayList<>();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read services from S3", e);
        }
    }

    /** Persist the full service list to S3. */
    private void saveServices(List<ServiceDto> services) {
        Map<String, Object> wrapper = Map.of("services", services);
        s3Service.uploadJson(wrapper, bucket, SERVICES_KEY);
    }

    public ServiceDto createService(ServiceDto dto) {
        dto.setId(UUID.randomUUID().toString());
        List<ServiceDto> services = getServices();
        dto.setDisplayOrder(services.size()); // Auto-set order
        services.add(dto);
        saveServices(services);
        return dto;
    }

    public ServiceDto updateService(String id, ServiceDto dto) {
        List<ServiceDto> services = getServices();
        boolean found = false;
        for (int i = 0; i < services.size(); i++) {
            if (services.get(i).getId().equals(id)) {
                dto.setId(id); // keep the same id
                services.set(i, dto);
                found = true;
                break;
            }
        }
        if (!found) {
            throw new RuntimeException("Service not found: " + id);
        }
        saveServices(services);
        return dto;
    }

    public void deleteService(String id) {
        List<ServiceDto> services = getServices();
        boolean removed = services.removeIf(s -> s.getId().equals(id));
        if (!removed) {
            throw new RuntimeException("Service not found: " + id);
        }
        saveServices(services);
    }
}
