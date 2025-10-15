package com.skydiveforecast.infrastructure.weather.openmeteo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenMeteoResponse {
    public double latitude;
    public double longitude;
    public Hourly hourly;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Hourly {
        public List<String> time;
        public List<Double> temperature_2m;
        public List<Double> wind_speed_10m;
        public List<Double> wind_gusts_10m;
        public List<Integer> wind_direction_10m;
        public List<Integer> cloud_cover;
        public List<Double> precipitation;
        public List<Integer> visibility;
        public List<Double> pressure_msl;
    }
}
