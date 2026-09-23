package com.terratrace.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageProcessingService {

    private final Path uploadDirectory =
        Paths.get("uploads");

    public Map<String, Object> saveImage(
        MultipartFile file
    ) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                "Image file is required"
            );
        }

        String originalName =
            file.getOriginalFilename();

        if (originalName == null) {
            throw new IllegalArgumentException(
                "Invalid file name"
            );
        }

        String lowerName =
            originalName.toLowerCase();

        if (
            !lowerName.endsWith(".jpg") &&
            !lowerName.endsWith(".jpeg") &&
            !lowerName.endsWith(".png") &&
            !lowerName.endsWith(".tif") &&
            !lowerName.endsWith(".tiff")
        ) {
            throw new IllegalArgumentException(
                "Unsupported image format"
            );
        }

        try {

            Files.createDirectories(
                uploadDirectory
            );

            String extension =
                originalName.substring(
                    originalName.lastIndexOf(".")
                );

            String fileName =
                UUID.randomUUID() + extension;

            Path target =
                uploadDirectory.resolve(fileName);

            Files.copy(
                file.getInputStream(),
                target
            );

            return Map.of(
                "status", "success",
                "message", "Image uploaded successfully",
                "fileName", fileName,
                "originalName", originalName,
                "size", file.getSize(),
                "path", target.toString()
            );

        } catch (IOException e) {

            throw new RuntimeException(
                "Failed to save image",
                e
            );
        }
    }
}