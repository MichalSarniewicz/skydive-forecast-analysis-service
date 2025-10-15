package com.skydiveforecast.domain.model;

import java.util.List;

public record Forecast(
        double latitude,
        double longitude,
        List<WeatherPoint> hours
) {}
