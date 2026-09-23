package com.terratrace.controller;

import com.terratrace.service.GeometryValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/geometry")
public class GeometryController {

    private final GeometryValidationService geometryValidationService;

    public GeometryController(
        GeometryValidationService geometryValidationService
    ) {
        this.geometryValidationService =
            geometryValidationService;
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(
        @RequestBody double[][] coordinates
    ) {

        return ResponseEntity.ok(
            geometryValidationService.validatePolygon(
                coordinates
            )
        );
    }

    @PostMapping("/repair")
    public ResponseEntity<Map<String, Object>> repair(
        @RequestBody double[][] coordinates
    ) {

        return ResponseEntity.ok(
            geometryValidationService.repairPolygon(
                coordinates
            )
        );
    }
}