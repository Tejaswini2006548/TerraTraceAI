package com.terratrace.controller;

import com.terratrace.service.GeoTiffService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GeoTiffController {

    private final GeoTiffService geoTiffService;

    public GeoTiffController(
        GeoTiffService geoTiffService
    ) {
        this.geoTiffService = geoTiffService;
    }

    @GetMapping("/api/geotiff/metadata")
    public Map<String, Object> metadata(
        @RequestParam String fileName
    ) {
        return geoTiffService.readMetadata(fileName);
    }
}