package com.skydiveforecast.domain.model;

import java.time.LocalDateTime;

public record WeatherPoint(
        LocalDateTime time,
        double temperatureC,
        double windSpeed10m,
        double windGusts10m,
        int windDirectionDeg,
        int cloudCoverPct,
        double precipitationMm,
        Integer visibilityM,
        Double pressureHpa
) {}
