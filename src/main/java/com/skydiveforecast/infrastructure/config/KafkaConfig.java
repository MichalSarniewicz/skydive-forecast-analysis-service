package com.skydiveforecast.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic weatherReportRequestsTopic() {
        return TopicBuilder.name("weather-report-requests")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic deadLetterTopic() {
        return TopicBuilder.name("weather-report-requests-dlq")
                .partitions(1)
                .replicas(1)
                .build();
    }
}