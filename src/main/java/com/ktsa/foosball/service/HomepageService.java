package com.ktsa.foosball.service;

import com.ktsa.foosball.dto.HomepageContentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class HomepageService {

    private final S3Service s3Service;

    @Value("${aws.s3.buckets.documents}")
    private String bucket;

    private static final String HOMEPAGE_KEY = "homepage/content.json"; // fixed key, always overwrite

    // Admin saves homepage content
    public void saveHomepageContent(HomepageContentDto dto, MultipartFile image) {

        // 1. Upload image to S3 and get URL
        String imageUrl = s3Service.uploadFile(image, bucket, "homepage");

        // 2. Set image URL into dto
        dto.setHeroBannerUrl(imageUrl);

        // 3. Save JSON (with imageUrl inside) to S3
        s3Service.uploadJson(dto, bucket, HOMEPAGE_KEY);
    }

    // Frontend fetches homepage content
    public Map<String, Object> getHomepageContent() {
        return s3Service.readJson(bucket, HOMEPAGE_KEY);
    }
}