package com.terratrace.service;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Service
public class AIModelService {

    private final OrtEnvironment environment;
    private OrtSession session;

    private final Path modelPath =
        Paths.get(
            "models",
            "segmentation",
            "terratrace-segmentation.onnx"
        );

    public AIModelService() {
        environment = OrtEnvironment.getEnvironment();
    }

    public boolean isModelAvailable() {
        return Files.exists(modelPath);
    }

    public boolean isModelLoaded() {
        return session != null;
    }

    public String getModelPath() {
        return modelPath.toString();
    }

    public String loadModel() {

        if (!Files.exists(modelPath)) {
            return "AI model file not found";
        }

        try {

            if (session != null) {
                session.close();
            }

            OrtSession.SessionOptions options =
                new OrtSession.SessionOptions();

            session =
                environment.createSession(
                    modelPath.toString(),
                    options
                );

            options.close();

            return "AI model loaded successfully";

        } catch (Exception e) {
            return "Failed to load AI model: "
                + e.getMessage();
        }
    }

    public String getModelInfo() {

        if (session == null) {
            return "AI model is not loaded";
        }

        try {

            String inputName =
                session.getInputNames()
                    .iterator()
                    .next();

            String outputName =
                session.getOutputNames()
                    .iterator()
                    .next();

            return "Input: "
                + inputName
                + ", Output: "
                + outputName;

        } catch (Exception e) {
            return "Unable to read model information";
        }
    }

    public Map<String, Object> getModelDetails() {

        if (session == null) {
            return Map.of(
                "loaded", false,
                "message", "AI model is not loaded"
            );
        }

        try {

            String inputName =
                session.getInputNames()
                    .iterator()
                    .next();

            String outputName =
                session.getOutputNames()
                    .iterator()
                    .next();

            return Map.of(
                "loaded", true,
                "inputName", inputName,
                "outputName", outputName,
                "inputShape", "(1, 3, 512, 512)",
                "outputShape", "(1, 5, 128, 128)",
                "classes", 5
            );

        } catch (Exception e) {

            return Map.of(
                "loaded", false,
                "message",
                "Unable to read model details"
            );
        }
    }
	public OrtSession getSession() {
    return session;
}

public OrtEnvironment getEnvironment() {
    return environment;
}
}