package com.ktsa.foosball.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktsa.foosball.dto.SponsorDto;
import com.ktsa.foosball.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SponsorService {

    private final S3Service s3Service;
    private final ObjectMapper objectMapper;

    @Value("${aws.s3.buckets.documents}")
    private String bucket;

    private static final String SPONSORS_KEY = "sponsors/content.json";

    // ── Helpers ───────────────────────────────────────────────────────────────

    private List<SponsorDto> readAll() {
        try {
            Map<String, Object> raw = s3Service.readJson(bucket, SPONSORS_KEY);
            Object arr = raw.get("sponsors");
            if (arr == null) return new ArrayList<>();
            String json = objectMapper.writeValueAsString(arr);
            return objectMapper.readValue(json, new TypeReference<List<SponsorDto>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void writeAll(List<SponsorDto> sponsors) {
        s3Service.uploadJson(Map.of("sponsors", sponsors), bucket, SPONSORS_KEY);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public List<SponsorDto> getAllSponsors() {
        return readAll();
    }

    /** Create — upload image to S3, store URL */
    public SponsorDto createSponsor(SponsorDto dto, MultipartFile image) {
        dto.setId(UUID.randomUUID().toString());
        if (image != null && !image.isEmpty()) {
            dto.setImageUrl(s3Service.uploadFile(image, bucket, "sponsors"));
        }
        List<SponsorDto> list = readAll();
        list.add(dto);
        writeAll(list);
        return dto;
    }

    /** Update — upload new image if provided, else keep existing */
    public SponsorDto updateSponsor(String id, SponsorDto dto, MultipartFile image) {
        List<SponsorDto> list = readAll();
        for (int i = 0; i < list.size(); i++) {
            if (id.equals(list.get(i).getId())) {
                dto.setId(id);
                if (image != null && !image.isEmpty()) {
                    dto.setImageUrl(s3Service.uploadFile(image, bucket, "sponsors"));
                } else if (dto.getImageUrl() == null || dto.getImageUrl().isBlank()) {
                    dto.setImageUrl(list.get(i).getImageUrl());
                }
                list.set(i, dto);
                writeAll(list);
                return dto;
            }
        }
        throw new NotFoundException("Sponsor not found: " + id);
    }

    public void deleteSponsor(String id) {
        List<SponsorDto> list = readAll();
        if (!list.removeIf(s -> id.equals(s.getId())))
            throw new NotFoundException("Sponsor not found: " + id);
        writeAll(list);
    }
}
