package com.terratrace.controller;

import com.terratrace.service.BuildingStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/buildings")
public class BuildingStorageController {

    private final BuildingStorageService buildingStorageService;

    public BuildingStorageController(
        BuildingStorageService buildingStorageService
    ) {
        this.buildingStorageService =
            buildingStorageService;
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importGeoJson(
        @RequestParam String fileName
    ) {

        return ResponseEntity.ok(
            buildingStorageService.importGeoJson(
                fileName
            )
        );
    }
}