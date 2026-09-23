package com.terratrace.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.terratrace.model.Building;
import com.terratrace.repository.BuildingRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BuildingStorageService {

    private final BuildingRepository buildingRepository;

    private final ObjectMapper objectMapper =
        new ObjectMapper();

    private final GeometryFactory geometryFactory =
    new GeometryFactory(
        new org.locationtech.jts.geom.PrecisionModel(),
        4326
    );

    public BuildingStorageService(
        BuildingRepository buildingRepository
    ) {
        this.buildingRepository =
            buildingRepository;
    }

    @Transactional
    public Map<String, Object> importGeoJson(
        String fileName
    ) {

        Path inputPath =
            Paths.get(
                "uploads",
                fileName
            );

        if (!Files.exists(inputPath)) {
            throw new IllegalArgumentException(
                "GeoJSON file not found"
            );
        }

        try {

            JsonNode root =
                objectMapper.readTree(
                    inputPath.toFile()
                );

            JsonNode features =
                root.get("features");

            if (
                features == null ||
                !features.isArray()
            ) {
                throw new IllegalArgumentException(
                    "Invalid GeoJSON FeatureCollection"
                );
            }

            buildingRepository.deleteAll();

            int importedCount = 0;

            for (JsonNode feature : features) {

                JsonNode properties =
                    feature.get("properties");

                JsonNode geometryNode =
                    feature.get("geometry");

                if (
                    geometryNode == null ||
                    geometryNode.isNull()
                ) {
                    continue;
                }

                Geometry geometry =
                    createGeometry(
                        geometryNode
                    );

                if (
                    geometry == null ||
                    geometry.isEmpty()
                ) {
                    continue;
                }

                Building building =
                    new Building();

                building.setFeatureType(
                    properties.path(
                        "featureType"
                    ).asText("building")
                );

                building.setAreaPixels(
                    properties.path(
                        "areaPixels"
                    ).asDouble(
                        0.0
                    )
                );

                building.setMapArea(
                    properties.path(
                        "mapArea"
                    ).asDouble(
                        geometry.getArea()
                    )
                );

                building.setPerimeter(
                    properties.path(
                        "perimeter"
                    ).asDouble(
                        geometry.getLength()
                    )
                );

                building.setGeometry(
                    geometry
                );

                buildingRepository.save(
                    building
                );

                importedCount++;
            }

            return Map.of(
                "status",
                "success",
                "importedCount",
                importedCount,
                "databaseTable",
                "buildings",
                "sourceFile",
                fileName
            );

        } catch (Exception e) {

            throw new RuntimeException(
                "Failed to import GeoJSON: "
                    + e.getMessage(),
                e
            );
        }
    }

    private Geometry createGeometry(
        JsonNode geometryNode
    ) {

        String type =
            geometryNode
                .path("type")
                .asText();

        JsonNode coordinates =
            geometryNode.get(
                "coordinates"
            );

        if ("Polygon".equals(type)) {
            return createPolygon(
                coordinates
            );
        }

        if ("MultiPolygon".equals(type)) {
            return createMultiPolygon(
                coordinates
            );
        }

        throw new IllegalArgumentException(
            "Unsupported geometry type: "
                + type
        );
    }

    private Polygon createPolygon(
        JsonNode coordinates
    ) {

        JsonNode outerRing =
            coordinates.get(0);

        Coordinate[] points =
            createCoordinates(
                outerRing
            );

        LinearRing shell =
            geometryFactory.createLinearRing(
                points
            );

        return geometryFactory.createPolygon(
            shell
        );
    }

    private MultiPolygon createMultiPolygon(
        JsonNode coordinates
    ) {

        List<Polygon> polygons =
            new ArrayList<>();

        for (
            JsonNode polygonNode :
            coordinates
        ) {

            JsonNode outerRing =
                polygonNode.get(0);

            Coordinate[] points =
                createCoordinates(
                    outerRing
                );

            LinearRing shell =
                geometryFactory.createLinearRing(
                    points
                );

            polygons.add(
                geometryFactory.createPolygon(
                    shell
                )
            );
        }

        return geometryFactory.createMultiPolygon(
            polygons.toArray(
                new Polygon[0]
            )
        );
    }

    private Coordinate[] createCoordinates(
        JsonNode ring
    ) {

        List<Coordinate> coordinates =
            new ArrayList<>();

        for (
            JsonNode point :
            ring
        ) {

            double x =
                point.get(0).asDouble();

            double y =
                point.get(1).asDouble();

            coordinates.add(
                new Coordinate(
                    x,
                    y
                )
            );
        }

        return coordinates.toArray(
            new Coordinate[0]
        );
    }
}