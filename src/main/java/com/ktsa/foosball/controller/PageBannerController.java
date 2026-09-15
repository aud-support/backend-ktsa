package com.ktsa.foosball.controller;

import com.ktsa.foosball.dto.ApiResponse;
import com.ktsa.foosball.dto.PageBannerDto;
import com.ktsa.foosball.service.PageBannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/page-banners")
@RequiredArgsConstructor
public class PageBannerController {

    private final PageBannerService pageBannerService;

    private static final Set<String> ALLOWED_PAGES = Set.of("news", "services");

    /**
     * GET /api/page-banners/{page}
     * Public — frontend reads banner content for "news" or "services".
     */
    @GetMapping("/{page}")
    public ResponseEntity<ApiResponse<?>> getBanner(@PathVariable String page) {
        if (!ALLOWED_PAGES.contains(page)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Unknown page: " + page, null));
        }
        Map<String, Object> content = pageBannerService.getBanner(page);
        if (content == null) {
            return ResponseEntity.ok(ApiResponse.success(200, "No banner configured yet", null));
        }
        return ResponseEntity.ok(ApiResponse.success(200, "Banner fetched", content));
    }

    @PostMapping(value = "/{page}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> saveBanner(
            @PathVariable String page,
            @RequestPart("data") PageBannerDto dto,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        if (!ALLOWED_PAGES.contains(page)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Unknown page: " + page, null));
        }
        PageBannerDto saved = pageBannerService.saveBanner(page, dto, image);
        return ResponseEntity.ok(ApiResponse.success(200, "Banner saved successfully", saved));
    }
}
