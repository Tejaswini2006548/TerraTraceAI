package com.terratrace.service;

import com.terratrace.model.Building;
import com.terratrace.repository.BuildingRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BuildingGeoJsonService {

    private final BuildingRepository buildingRepository;

    public BuildingGeoJsonService(
        BuildingRepository buildingRepository
    ) {
        this.buildingRepository =
            buildingRepository;
    }

    public Map<String, Object> getBuildingsGeoJson() {

        List<Building> buildings =
            buildingRepository.findAll();

        List<Map<String, Object>> features =
            new ArrayList<>();

        for (Building building : buildings) {

            Geometry geometry =
                building.getGeometry();

            if (
                geometry == null ||
                geometry.isEmpty()
            ) {
                continue;
            }

            Map<String, Object> properties =
                new LinkedHashMap<>();

            properties.put(
                "id",
                building.getId()
            );

            properties.put(
                "featureType",
                building.getFeatureType()
            );

            properties.put(
                "areaPixels",
                building.getAreaPixels()
            );

            properties.put(
                "mapArea",
                building.getMapArea()
            );

            properties.put(
                "perimeter",
                building.getPerimeter()
            );

            properties.put(
                "verificationStatus",
                building.getVerificationStatus()
            );

            properties.put(
                "verificationNote",
                building.getVerificationNote()
            );

            Map<String, Object> geometryJson =
                createGeometryJson(
                    geometry
                );

            Map<String, Object> feature =
                new LinkedHashMap<>();

            feature.put(
                "type",
                "Feature"
            );

            feature.put(
                "properties",
                properties
            );

            feature.put(
                "geometry",
                geometryJson
            );

            features.add(feature);
        }

        Map<String, Object> geoJson =
            new LinkedHashMap<>();

        geoJson.put(
            "type",
            "FeatureCollection"
        );

        geoJson.put(
            "features",
            features
        );

        return geoJson;
    }

    private Map<String, Object> createGeometryJson(
        Geometry geometry
    ) {

        Map<String, Object> result =
            new LinkedHashMap<>();

        String geometryType =
            geometry.getGeometryType();

        if ("Polygon".equals(geometryType)) {

            result.put(
                "type",
                "Polygon"
            );

            result.put(
                "coordinates",
                List.of(
                    createRing(
                        geometry.getCoordinates()
                    )
                )
            );

            return result;
        }

        if ("MultiPolygon".equals(geometryType)) {

            List<List<List<List<Double>>>> polygons =
                new ArrayList<>();

            for (
                int i = 0;
                i < geometry.getNumGeometries();
                i++
            ) {

                Geometry polygon =
                    geometry.getGeometryN(i);

                List<List<Double>> ring =
                    createRing(
                        polygon.getCoordinates()
                    );

                polygons.add(
                    List.of(ring)
                );
            }

            result.put(
                "type",
                "MultiPolygon"
            );

            result.put(
                "coordinates",
                polygons
            );

            return result;
        }

        throw new IllegalArgumentException(
            "Unsupported geometry type: "
                + geometryType
        );
    }

    private List<List<Double>> createRing(
        Coordinate[] coordinates
    ) {

        List<List<Double>> ring =
            new ArrayList<>();

        for (Coordinate coordinate : coordinates) {

            ring.add(
                List.of(
                    coordinate.getX(),
                    coordinate.getY()
                )
            );
        }

        return ring;
    }
}