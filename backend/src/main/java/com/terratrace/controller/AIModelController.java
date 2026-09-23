package com.terratrace.controller;

import com.terratrace.service.AIModelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/model")
public class AIModelController {

    private final AIModelService aiModelService;

    public AIModelController(
        AIModelService aiModelService
    ) {
        this.aiModelService = aiModelService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {

        return ResponseEntity.ok(
            Map.of(
                "modelPath",
                aiModelService.getModelPath(),
                "available",
                aiModelService.isModelAvailable(),
                "loaded",
                aiModelService.isModelLoaded(),
                "info",
                aiModelService.getModelInfo()
            )
        );
    }

    @PostMapping("/load")
    public ResponseEntity<Map<String, String>> load() {

        String result =
            aiModelService.loadModel();

        return ResponseEntity.ok(
            Map.of(
                "result",
                result
            )
        );
    }
}