package com.terratrace.service;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CrsTransformationService {

    public Map<String, Object> transform(
        double x,
        double y,
        String sourceEpsg,
        String targetEpsg
    ) {
        try {
            CoordinateReferenceSystem sourceCrs =
                CRS.decode(sourceEpsg, true);

            CoordinateReferenceSystem targetCrs =
                CRS.decode(targetEpsg, true);

            MathTransform transform =
                CRS.findMathTransform(
                    sourceCrs,
                    targetCrs,
                    true
                );

            Coordinate source =
                new Coordinate(x, y);

            Coordinate target =
                JTS.transform(
                    source,
                    null,
                    transform
                );

            Map<String, Object> result =
                new LinkedHashMap<>();

            result.put("status", "success");
            result.put("sourceCrs", sourceEpsg);
            result.put("targetCrs", targetEpsg);
            result.put("sourceX", x);
            result.put("sourceY", y);
            result.put("targetX", target.x);
            result.put("targetY", target.y);

            return result;

        } catch (Exception e) {
            throw new RuntimeException(
                "CRS transformation failed: " + e.getMessage(),
                e
            );
        }
    }
}