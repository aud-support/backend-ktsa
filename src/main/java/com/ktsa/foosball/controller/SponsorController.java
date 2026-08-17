package com.ktsa.foosball.controller;

import com.ktsa.foosball.dto.SponsorDto;
import com.ktsa.foosball.service.SponsorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/sponsors")
@RequiredArgsConstructor
public class SponsorController {

    private final SponsorService sponsorService;

    @GetMapping
    public ResponseEntity<List<SponsorDto>> getAll() {
        return ResponseEntity.ok(sponsorService.getAllSponsors());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SponsorDto> create(
            @RequestPart("data") SponsorDto dto,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(sponsorService.createSponsor(dto, image));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SponsorDto> update(
            @PathVariable String id,
            @RequestPart("data") SponsorDto dto,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(sponsorService.updateSponsor(id, dto, image));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        sponsorService.deleteSponsor(id);
        return ResponseEntity.ok("Sponsor deleted");
    }
}
