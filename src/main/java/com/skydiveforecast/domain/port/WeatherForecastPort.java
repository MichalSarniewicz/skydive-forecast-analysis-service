package com.skydiveforecast.domain.port;

import com.skydiveforecast.domain.model.Forecast;

import java.time.LocalDate;

public interface WeatherForecastPort {
    Forecast getHourlyForecast(double latitude, double longitude, LocalDate date);
}
