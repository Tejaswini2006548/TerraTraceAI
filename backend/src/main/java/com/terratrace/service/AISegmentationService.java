package com.terratrace.service;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtSession;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class AISegmentationService {

    private final AIModelService aiModelService;

    public AISegmentationService(
        AIModelService aiModelService
    ) {
        this.aiModelService = aiModelService;
    }

    public Map<String, Object> segmentImage(
        String fileName
    ) {

        try {

            if (!aiModelService.isModelLoaded()) {
                throw new IllegalStateException(
                    "AI model is not loaded"
                );
            }

            File imageFile =
                Paths.get(
                    "uploads",
                    fileName
                ).toFile();

            if (!imageFile.exists()) {
                throw new IllegalArgumentException(
                    "Image not found"
                );
            }

            BufferedImage original =
                ImageIO.read(imageFile);

            if (original == null) {
                throw new IllegalArgumentException(
                    "Unable to read image"
                );
            }

            int originalWidth =
                original.getWidth();

            int originalHeight =
                original.getHeight();

            Image resizedImage =
                original.getScaledInstance(
                    512,
                    512,
                    Image.SCALE_SMOOTH
                );

            BufferedImage image =
                new BufferedImage(
                    512,
                    512,
                    BufferedImage.TYPE_INT_RGB
                );

            Graphics2D graphics =
                image.createGraphics();

            graphics.drawImage(
                resizedImage,
                0,
                0,
                null
            );

            graphics.dispose();

            float[][][][] input =
                new float[1][3][512][512];

            for (int y = 0; y < 512; y++) {

                for (int x = 0; x < 512; x++) {

                    int rgb =
                        image.getRGB(x, y);

                    int red =
                        (rgb >> 16) & 0xFF;

                    int green =
                        (rgb >> 8) & 0xFF;

                    int blue =
                        rgb & 0xFF;

                    input[0][0][y][x] =
                        red / 255.0f;

                    input[0][1][y][x] =
                        green / 255.0f;

                    input[0][2][y][x] =
                        blue / 255.0f;
                }
            }

            OrtSession session =
                aiModelService.getSession();

            String inputName =
                session.getInputNames()
                    .iterator()
                    .next();

            try (
                OnnxTensor tensor =
                    OnnxTensor.createTensor(
                        aiModelService.getEnvironment(),
                        input
                    )
            ) {

                try (
                    OrtSession.Result result =
                        session.run(
                            Map.of(
                                inputName,
                                tensor
                            )
                        )
                ) {

                    Object output =
                        result.get(0).getValue();

                    float[][][][] logits =
                        (float[][][][]) output;

                    int height =
                        logits[0][0].length;

                    int width =
                        logits[0][0][0].length;

                    int[] classCounts =
                        new int[5];

                    int[][] prediction =
                        new int[height][width];

                    for (int y = 0; y < height; y++) {

                        for (int x = 0; x < width; x++) {

                            int bestClass = 0;

                            float bestValue =
                                logits[0][0][y][x];

                            for (
                                int c = 1;
                                c < 5;
                                c++
                            ) {

                                float value =
                                    logits[0][c][y][x];

                                if (
                                    value > bestValue
                                ) {

                                    bestValue =
                                        value;

                                    bestClass =
                                        c;
                                }
                            }

                            prediction[y][x] =
                                bestClass;

                            classCounts[bestClass]++;
                        }
                    }

                    BufferedImage smallMask =
                        new BufferedImage(
                            width,
                            height,
                            BufferedImage.TYPE_BYTE_GRAY
                        );

                    for (int y = 0; y < height; y++) {

                        for (int x = 0; x < width; x++) {

                            int classId =
                                prediction[y][x];

                            int value =
                                classId * 63;

                            smallMask
                                .getRaster()
                                .setSample(
                                    x,
                                    y,
                                    0,
                                    value
                                );
                        }
                    }

                    BufferedImage fullMask =
                        new BufferedImage(
                            originalWidth,
                            originalHeight,
                            BufferedImage.TYPE_BYTE_GRAY
                        );

                    Graphics2D maskGraphics =
                        fullMask.createGraphics();

                    maskGraphics.drawImage(
                        smallMask.getScaledInstance(
                            originalWidth,
                            originalHeight,
                            Image.SCALE_REPLICATE
                        ),
                        0,
                        0,
                        null
                    );

                    maskGraphics.dispose();

                    String maskFileName =
    "segmentation_"
    + fileName.substring(
        0,
        fileName.lastIndexOf(".")
    )
    + ".png";

                    File maskFile =
                        Paths.get(
                            "uploads",
                            maskFileName
                        ).toFile();

                    ImageIO.write(
                        fullMask,
                        "png",
                        maskFile
                    );

                    Map<String, Object> response =
                        new HashMap<>();

                    response.put(
                        "status",
                        "success"
                    );

                    response.put(
                        "inputWidth",
                        512
                    );

                    response.put(
                        "inputHeight",
                        512
                    );

                    response.put(
                        "outputWidth",
                        width
                    );

                    response.put(
                        "outputHeight",
                        height
                    );

                    response.put(
                        "originalWidth",
                        originalWidth
                    );

                    response.put(
                        "originalHeight",
                        originalHeight
                    );

                    response.put(
                        "backgroundPixels",
                        classCounts[0]
                    );

                    response.put(
                        "buildingPixels",
                        classCounts[1]
                    );

                    response.put(
                        "roadPixels",
                        classCounts[2]
                    );

                    response.put(
                        "waterPixels",
                        classCounts[3]
                    );

                    response.put(
                        "openPlotPixels",
                        classCounts[4]
                    );

                    response.put(
                        "maskFile",
                        maskFileName
                    );

                    response.put(
                        "maskPath",
                        maskFile.getPath()
                    );

                    return response;
                }
            }

        } catch (Exception e) {

            throw new RuntimeException(
                "AI segmentation failed: "
                    + e.getMessage(),
                e
            );
        }
    }
}