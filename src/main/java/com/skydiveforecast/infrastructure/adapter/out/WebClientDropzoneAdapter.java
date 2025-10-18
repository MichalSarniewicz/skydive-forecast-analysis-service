package com.skydiveforecast.infrastructure.adapter.out;

import com.skydiveforecast.domain.model.Dropzone;
import com.skydiveforecast.domain.port.out.DropzoneClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebClientDropzoneAdapter implements DropzoneClient {

    private final WebClient webClient;
    
    @Value("${location.service.url}")
    private String locationServiceUrl;

    @Override
    //TODO configure cache
    public List<Dropzone> getDropzones(String jwtToken) {
        log.info("Fetching dropzones from location service");
        
        try {
            List<Dropzone> dropzones = webClient
                .get()
                .uri(locationServiceUrl + "/api/locations/dropzones")
                .header("Authorization", "Bearer " + jwtToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Dropzone>>() {})
                .block();
            
            log.info("Successfully fetched {} dropzones", dropzones != null ? dropzones.size() : 0);
            return dropzones;
            
        } catch (WebClientResponseException e) {
            log.error("Error fetching dropzones: status={}, body={}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new DropzoneClientException("Failed to fetch dropzones: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error fetching dropzones", e);
            throw new DropzoneClientException("Unexpected error: " + e.getMessage(), e);
        }
    }
}