package com.terratrace.controller;

import com.terratrace.service.ImagePreprocessingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/preprocessing")
public class ImagePreprocessingController {

    private final ImagePreprocessingService imagePreprocessingService;

    public ImagePreprocessingController(
        ImagePreprocessingService imagePreprocessingService
    ) {
        this.imagePreprocessingService = imagePreprocessingService;
    }

    @PostMapping("/process")
    public ResponseEntity<Map<String, String>> process(
        @RequestParam String fileName
    ) {

        Path inputPath = Paths.get(
            "uploads",
            fileName
        );

        if (!Files.exists(inputPath)) {
            return ResponseEntity.notFound().build();
        }

        String result =
            imagePreprocessingService.preprocessImage(
                fileName
            );

        return ResponseEntity.ok(
            Map.of(
                "status", "success",
                "processedFile", result
            )
        );
    }
}