package com.terratrace.controller;

import com.terratrace.service.BuildingGeoJsonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/gis")
public class BuildingGeoJsonController {

    private final BuildingGeoJsonService buildingGeoJsonService;

    public BuildingGeoJsonController(
        BuildingGeoJsonService buildingGeoJsonService
    ) {
        this.buildingGeoJsonService =
            buildingGeoJsonService;
    }

    @GetMapping("/buildings")
    public ResponseEntity<Map<String, Object>> getBuildings() {

        return ResponseEntity.ok(
            buildingGeoJsonService
                .getBuildingsGeoJson()
        );
    }
}