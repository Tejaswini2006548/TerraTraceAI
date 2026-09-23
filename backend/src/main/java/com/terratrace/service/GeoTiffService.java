package com.terratrace.service;

import org.geotools.api.geometry.Position;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.GridCoordinates2D;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class GeoTiffService {

    public Map<String, Object> readMetadata(
        String fileName
    ) {

        Path inputPath =
            Paths.get(
                "uploads",
                fileName
            );

        if (!Files.exists(inputPath)) {
            throw new IllegalArgumentException(
                "GeoTIFF file not found"
            );
        }

        GeoTiffReader reader = null;
        GridCoverage2D coverage = null;

        try {

            File file =
                inputPath.toFile();

            reader =
                new GeoTiffReader(file);

            coverage =
                reader.read(null);

            if (coverage == null) {
                throw new IllegalArgumentException(
                    "Unable to read GeoTIFF"
                );
            }

            int width =
                coverage
                    .getRenderedImage()
                    .getWidth();

            int height =
                coverage
                    .getRenderedImage()
                    .getHeight();

            CoordinateReferenceSystem crs =
                coverage
                    .getCoordinateReferenceSystem2D();

            String crsWkt =
                crs.toWKT();

            double minX =
                coverage
                    .getEnvelope2D()
                    .getMinX();

            double minY =
                coverage
                    .getEnvelope2D()
                    .getMinY();

            double maxX =
                coverage
                    .getEnvelope2D()
                    .getMaxX();

            double maxY =
                coverage
                    .getEnvelope2D()
                    .getMaxY();

            double pixelSizeX =
                (maxX - minX)
                    / width;

            double pixelSizeY =
                (maxY - minY)
                    / height;

            GridGeometry2D gridGeometry =
                coverage
                    .getGridGeometry();

            Map<String, Object> origin =
                createWorldPoint(
                    gridGeometry,
                    0,
                    0
                );

            Map<String, Object> topRight =
    createWorldPoint(
        gridGeometry,
        width - 1,
        0
    );

Map<String, Object> bottomLeft =
    createWorldPoint(
        gridGeometry,
        0,
        height - 1
    );

            Map<String, Object> pixelSize =
                new LinkedHashMap<>();

            pixelSize.put(
                "x",
                pixelSizeX
            );

            pixelSize.put(
                "y",
                pixelSizeY
            );

            Map<String, Object> response =
                new LinkedHashMap<>();

            response.put(
                "status",
                "success"
            );

            response.put(
                "fileName",
                fileName
            );

            response.put(
                "width",
                width
            );

            response.put(
                "height",
                height
            );

            response.put(
                "minX",
                minX
            );

            response.put(
                "minY",
                minY
            );

            response.put(
                "maxX",
                maxX
            );

            response.put(
                "maxY",
                maxY
            );

            response.put(
                "pixelSize",
                pixelSize
            );

            response.put(
                "origin",
                origin
            );

            response.put(
                "topRight",
                topRight
            );

            response.put(
                "bottomLeft",
                bottomLeft
            );

            response.put(
                "crs",
                crsWkt
            );

            response.put(
                "georeferenced",
                true
            );

            return response;

        } catch (Exception e) {

            throw new RuntimeException(
                "Failed to read GeoTIFF metadata: "
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

    private Map<String, Object> createWorldPoint(
    GridGeometry2D gridGeometry,
    int pixelX,
    int pixelY
) throws Exception {

        Position position =
            gridGeometry.gridToWorld(
                new GridCoordinates2D(
                    pixelX,
                    pixelY
                )
            );

        double[] coordinates =
            position.getCoordinate();

        Map<String, Object> point =
            new LinkedHashMap<>();

        point.put(
            "x",
            coordinates[0]
        );

        point.put(
            "y",
            coordinates[1]
        );

        point.put(
            "pixelX",
            pixelX
        );

        point.put(
            "pixelY",
            pixelY
        );

        return point;
    }
}