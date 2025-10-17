package com.skydiveforecast.domain.service;
import com.skydiveforecast.infrastructure.adapter.in.web.dto.WeatherReportRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final String KAFKA_TOPIC_NAME = "weather-report-requests";

    private final KafkaTemplate<String, WeatherReportRequestDto> kafkaTemplate;

    public String submitReportRequest(LocalDate start, LocalDate end, List<String> airports) {
        String requestId = UUID.randomUUID().toString();
        WeatherReportRequestDto request = new WeatherReportRequestDto();
        request.setRequestId(requestId);
        request.setStartDate(start);
        request.setEndDate(end);
        request.setAirportCodes(airports);

        kafkaTemplate.send(KAFKA_TOPIC_NAME, requestId, request);

        return requestId;
    }
}