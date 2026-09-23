package com.terratrace.controller;

import com.terratrace.service.MaskProcessingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mask")
public class MaskProcessingController {

    private final MaskProcessingService maskProcessingService;

    public MaskProcessingController(
        MaskProcessingService maskProcessingService
    ) {
        this.maskProcessingService =
            maskProcessingService;
    }

    @PostMapping("/clean")
    public ResponseEntity<Map<String, String>> clean(
        @RequestParam String fileName
    ) {

        String result =
            maskProcessingService.cleanBuildingMask(
                fileName
            );

        return ResponseEntity.ok(
            Map.of(
                "status",
                "success",
                "cleanedMask",
                result
            )
        );
    }
}