package com.terratrace.service;

import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImagePreprocessingService {

    public String preprocessImage(String fileName) {

        OpenCV.loadLocally();

        Path inputPath = Paths.get(
            "uploads",
            fileName
        );

        if (!Files.exists(inputPath)) {
            return "Image not found";
        }

        Mat image = Imgcodecs.imread(
            inputPath.toString()
        );

        if (image.empty()) {
            return "Unable to read image";
        }

        Mat resized = new Mat();

        Imgproc.resize(
            image,
            resized,
            new Size(1024, 1024)
        );

        String outputFileName =
            "processed_" + fileName;

        Path outputPath = Paths.get(
            "uploads",
            outputFileName
        );

        Imgcodecs.imwrite(
            outputPath.toString(),
            resized
        );

        return outputFileName;
    }
}