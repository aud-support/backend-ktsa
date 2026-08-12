package com.ktsa.foosball.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktsa.foosball.dto.ArticleDto;
import com.ktsa.foosball.dto.HomepageContentDto;
import com.ktsa.foosball.service.HomepageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/homepage")
@RequiredArgsConstructor
public class HomepageController {

    private final HomepageService homepageService;
    private final ObjectMapper objectMapper;

    // Admin updates content via form fields
    @PostMapping(value = "/content", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateContent(
            @RequestPart("data") HomepageContentDto dto,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        homepageService.saveHomepageContent(dto, image);
        return ResponseEntity.ok("Homepage content updated successfully");
    }

    @GetMapping("/content")
    public ResponseEntity<Map<String, Object>> getContent() {
        return ResponseEntity.ok(homepageService.getHomepageContent());
    }

    // ──────────────────────────────────────────────────────────────
    // Articles
    // ──────────────────────────────────────────────────────────────

    /** GET /api/homepage/articles — fetch all articles (public) */
    @GetMapping("/articles")
    public ResponseEntity<List<ArticleDto>> getArticles() {
        return ResponseEntity.ok(homepageService.getArticles());
    }

    /** POST /api/homepage/articles — create a new article */
    @PostMapping("/articles")
    public ResponseEntity<ArticleDto> createArticle(@RequestBody ArticleDto dto) {
        return ResponseEntity.ok(homepageService.createArticle(dto));
    }

    /** PUT /api/homepage/articles/{id} — update an existing article */
    @PutMapping("/articles/{id}")
    public ResponseEntity<ArticleDto> updateArticle(
            @PathVariable String id,
            @RequestBody ArticleDto dto) {
        return ResponseEntity.ok(homepageService.updateArticle(id, dto));
    }

    /** DELETE /api/homepage/articles/{id} — remove an article */
    @DeleteMapping("/articles/{id}")
    public ResponseEntity<String> deleteArticle(@PathVariable String id) {
        homepageService.deleteArticle(id);
        return ResponseEntity.ok("Article deleted successfully");
    }
}
