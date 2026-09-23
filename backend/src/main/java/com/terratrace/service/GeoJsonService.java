package com.terratrace.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import nu.pattern.OpenCV;
import org.geotools.api.geometry.Position;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.util.GeometryFixer;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeoJsonService {

    private final ObjectMapper objectMapper =
        new ObjectMapper();

    private final GeometryFactory geometryFactory =
        new GeometryFactory();

    public Map<String, Object> generateGeoJson(
        String maskFileName,
        String geoTiffFileName
    ) {

        OpenCV.loadLocally();

        Path maskPath =
            Paths.get(
                "uploads",
                maskFileName
            );

        Path geoTiffPath =
            Paths.get(
                "uploads",
                geoTiffFileName
            );

        if (!Files.exists(maskPath)) {
            throw new IllegalArgumentException(
                "Building mask not found"
            );
        }

        if (!Files.exists(geoTiffPath)) {
            throw new IllegalArgumentException(
                "GeoTIFF file not found"
            );
        }

        GeoTiffReader reader = null;
        GridCoverage2D coverage = null;

        try {

            reader =
                new GeoTiffReader(
                    geoTiffPath.toFile()
                );

            coverage =
                reader.read(null);

            if (coverage == null) {
                throw new IllegalArgumentException(
                    "Unable to read GeoTIFF"
                );
            }

            GridGeometry2D gridGeometry =
                coverage.getGridGeometry();

            CoordinateReferenceSystem sourceCrs =
                coverage
                    .getCoordinateReferenceSystem2D();

            CoordinateReferenceSystem targetCrs =
                CRS.decode(
                    "EPSG:4326",
                    true
                );

            MathTransform crsTransform =
                CRS.findMathTransform(
                    sourceCrs,
                    targetCrs,
                    true
                );

            Mat mask =
                Imgcodecs.imread(
                    maskPath.toString(),
                    Imgcodecs.IMREAD_GRAYSCALE
                );

            if (mask.empty()) {
                throw new IllegalArgumentException(
                    "Unable to read building mask"
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

            List<Map<String, Object>> features =
                new ArrayList<>();

            int buildingId = 1;
            int validCount = 0;
            int repairedCount = 0;
            int invalidCount = 0;

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

                MatOfPoint2f approximation =
                    new MatOfPoint2f();

                Imgproc.approxPolyDP(
                    contour2f,
                    approximation,
                    2.0,
                    true
                );

                List<Coordinate> projectedCoordinates =
                    new ArrayList<>();

                for (
                    org.opencv.core.Point point :
                    approximation.toArray()
                ) {

                    Position position =
                        gridGeometry.gridToWorld(
                            new GridCoordinates2D(
                                (int) Math.round(point.x),
                                (int) Math.round(point.y)
                            )
                        );

                    double[] coordinates =
                        position.getCoordinate();

                    projectedCoordinates.add(
                        new Coordinate(
                            coordinates[0],
                            coordinates[1]
                        )
                    );
                }

                if (projectedCoordinates.size() < 3) {
                    approximation.release();
                    contour2f.release();
                    contour.release();
                    continue;
                }

                Coordinate first =
                    projectedCoordinates.get(0);

                Coordinate last =
                    projectedCoordinates.get(
                        projectedCoordinates.size() - 1
                    );

                if (!first.equals2D(last)) {
                    projectedCoordinates.add(
                        new Coordinate(first)
                    );
                }

                Polygon polygon;

                try {

                    polygon =
                        geometryFactory.createPolygon(
                            projectedCoordinates.toArray(
                                new Coordinate[0]
                            )
                        );

                } catch (Exception e) {

                    invalidCount++;

                    approximation.release();
                    contour2f.release();
                    contour.release();

                    continue;
                }

                boolean validBefore =
                    polygon.isValid();

                Geometry finalProjectedGeometry =
                    polygon;

                if (!validBefore) {

                    invalidCount++;

                    finalProjectedGeometry =
                        GeometryFixer.fix(
                            polygon
                        );

                    if (
                        finalProjectedGeometry == null
                        || finalProjectedGeometry.isEmpty()
                        || !finalProjectedGeometry.isValid()
                    ) {

                        approximation.release();
                        contour2f.release();
                        contour.release();

                        continue;
                    }

                    repairedCount++;

                } else {

                    validCount++;
                }

                Geometry finalWgs84Geometry;

                try {

                    finalWgs84Geometry =
                        JTS.transform(
                            finalProjectedGeometry,
                            crsTransform
                        );

                } catch (Exception e) {

                    approximation.release();
                    contour2f.release();
                    contour.release();

                    continue;
                }

                Map<String, Object> geometry =
                    createGeometryJson(
                        finalWgs84Geometry
                    );

                Map<String, Object> properties =
                    new LinkedHashMap<>();

                properties.put(
                    "id",
                    buildingId
                );

                properties.put(
                    "featureType",
                    "building"
                );

                properties.put(
                    "areaPixels",
                    area
                );

                properties.put(
                    "projectedArea",
                    finalProjectedGeometry.getArea()
                );

                properties.put(
                    "projectedPerimeter",
                    finalProjectedGeometry.getLength()
                );

                properties.put(
                    "validBeforeRepair",
                    validBefore
                );

                properties.put(
                    "geometryValid",
                    finalProjectedGeometry.isValid()
                );

                properties.put(
                    "sourceCrs",
                    sourceCrs.toString()
                );

                properties.put(
                    "targetCrs",
                    "EPSG:4326"
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
                    geometry
                );

                features.add(feature);

                buildingId++;

                approximation.release();
                contour2f.release();
                contour.release();
            }

            mask.release();
            hierarchy.release();

            Map<String, Object> geoJson =
                new LinkedHashMap<>();

            geoJson.put(
                "type",
                "FeatureCollection"
            );

            geoJson.put(
                "sourceCrs",
                sourceCrs.toString()
            );

            geoJson.put(
                "targetCrs",
                "EPSG:4326"
            );

            geoJson.put(
                "coordinateSystem",
                "WGS84"
            );

            geoJson.put(
                "geoTiffFile",
                geoTiffFileName
            );

            geoJson.put(
                "maskFile",
                maskFileName
            );

            geoJson.put(
                "features",
                features
            );

            String outputFileName =
                "buildings_wgs84.geojson";

            Path outputPath =
                Paths.get(
                    "uploads",
                    outputFileName
                );

            objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValue(
                    outputPath.toFile(),
                    geoJson
                );

            Map<String, Object> response =
                new LinkedHashMap<>();

            response.put(
                "status",
                "success"
            );

            response.put(
                "featureCount",
                features.size()
            );

            response.put(
                "validCount",
                validCount
            );

            response.put(
                "repairedCount",
                repairedCount
            );

            response.put(
                "invalidCount",
                invalidCount
            );

            response.put(
                "sourceCrs",
                sourceCrs.toString()
            );

            response.put(
                "targetCrs",
                "EPSG:4326"
            );

            response.put(
                "coordinateSystem",
                "WGS84"
            );

            response.put(
                "geoTiffFile",
                geoTiffFileName
            );

            response.put(
                "maskFile",
                maskFileName
            );

            response.put(
                "geoJsonFile",
                outputFileName
            );

            response.put(
                "path",
                outputPath.toString()
            );

            return response;

        } catch (Exception e) {

            throw new RuntimeException(
                "Failed to create GeoJSON: "
                    + e.getMessage(),
                e
            );

        } finally {

            if (coverage != null) {
                coverage.dispose(true);
            }

            if (reader != null) {
                reader.dispose();
            }
        }
    }

    private Map<String, Object> createGeometryJson(
        Geometry geometry
    ) {

        Map<String, Object> result =
            new LinkedHashMap<>();

        if (geometry instanceof Polygon polygon) {

            result.put(
                "type",
                "Polygon"
            );

            result.put(
                "coordinates",
                List.of(
                    createRing(
                        polygon
                            .getExteriorRing()
                            .getCoordinates()
                    )
                )
            );

            return result;
        }

        if (geometry instanceof MultiPolygon multiPolygon) {

            List<List<List<List<Double>>>> polygons =
                new ArrayList<>();

            for (
                int i = 0;
                i < multiPolygon.getNumGeometries();
                i++
            ) {

                Polygon polygon =
                    (Polygon)
                        multiPolygon.getGeometryN(i);

                List<List<Double>> ring =
                    createRing(
                        polygon
                            .getExteriorRing()
                            .getCoordinates()
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
                + geometry.getGeometryType()
        );
    }

    private List<List<Double>> createRing(
        Coordinate[] coordinates
    ) {

        List<List<Double>> ring =
            new ArrayList<>();

        for (
            Coordinate coordinate :
            coordinates
        ) {

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