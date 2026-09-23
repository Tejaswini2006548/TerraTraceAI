package com.terratrace.service;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class GeometryValidationService {

    private final GeometryFactory geometryFactory =
        new GeometryFactory();

    public Map<String, Object> validatePolygon(
        double[][] coordinates
    ) {

        Polygon polygon =
            createPolygon(coordinates);

        IsValidOp validator =
            new IsValidOp(polygon);

        boolean valid =
            validator.isValid();

        Map<String, Object> result =
            new LinkedHashMap<>();

        result.put("valid", valid);
        result.put(
            "area",
            polygon.getArea()
        );
        result.put(
            "perimeter",
            polygon.getLength()
        );

        if (!valid) {
            result.put(
                "error",
                validator
                    .getValidationError()
                    .getMessage()
            );
        }

        return result;
    }

    public Map<String, Object> repairPolygon(
        double[][] coordinates
    ) {

        Polygon polygon =
            createPolygon(coordinates);

        boolean validBefore =
            polygon.isValid();

        Geometry repaired =
            GeometryFixer.fix(polygon);

        boolean validAfter =
            repaired.isValid();

        Map<String, Object> result =
            new LinkedHashMap<>();

        result.put(
            "validBefore",
            validBefore
        );

        result.put(
            "validAfter",
            validAfter
        );

        result.put(
            "geometryType",
            repaired.getGeometryType()
        );

        result.put(
            "area",
            repaired.getArea()
        );

        result.put(
            "perimeter",
            repaired.getLength()
        );

        result.put(
            "coordinateCount",
            repaired.getCoordinates().length
        );

        return result;
    }

    private Polygon createPolygon(
        double[][] coordinates
    ) {

        if (
            coordinates == null ||
            coordinates.length < 4
        ) {
            throw new IllegalArgumentException(
                "Polygon requires at least four coordinates"
            );
        }

        Coordinate[] jtsCoordinates =
            new Coordinate[coordinates.length];

        for (int i = 0; i < coordinates.length; i++) {

            if (
                coordinates[i] == null ||
                coordinates[i].length < 2
            ) {
                throw new IllegalArgumentException(
                    "Each coordinate must contain X and Y"
                );
            }

            jtsCoordinates[i] =
                new Coordinate(
                    coordinates[i][0],
                    coordinates[i][1]
                );
        }

        if (
            !jtsCoordinates[0]
                .equals2D(
                    jtsCoordinates[
                        jtsCoordinates.length - 1
                    ]
                )
        ) {
            Coordinate[] closedCoordinates =
                new Coordinate[
                    jtsCoordinates.length + 1
                ];

            System.arraycopy(
                jtsCoordinates,
                0,
                closedCoordinates,
                0,
                jtsCoordinates.length
            );

            closedCoordinates[
                closedCoordinates.length - 1
            ] =
                new Coordinate(
                    jtsCoordinates[0]
                );

            jtsCoordinates =
                closedCoordinates;
        }

        return geometryFactory.createPolygon(
            jtsCoordinates
        );
    }
}