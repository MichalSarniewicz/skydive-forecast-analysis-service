package com.skydiveforecast.infrastructure.weather.openmeteo;

import com.skydiveforecast.domain.model.Forecast;
import com.skydiveforecast.domain.model.WeatherPoint;
import com.skydiveforecast.domain.port.WeatherForecastPort;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class OpenMeteoWeatherAdapter implements WeatherForecastPort {

    private static final String BASE_URL = "https://api.open-meteo.com/v1/forecast";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final WebClient webClient;

    public OpenMeteoWeatherAdapter(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Forecast getHourlyForecast(double latitude, double longitude, LocalDate date) {
        URI uri = buildUri(latitude, longitude, date);
        OpenMeteoResponse response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(OpenMeteoResponse.class)
                .block();

        if (response == null || response.hourly == null || response.hourly.time == null) {
            return new Forecast(latitude, longitude, List.of());
        }

        List<WeatherPoint> points = new ArrayList<>();
        List<String> times = response.hourly.time;
        for (int i = 0; i < times.size(); i++) {
            LocalDateTime time = LocalDateTime.parse(times.get(i));
            double temperatureC = getDouble(response.hourly.temperature_2m, i, Double.NaN);
            double windSpeed = getDouble(response.hourly.wind_speed_10m, i, Double.NaN);
            double windGusts = getDouble(response.hourly.wind_gusts_10m, i, Double.NaN);
            int windDir = getInt(response.hourly.wind_direction_10m, i, 0);
            int cloud = getInt(response.hourly.cloud_cover, i, 0);
            double precip = getDouble(response.hourly.precipitation, i, 0.0);
            Integer visibility = getInteger(response.hourly.visibility, i);
            Double pressure = getDoubleObj(response.hourly.pressure_msl, i);

            points.add(new WeatherPoint(
                    time,
                    temperatureC,
                    windSpeed,
                    windGusts,
                    windDir,
                    cloud,
                    precip,
                    visibility,
                    pressure
            ));
        }

        return new Forecast(response.latitude, response.longitude, points);
    }

    private static URI buildUri(double latitude, double longitude, LocalDate date) {
        String hourlyVars = String.join(",",
                "temperature_2m",
                "wind_speed_10m",
                "wind_gusts_10m",
                "wind_direction_10m",
                "cloud_cover",
                "precipitation",
                "visibility",
                "pressure_msl"
        );

        return UriComponentsBuilder.newInstance()
                .scheme("https")
                .host("api.open-meteo.com")
                .path("/v1/forecast")
                .queryParam("latitude", latitude)
                .queryParam("longitude", longitude)
                .queryParam("hourly", hourlyVars)
                .queryParam("start_date", DATE_FORMAT.format(date))
                .queryParam("end_date", DATE_FORMAT.format(date))
                .queryParam("timezone", "auto")
                .build(true)
                .toUri();
    }

    private static double getDouble(List<Double> list, int index, double defaultValue) {
        if (list == null || index >= list.size() || list.get(index) == null) return defaultValue;
        return list.get(index);
    }

    private static Double getDoubleObj(List<Double> list, int index) {
        if (list == null || index >= list.size()) return null;
        return list.get(index);
    }

    private static int getInt(List<Integer> list, int index, int defaultValue) {
        if (list == null || index >= list.size() || list.get(index) == null) return defaultValue;
        return list.get(index);
    }

    private static Integer getInteger(List<Integer> list, int index) {
        if (list == null || index >= list.size()) return null;
        return list.get(index);
    }
}
