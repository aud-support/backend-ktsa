package com.ktsa.foosball.controller;

import com.ktsa.foosball.dto.FooterSocialDto;
import com.ktsa.foosball.service.FooterSocialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/footer")
@RequiredArgsConstructor
public class FooterSocialController {

    private final FooterSocialService footerSocialService;

    /**
     * POST /api/footer/content
     * Admin saves footer & social content (plain JSON — no file upload needed).
     */
    @PostMapping("/content")
    public ResponseEntity<String> updateContent(@RequestBody FooterSocialDto dto) {
        footerSocialService.saveFooterContent(dto);
        return ResponseEntity.ok("Footer content updated successfully");
    }

    /**
     * GET /api/footer/content
     * KTSA frontend reads footer & social content.
     */
    @GetMapping("/content")
    public ResponseEntity<Map<String, Object>> getContent() {
        Map<String, Object> content = footerSocialService.getFooterContent();
        if (content == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(content);
    }
}
