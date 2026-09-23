package com.terratrace.controller;

import com.terratrace.service.CrsTransformationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/crs")
public class CrsTransformationController {

    private final CrsTransformationService
        crsTransformationService;

    public CrsTransformationController(
        CrsTransformationService
            crsTransformationService
    ) {
        this.crsTransformationService =
            crsTransformationService;
    }

    @GetMapping("/transform")
    public ResponseEntity<Map<String, Object>> transform(
        @RequestParam double x,
        @RequestParam double y,
        @RequestParam String sourceEpsg,
        @RequestParam String targetEpsg
    ) {

        return ResponseEntity.ok(
            crsTransformationService.transform(
                x,
                y,
                sourceEpsg,
                targetEpsg
            )
        );
    }
}
