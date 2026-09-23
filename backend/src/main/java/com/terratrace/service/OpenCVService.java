package com.terratrace.service;

import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.stereotype.Service;

@Service
public class OpenCVService {

    private boolean loaded;

    public OpenCVService() {
        try {
            OpenCV.loadLocally();
            loaded = true;
        } catch (Exception e) {
            loaded = false;
        }
    }

    public boolean isLoaded() {
        return loaded;
    }

    public String analyzeImage(String imagePath) {

        if (!loaded) {
            return "OpenCV is not loaded";
        }

        Mat image = Imgcodecs.imread(imagePath);

        if (image.empty()) {
            return "Unable to read image";
        }

        return "Image loaded successfully: "
                + image.cols()
                + " x "
                + image.rows()
                + " pixels";
    }
}