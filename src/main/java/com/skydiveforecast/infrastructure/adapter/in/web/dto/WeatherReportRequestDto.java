package com.skydiveforecast.infrastructure.adapter.in.web.dto;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class WeatherReportRequestDto {
    private String requestId;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> airportCodes;
    private String userId;
}