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
     * If a new image is provided it is uploaded to S3 and its URL stored in the DTO.
     * If no image is provided the existing whoWeAreImageUrl is preserved.
     */
    public void saveAboutUsContent(AboutUsContentDto dto, MultipartFile image) {

        if (image != null && !image.isEmpty()) {
            String imageUrl = s3Service.uploadFile(image, bucket, "about-us");
            dto.setWhoWeAreImageUrl(imageUrl);
        } else {
            // Preserve existing image URL if one was already saved
            Map<String, Object> existing = getAboutUsContent();
            if (existing != null && existing.get("whoWeAreImageUrl") != null) {
                String existingUrl = existing.get("whoWeAreImageUrl").toString();
                if (!existingUrl.isBlank()) {
                    dto.setWhoWeAreImageUrl(existingUrl);
                }
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
