package com.ktsa.foosball.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktsa.foosball.dto.HomepageContentDto;
import com.ktsa.foosball.service.HomepageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/homepage")
@RequiredArgsConstructor
public class HomepageController {

    private final HomepageService homepageService;
    private final ObjectMapper objectMapper;

    // Admin updates content via form fields
    @PostMapping(value = "/content", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateContent(
            @RequestPart("data") HomepageContentDto dto,
            @RequestPart("image") MultipartFile image) throws JsonProcessingException {

//        HomepageContentDto dto = objectMapper.readValue(data, HomepageContentDto.class); // ✅ then convert

        homepageService.saveHomepageContent(dto, image);
        return ResponseEntity.ok("Homepage content updated successfully");
    }

    // Frontend reads content
    @GetMapping("/content")
    public ResponseEntity<Map<String, Object>> getContent() {
        return ResponseEntity.ok(homepageService.getHomepageContent());
    }
}