package com.terratrace.service;

import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BuildingPolygonService {

    public Map<String, Object> extractBuildings(
        String fileName
    ) {

        OpenCV.loadLocally();

        Path inputPath =
            Paths.get(
                "uploads",
                fileName
            );

        if (!Files.exists(inputPath)) {
            throw new IllegalArgumentException(
                "Mask file not found"
            );
        }

        Mat mask =
            Imgcodecs.imread(
                inputPath.toString(),
                Imgcodecs.IMREAD_GRAYSCALE
            );

        if (mask.empty()) {
            throw new IllegalArgumentException(
                "Unable to read mask"
            );
        }

        List<MatOfPoint> contours =
            new ArrayList<>();

        Mat hierarchy =
            new Mat();

        Imgproc.findContours(
            mask,
            contours,
            hierarchy,
            Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_SIMPLE
        );

        List<Map<String, Object>> buildings =
            new ArrayList<>();

        int buildingId = 1;

        for (MatOfPoint contour : contours) {

            double area =
                Imgproc.contourArea(
                    contour
                );

            if (area < 100.0) {
                contour.release();
                continue;
            }

            MatOfPoint2f contour2f =
                new MatOfPoint2f(
                    contour.toArray()
                );

            double perimeter =
                Imgproc.arcLength(
                    contour2f,
                    true
                );

            MatOfPoint2f approximation =
                new MatOfPoint2f();

            Imgproc.approxPolyDP(
                contour2f,
                approximation,
                2.0,
                true
            );

            List<List<Double>> points =
                new ArrayList<>();

            for (
                org.opencv.core.Point point :
                approximation.toArray()
            ) {

                points.add(
                    List.of(
                        point.x,
                        point.y
                    )
                );
            }

            if (
                points.size() >= 3 &&
                !points.get(0).equals(
                    points.get(
                        points.size() - 1
                    )
                )
            ) {

                points.add(
                    new ArrayList<>(
                        points.get(0)
                    )
                );
            }

            Map<String, Object> building =
                new HashMap<>();

            building.put(
                "id",
                buildingId
            );

            building.put(
                "featureType",
                "building"
            );

            building.put(
                "areaPixels",
                area
            );

            building.put(
                "perimeterPixels",
                perimeter
            );

            building.put(
                "vertexCount",
                points.size()
            );

            building.put(
                "polygon",
                points
            );

            buildings.add(
                building
            );

            buildingId++;

            approximation.release();
            contour2f.release();
            contour.release();
        }

        mask.release();
        hierarchy.release();

        Map<String, Object> response =
            new HashMap<>();

        response.put(
            "status",
            "success"
        );

        response.put(
            "coordinateSystem",
            "image-pixel"
        );

        response.put(
            "buildingCount",
            buildings.size()
        );

        response.put(
            "buildings",
            buildings
        );

        return response;
    }
}