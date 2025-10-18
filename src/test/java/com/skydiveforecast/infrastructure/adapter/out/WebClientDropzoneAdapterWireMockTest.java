package com.skydiveforecast.infrastructure.adapter.out;

import com.skydiveforecast.domain.model.Dropzone;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.skydiveforecast.infrastructure.adapter.out.JsonFileLoader.loadWiremockDropzoneJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest(httpPort = 8089)
class WebClientDropzoneAdapterWireMockTest {

    private WebClientDropzoneAdapter adapter;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:8089")
                .build();

        adapter = new WebClientDropzoneAdapter(webClient);
        ReflectionTestUtils.setField(adapter, "locationServiceUrl", "");

        jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test";
    }

    @Test
    void shouldFetchDropzonesWithJwtToken() {
        // Arrange
        stubFor(get(urlEqualTo("/api/locations/dropzones"))
                .withHeader("Authorization", equalTo("Bearer " + jwtToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadWiremockDropzoneJson("three-dropzones-response.json"))));

        // Act
        List<Dropzone> dropzones = adapter.getDropzones(jwtToken);

        // Assert
        assertThat(dropzones).hasSize(3);

        Dropzone firstDropzone = dropzones.get(0);
        assertThat(firstDropzone.getId()).isEqualTo(1L);
        assertThat(firstDropzone.getName()).isEqualTo("Skydive Warsaw");
        assertThat(firstDropzone.getCity()).isEqualTo("Warsaw");
        assertThat(firstDropzone.getLatitude()).isEqualByComparingTo(new BigDecimal("52.1657"));
        assertThat(firstDropzone.getLongitude()).isEqualByComparingTo(new BigDecimal("20.9671"));
        assertThat(firstDropzone.getIsWingsuitFriendly()).isTrue();

        Dropzone thirdDropzone = dropzones.get(2);
        assertThat(thirdDropzone.getName()).isEqualTo("Skydive Gdansk");
        assertThat(thirdDropzone.getCity()).isEqualTo("Gdansk");
        assertThat(thirdDropzone.getIsWingsuitFriendly()).isFalse();

        verify(getRequestedFor(urlEqualTo("/api/locations/dropzones"))
                .withHeader("Authorization", equalTo("Bearer " + jwtToken)));
    }

    @Test
    void shouldHandleEmptyDropzonesList() {
        // Arrange
        stubFor(get(urlEqualTo("/api/locations/dropzones"))
                .withHeader("Authorization", equalTo("Bearer " + jwtToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadWiremockDropzoneJson("empty-list-response.json"))));

        // Act
        List<Dropzone> dropzones = adapter.getDropzones(jwtToken);

        // Assert
        assertThat(dropzones).isEmpty();
    }

    @Test
    void shouldHandleWingsuitFriendlyDropzones() {
        // Arrange
        stubFor(get(urlEqualTo("/api/locations/dropzones"))
                .withHeader("Authorization", equalTo("Bearer " + jwtToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadWiremockDropzoneJson("wingsuit-friendly-response.json"))));

        // Act
        List<Dropzone> dropzones = adapter.getDropzones(jwtToken);

        // Assert
        List<Dropzone> wingsuitFriendly = dropzones.stream()
                .filter(d -> Boolean.TRUE.equals(d.getIsWingsuitFriendly()))
                .toList();

        assertThat(wingsuitFriendly).hasSize(1);
        assertThat(wingsuitFriendly.get(0).getName()).isEqualTo("Expert Zone");
        assertThat(wingsuitFriendly.get(0).getCity()).isEqualTo("Wroclaw");
    }

    @Test
    void shouldHandleNullWingsuitFriendlyField() {
        // Arrange
        stubFor(get(urlEqualTo("/api/locations/dropzones"))
                .withHeader("Authorization", equalTo("Bearer " + jwtToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadWiremockDropzoneJson("null-wingsuit-field-response.json"))));

        // Act
        List<Dropzone> dropzones = adapter.getDropzones(jwtToken);

        // Assert
        assertThat(dropzones).hasSize(1);
        Dropzone dropzone = dropzones.get(0);
        assertThat(dropzone.getName()).isEqualTo("Unknown Zone");
        assertThat(dropzone.getIsWingsuitFriendly()).isNull();
    }

    @Test
    void shouldThrowExceptionWhenUnauthorized() {
        // Arrange
        stubFor(get(urlEqualTo("/api/locations/dropzones"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadWiremockDropzoneJson("unauthorized-response.json"))));

        // Act & Assert
        assertThatThrownBy(() -> adapter.getDropzones(jwtToken))
                .isInstanceOf(DropzoneClientException.class)
                .hasMessageContaining("Failed to fetch dropzones");
    }

    @Test
    void shouldThrowExceptionWhenServiceUnavailable() {
        // Arrange
        stubFor(get(urlEqualTo("/api/locations/dropzones"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withBody("Service Temporarily Unavailable")));

        // Act & Assert
        assertThatThrownBy(() -> adapter.getDropzones(jwtToken))
                .isInstanceOf(DropzoneClientException.class);
    }

    @Test
    void shouldHandleNetworkDelaySuccessfully() {
        // Arrange
        stubFor(get(urlEqualTo("/api/locations/dropzones"))
                .withHeader("Authorization", equalTo("Bearer " + jwtToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withFixedDelay(1000)
                        .withBody(loadWiremockDropzoneJson("delayed-response.json"))));

        // Act
        List<Dropzone> dropzones = adapter.getDropzones(jwtToken);

        // Assert
        assertThat(dropzones).hasSize(1);
        assertThat(dropzones.get(0).getName()).isEqualTo("Delayed Response Zone");
    }

    @Test
    void shouldHandlePreciseCoordinates() {
        // Arrange
        stubFor(get(urlEqualTo("/api/locations/dropzones"))
                .withHeader("Authorization", equalTo("Bearer " + jwtToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadWiremockDropzoneJson("precise-coordinates-response.json"))));

        // Act
        List<Dropzone> dropzones = adapter.getDropzones(jwtToken);

        // Assert
        assertThat(dropzones).hasSize(1);
        Dropzone dropzone = dropzones.get(0);
        assertThat(dropzone.getLatitude()).isEqualByComparingTo(new BigDecimal("53.428544"));
        assertThat(dropzone.getLongitude()).isEqualByComparingTo(new BigDecimal("14.552812"));
    }
}