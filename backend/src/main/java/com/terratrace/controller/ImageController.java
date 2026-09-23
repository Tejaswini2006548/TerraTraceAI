package com.terratrace.controller;

import com.terratrace.service.ImageProcessingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageProcessingService imageProcessingService;

    public ImageController(
        ImageProcessingService imageProcessingService
    ) {
        this.imageProcessingService = imageProcessingService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadImage(
        @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(
            imageProcessingService.saveImage(file)
        );
    }
}