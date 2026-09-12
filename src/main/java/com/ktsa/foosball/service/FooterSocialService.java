package com.ktsa.foosball.service;

import com.ktsa.foosball.dto.FooterSocialDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class FooterSocialService {

    private final S3Service s3Service;

    @Value("${aws.s3.buckets.documents}")
    private String bucket;

    // Fixed S3 key — always overwrite on save
    private static final String FOOTER_KEY = "footer/content.json";

    /**
     * Admin saves footer & social content.
     */
    public void saveFooterContent(FooterSocialDto dto) {
        s3Service.uploadJson(dto, bucket, FOOTER_KEY);
    }

    /**
     * KTSA frontend fetches footer & social content.
     * Returns null gracefully if the file doesn't exist yet (first run).
     */
    public Map<String, Object> getFooterContent() {
        try {
            return s3Service.readJson(bucket, FOOTER_KEY);
        } catch (Exception e) {
            return null;
        }
    }
}
