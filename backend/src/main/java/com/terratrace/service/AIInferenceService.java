package com.terratrace.service;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import org.springframework.stereotype.Service;

@Service
public class AIInferenceService {

    private final OrtEnvironment environment;
    private OrtSession session;

    public AIInferenceService() {
        environment = OrtEnvironment.getEnvironment();
    }

    public boolean isRuntimeReady() {
        return environment != null;
    }

    public String getRuntimeInfo() {
        return "ONNX Runtime initialized successfully";
    }
}