package com.terratrace.controller;

import com.terratrace.service.BuildingPolygonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/buildings")
public class BuildingPolygonController {

    private final BuildingPolygonService buildingPolygonService;

    public BuildingPolygonController(
        BuildingPolygonService buildingPolygonService
    ) {
        this.buildingPolygonService =
            buildingPolygonService;
    }

    @PostMapping("/extract")
    public ResponseEntity<Map<String, Object>> extract(
        @RequestParam String fileName
    ) {

        return ResponseEntity.ok(
            buildingPolygonService.extractBuildings(
                fileName
            )
        );
    }
}