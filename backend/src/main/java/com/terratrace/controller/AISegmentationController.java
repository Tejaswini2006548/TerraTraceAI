package com.terratrace.controller;

import com.terratrace.service.AISegmentationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AISegmentationController {

    private final AISegmentationService aiSegmentationService;

    public AISegmentationController(
        AISegmentationService aiSegmentationService
    ) {
        this.aiSegmentationService =
            aiSegmentationService;
    }

    @PostMapping("/segment")
    public ResponseEntity<Map<String, Object>> segment(
        @RequestParam String fileName
    ) {

        return ResponseEntity.ok(
            aiSegmentationService.segmentImage(
                fileName
            )
        );
    }
}