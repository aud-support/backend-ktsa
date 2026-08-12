package com.ktsa.foosball.controller;

import com.ktsa.foosball.dto.AboutUsContentDto;
import com.ktsa.foosball.service.AboutUsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/about-us")
@RequiredArgsConstructor
public class AboutUsController {

    private final AboutUsService aboutUsService;

    /**
     * POST /api/about-us/content
     * Admin updates About Us content (with optional team image upload).
     */
    @PostMapping(value = "/content", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateContent(
            @RequestPart("data") AboutUsContentDto dto,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        aboutUsService.saveAboutUsContent(dto, image);
        return ResponseEntity.ok("About Us content updated successfully");
    }

    /**
     * GET /api/about-us/content
     * KTSA frontend reads About Us content.
     */
    @GetMapping("/content")
    public ResponseEntity<Map<String, Object>> getContent() {
        Map<String, Object> content = aboutUsService.getAboutUsContent();
        if (content == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(content);
    }
}
