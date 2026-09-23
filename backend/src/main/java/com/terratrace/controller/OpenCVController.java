package com.terratrace.controller;

import com.terratrace.service.OpenCVService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/opencv")
public class OpenCVController {

    private final OpenCVService openCVService;

    public OpenCVController(
        OpenCVService openCVService
    ) {
        this.openCVService = openCVService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {

        return ResponseEntity.ok(
            Map.of(
                "opencv", "OpenCV Java",
                "loaded", openCVService.isLoaded()
            )
        );
    }

    @GetMapping("/analyze")
    public ResponseEntity<Map<String, String>> analyze(
        @RequestParam String fileName
    ) {

        Path imagePath = Paths.get(
            "uploads",
            fileName
        );

        if (!Files.exists(imagePath)) {
            return ResponseEntity.notFound().build();
        }

        String result =
            openCVService.analyzeImage(
                imagePath.toString()
            );

        return ResponseEntity.ok(
            Map.of(
                "result",
                result
            )
        );
    }
}