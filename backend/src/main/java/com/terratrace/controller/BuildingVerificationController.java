package com.terratrace.controller;

import com.terratrace.service.BuildingVerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/buildings")
public class BuildingVerificationController {

    private final BuildingVerificationService
        buildingVerificationService;

    public BuildingVerificationController(
        BuildingVerificationService
            buildingVerificationService
    ) {
        this.buildingVerificationService =
            buildingVerificationService;
    }

    @PutMapping("/{id}/verification")
    public ResponseEntity<Map<String, Object>>
        updateVerification(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false)
                String note
        ) {

        return ResponseEntity.ok(
            buildingVerificationService
                .updateStatus(
                    id,
                    status,
                    note
                )
        );
    }
}