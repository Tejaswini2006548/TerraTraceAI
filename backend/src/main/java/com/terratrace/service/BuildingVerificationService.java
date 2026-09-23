package com.terratrace.service;

import com.terratrace.model.Building;
import com.terratrace.repository.BuildingRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class BuildingVerificationService {

    private final BuildingRepository buildingRepository;

    public BuildingVerificationService(
        BuildingRepository buildingRepository
    ) {
        this.buildingRepository =
            buildingRepository;
    }

    public Map<String, Object> updateStatus(
        Long id,
        String status,
        String note
    ) {

        Building building =
            buildingRepository
                .findById(id)
                .orElseThrow(
                    () -> new IllegalArgumentException(
                        "Building not found"
                    )
                );

        String normalizedStatus =
            status.toUpperCase();

        if (
            !normalizedStatus.equals("PENDING") &&
            !normalizedStatus.equals("APPROVED") &&
            !normalizedStatus.equals("REJECTED")
        ) {
            throw new IllegalArgumentException(
                "Invalid verification status"
            );
        }

        building.setVerificationStatus(
            normalizedStatus
        );

        building.setVerificationNote(
            note
        );

        Building saved =
            buildingRepository.save(
                building
            );

        Map<String, Object> response =
            new LinkedHashMap<>();

        response.put(
            "status",
            "success"
        );

        response.put(
            "buildingId",
            saved.getId()
        );

        response.put(
            "verificationStatus",
            saved.getVerificationStatus()
        );

        response.put(
            "verificationNote",
            saved.getVerificationNote()
        );

        return response;
    }
}