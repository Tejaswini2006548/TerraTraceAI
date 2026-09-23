package com.terratrace.controller;

import com.terratrace.service.AIInferenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIInferenceService aiInferenceService;

    public AIController(
        AIInferenceService aiInferenceService
    ) {
        this.aiInferenceService = aiInferenceService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {

        return ResponseEntity.ok(
            Map.of(
                "runtime", "ONNX Runtime",
                "ready", aiInferenceService.isRuntimeReady(),
                "message", aiInferenceService.getRuntimeInfo()
            )
        );
    }
}