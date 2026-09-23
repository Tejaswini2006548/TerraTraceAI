package com.terratrace.service;

import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class MaskProcessingService {

    public String cleanBuildingMask(String maskFileName) {

        OpenCV.loadLocally();

        Path inputPath =
            Paths.get(
                "uploads",
                maskFileName
            );

        if (!Files.exists(inputPath)) {
            throw new IllegalArgumentException(
                "Segmentation mask not found"
            );
        }

        Mat mask =
            Imgcodecs.imread(
                inputPath.toString(),
                Imgcodecs.IMREAD_GRAYSCALE
            );

        if (mask.empty()) {
            throw new IllegalArgumentException(
                "Unable to read segmentation mask"
            );
        }

        Mat buildingMask =
            new Mat();

        Core.inRange(
            mask,
            new Scalar(136),
            new Scalar(136),
            buildingMask
        );

        Mat kernel =
            Imgproc.getStructuringElement(
                Imgproc.MORPH_RECT,
                new Size(5, 5)
            );

        Mat closedMask =
            new Mat();

        Imgproc.morphologyEx(
            buildingMask,
            closedMask,
            Imgproc.MORPH_CLOSE,
            kernel
        );

        Mat openedMask =
            new Mat();

        Imgproc.morphologyEx(
            closedMask,
            openedMask,
            Imgproc.MORPH_OPEN,
            kernel
        );

        List<MatOfPoint> contours =
            new ArrayList<>();

        Mat hierarchy =
            new Mat();

        Imgproc.findContours(
            openedMask,
            contours,
            hierarchy,
            Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_SIMPLE
        );

        Mat finalMask =
            Mat.zeros(
                openedMask.size(),
                openedMask.type()
            );

        double minimumArea = 100.0;

        for (MatOfPoint contour : contours) {

            double area =
                Imgproc.contourArea(
                    contour
                );

            if (area >= minimumArea) {

                Imgproc.drawContours(
                    finalMask,
                    List.of(contour),
                    -1,
                    new Scalar(255),
                    -1
                );
            }

            contour.release();
        }

        String outputFileName =
            "clean_building_mask.png";

        Path outputPath =
            Paths.get(
                "uploads",
                outputFileName
            );

        Imgcodecs.imwrite(
            outputPath.toString(),
            finalMask
        );

        mask.release();
        buildingMask.release();
        closedMask.release();
        openedMask.release();
        finalMask.release();
        kernel.release();
        hierarchy.release();

        return outputFileName;
    }
}