package com.skydiveforecast.infrastructure.adapter.in.web;

import com.skydiveforecast.domain.service.AiService;
import com.skydiveforecast.infrastructure.adapter.in.web.dto.AnalysisRequest;
import com.skydiveforecast.infrastructure.adapter.in.web.dto.AnalysisResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analyses/ai")
@Tag(name = "AI forecast", description = "Endpoints for getting ai forecasts.")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping
    @Operation(summary = "Analyze forecast", description = "Analyzes forecast")
    public AnalysisResponse analyze(@RequestBody AnalysisRequest request) {
        return aiService.analyze(request);
    }
}