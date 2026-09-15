package com.ktsa.foosball.service;

import com.ktsa.foosball.dto.PageBannerDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PageBannerService {

    private final S3Service s3Service;

    @Value("${aws.s3.buckets.documents}")
    private String bucket;

    private String s3Key(String page) {
        return "page-banners/" + page + ".json";
    }

    /** Save banner content for a given page ("news" or "services"). */
    public PageBannerDto saveBanner(String page, PageBannerDto dto, MultipartFile image) {
        dto.setPage(page);

        if (image != null && !image.isEmpty()) {
            String url = s3Service.uploadFile(image, bucket, "page-banners/" + page);
            dto.setHeroBannerUrl(url);
        } else {
            // Preserve existing image URL if no new file uploaded
            Map<String, Object> existing = getBannerRaw(page);
            if (existing != null && existing.get("heroBannerUrl") != null) {
                String existingUrl = existing.get("heroBannerUrl").toString();
                if (!existingUrl.isBlank()) dto.setHeroBannerUrl(existingUrl);
            }
        }

        s3Service.uploadJson(dto, bucket, s3Key(page));
        return dto;
    }

    /** Fetch banner content for a page; returns null if not yet saved. */
    public Map<String, Object> getBanner(String page) {
        try {
            return s3Service.readJson(bucket, s3Key(page));
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> getBannerRaw(String page) {
        try {
            return s3Service.readJson(bucket, s3Key(page));
        } catch (Exception e) {
            return null;
        }
    }
}
