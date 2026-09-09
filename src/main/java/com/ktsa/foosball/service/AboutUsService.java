package com.ktsa.foosball.service;

import com.ktsa.foosball.dto.AboutUsContentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AboutUsService {

    private final S3Service s3Service;

    @Value("${aws.s3.buckets.documents}")
    private String bucket;

    // Fixed S3 key — always overwrite on save
    private static final String ABOUT_US_KEY = "about-us/content.json";

    /**
     * Admin saves About Us content.
     * Uploads team image and/or rulebook PDF to S3 if provided.
     * Existing URLs are preserved when no new file is uploaded.
     */
    public void saveAboutUsContent(AboutUsContentDto dto, MultipartFile image, MultipartFile rulebook) {

        Map<String, Object> existing = getAboutUsContent();

        // ── Team image ────────────────────────────────────────────────────────
        if (image != null && !image.isEmpty()) {
            String imageUrl = s3Service.uploadFile(image, bucket, "about-us");
            dto.setWhoWeAreImageUrl(imageUrl);
        } else {
            if (existing != null && existing.get("whoWeAreImageUrl") != null) {
                String existingUrl = existing.get("whoWeAreImageUrl").toString();
                if (!existingUrl.isBlank()) dto.setWhoWeAreImageUrl(existingUrl);
            }
        }

        // ── Rulebook PDF ──────────────────────────────────────────────────────
        if (rulebook != null && !rulebook.isEmpty()) {
            String rulebookUrl = s3Service.uploadFile(rulebook, bucket, "about-us/rulebook");
            dto.setRulebookUrl(rulebookUrl);
        } else {
            // Preserve existing URL if admin didn't upload a new file
            if ((dto.getRulebookUrl() == null || dto.getRulebookUrl().isBlank())
                    && existing != null && existing.get("rulebookUrl") != null) {
                dto.setRulebookUrl(existing.get("rulebookUrl").toString());
            }
        }

        s3Service.uploadJson(dto, bucket, ABOUT_US_KEY);
    }

    /**
     * KTSA frontend fetches About Us content.
     */
    public Map<String, Object> getAboutUsContent() {
        try {
            return s3Service.readJson(bucket, ABOUT_US_KEY);
        } catch (Exception e) {
            // Return null gracefully if the file doesn't exist yet (first run)
            return null;
        }
    }
}
