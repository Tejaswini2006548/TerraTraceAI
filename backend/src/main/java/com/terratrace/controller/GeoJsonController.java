package com.terratrace.controller;

import com.terratrace.service.GeoJsonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/geojson")
public class GeoJsonController {

    private final GeoJsonService geoJsonService;

    public GeoJsonController(
        GeoJsonService geoJsonService
    ) {
        this.geoJsonService =
            geoJsonService;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateGeoJson(
        @RequestParam String maskFileName,
        @RequestParam String geoTiffFileName
    ) {

        return ResponseEntity.ok(
            geoJsonService.generateGeoJson(
                maskFileName,
                geoTiffFileName
            )
        );
    }
}