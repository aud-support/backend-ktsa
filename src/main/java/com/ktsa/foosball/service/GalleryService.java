package com.ktsa.foosball.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktsa.foosball.dto.GalleryItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GalleryService {

    private final S3Service s3Service;
    private final ObjectMapper objectMapper;

    @Value("${aws.s3.buckets.documents}")
    private String bucket;

    @Value("${aws.s3.buckets.tournaments}")
    private String imagesBucket;

    private static final String GALLERY_KEY = "gallery/images.json";

    public List<GalleryItemDto> getGalleryItems() {
        try {
            String json = s3Service.readJsonAsString(bucket, GALLERY_KEY);
            if (json == null || json.isBlank()) return new ArrayList<>();

            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(json);
            com.fasterxml.jackson.databind.JsonNode items = root.get("items");
            if (items == null || items.isNull()) return new ArrayList<>();

            return objectMapper.readValue(items.toString(),
                    new TypeReference<List<GalleryItemDto>>() {});
        } catch (software.amazon.awssdk.services.s3.model.NoSuchKeyException e) {
            return new ArrayList<>();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read gallery from S3", e);
        }
    }

    private void saveItems(List<GalleryItemDto> items) {
        s3Service.uploadJson(Map.of("items", items), bucket, GALLERY_KEY);
    }

    public GalleryItemDto addGalleryItem(MultipartFile image, String caption, String year) {
        String imageUrl = s3Service.uploadFile(image, imagesBucket, "gallery");

        List<GalleryItemDto> items = getGalleryItems();

        GalleryItemDto item = new GalleryItemDto();
        item.setId(UUID.randomUUID().toString());
        item.setImageUrl(imageUrl);
        item.setCaption(caption != null ? caption : "");
        item.setYear(year != null ? year : "");
        item.setDisplayOrder(items.size());

        items.add(item);
        saveItems(items);
        return item;
    }

    public GalleryItemDto updateGalleryItem(String id, String caption, String year) {
        List<GalleryItemDto> items = getGalleryItems();
        for (GalleryItemDto item : items) {
            if (item.getId().equals(id)) {
                if (caption != null) item.setCaption(caption);
                if (year    != null) item.setYear(year);
                saveItems(items);
                return item;
            }
        }
        throw new RuntimeException("Gallery item not found: " + id);
    }

    public void deleteGalleryItem(String id) {
        List<GalleryItemDto> items = getGalleryItems();
        boolean removed = items.removeIf(i -> i.getId().equals(id));
        if (!removed) throw new RuntimeException("Gallery item not found: " + id);
        for (int i = 0; i < items.size(); i++) items.get(i).setDisplayOrder(i);
        saveItems(items);
    }
}
