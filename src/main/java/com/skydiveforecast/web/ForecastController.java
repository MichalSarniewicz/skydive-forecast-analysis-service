package com.skydiveforecast.web;

import com.skydiveforecast.application.ForecastService;
import com.skydiveforecast.domain.model.Forecast;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/forecast")
@Tag(name = "Forecast", description = "Endpoints for getting forecasts.")
@RequiredArgsConstructor
public class ForecastController {

    private final ForecastService forecastService;

    @GetMapping
    @Operation(summary = "Get forecasts",
            description = "Get forecast for a given location and date.", tags = {"Forecast"})
    public Forecast getForecasts(
            @RequestParam("lat") double latitude,
            @RequestParam("lon") double longitude,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return forecastService.getForecast(latitude, longitude, date);
    }
}
