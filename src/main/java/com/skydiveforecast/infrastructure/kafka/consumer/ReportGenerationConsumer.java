package com.skydiveforecast.infrastructure.kafka.consumer;

import com.skydiveforecast.infrastructure.adapter.in.web.dto.WeatherReportRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;

@Service
@RequiredArgsConstructor
public class ReportGenerationConsumer {

    @KafkaListener(topics = "weather-report-requests", groupId = "weather-report-group")
    @RetryableTopic(attempts = "3", backoff = @Backoff(delay = 5000))
    public void processReportRequest(@Payload WeatherReportRequestDto request,
                                     @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        //TODO send it to proper service
    }
}