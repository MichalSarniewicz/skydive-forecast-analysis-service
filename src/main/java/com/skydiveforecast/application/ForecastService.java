package com.skydiveforecast.application;

import com.skydiveforecast.domain.port.out.WeatherForecastPort;
import com.skydiveforecast.domain.model.Forecast;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;

@Service
public class ForecastService {

    private final WeatherForecastPort weatherForecastPort;

    public ForecastService(WeatherForecastPort weatherForecastPort) {
        this.weatherForecastPort = Objects.requireNonNull(weatherForecastPort);
    }

    public Forecast getForecast(double latitude, double longitude, LocalDate date) {
        return weatherForecastPort.getHourlyForecast(latitude, longitude, date);
    }
}
