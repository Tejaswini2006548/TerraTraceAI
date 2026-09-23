package com.terratrace.controller;

import com.terratrace.service.GeoreferencingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/georeferencing")
public class GeoreferencingController {

    private final GeoreferencingService georeferencingService;

    public GeoreferencingController(
        GeoreferencingService georeferencingService
    ) {
        this.georeferencingService =
            georeferencingService;
    }

    @GetMapping("/transform")
    public ResponseEntity<Map<String, Object>> transform(
        @RequestParam double pixelX,
        @RequestParam double pixelY,
        @RequestParam double originX,
        @RequestParam double originY,
        @RequestParam double pixelSizeX,
        @RequestParam double pixelSizeY
    ) {

        return ResponseEntity.ok(
            georeferencingService.pixelToMap(
                pixelX,
                pixelY,
                originX,
                originY,
                pixelSizeX,
                pixelSizeY
            )
        );
    }
}