package com.ktsa.foosball.controller;

import com.ktsa.foosball.dto.ApiResponse;
import com.ktsa.foosball.service.GalleryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/gallery")
@RequiredArgsConstructor
public class GalleryController {

    private final GalleryService galleryService;

    /** GET /api/gallery — public */
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getGallery() {
        return ResponseEntity.ok(
                ApiResponse.success(200, "Gallery fetched successfully",
                        galleryService.getGalleryItems()));
    }

    /** POST /api/gallery — requires auth (admin only) */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> addItem(
            @RequestPart("image") MultipartFile image,
            @RequestPart(value = "caption", required = false) String caption,
            @RequestPart(value = "year",    required = false) String year) {

        return ResponseEntity.ok(
                ApiResponse.success(201, "Image uploaded successfully",
                        galleryService.addGalleryItem(image, caption, year)));
    }

    /** PUT /api/gallery/{id} — requires auth */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateItem(
            @PathVariable String id,
            @RequestParam(required = false) String caption,
            @RequestParam(required = false) String year) {

        return ResponseEntity.ok(
                ApiResponse.success(200, "Gallery item updated",
                        galleryService.updateGalleryItem(id, caption, year)));
    }

    /** DELETE /api/gallery/{id} — requires auth */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteItem(@PathVariable String id) {
        galleryService.deleteGalleryItem(id);
        return ResponseEntity.ok(
                ApiResponse.success(200, "Gallery item deleted", null));
    }
}
