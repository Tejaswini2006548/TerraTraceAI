package com.terratrace.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class GeoreferencingService {

    public Map<String, Object> pixelToMap(
        double pixelX,
        double pixelY,
        double originX,
        double originY,
        double pixelSizeX,
        double pixelSizeY
    ) {

        double mapX =
            originX + pixelX * pixelSizeX;

        double mapY =
            originY - pixelY * pixelSizeY;

        Map<String, Object> result =
            new LinkedHashMap<>();

        result.put(
            "pixelX",
            pixelX
        );

        result.put(
            "pixelY",
            pixelY
        );

        result.put(
            "mapX",
            mapX
        );

        result.put(
            "mapY",
            mapY
        );

        return result;
    }
}