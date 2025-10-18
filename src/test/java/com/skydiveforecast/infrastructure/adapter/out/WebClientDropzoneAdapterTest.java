package com.skydiveforecast.infrastructure.adapter.out;

import com.skydiveforecast.domain.model.Dropzone;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static com.skydiveforecast.infrastructure.adapter.out.JsonFileLoader.loadMockwebserverDropzoneJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WebClientDropzoneAdapterTest {

    private static final String JWT_TOKEN = "test-jwt-token-12345";

    private MockWebServer mockWebServer;
    private WebClientDropzoneAdapter adapter;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        adapter = new WebClientDropzoneAdapter(webClient);
        ReflectionTestUtils.setField(adapter, "locationServiceUrl", "");
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldFetchDropzonesSuccessfully() throws InterruptedException {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody(loadMockwebserverDropzoneJson("two-dropzones-response.json"))
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200));

        // Act
        List<Dropzone> result = adapter.getDropzones(JWT_TOKEN);

        // Assert
        assertThat(result).hasSize(2);

        Dropzone firstDropzone = result.get(0);
        assertThat(firstDropzone.getId()).isEqualTo(1L);
        assertThat(firstDropzone.getName()).isEqualTo("Skydive Warsaw");
        assertThat(firstDropzone.getCity()).isEqualTo("Warsaw");
        assertThat(firstDropzone.getLatitude()).isEqualByComparingTo(new BigDecimal("52.2297"));
        assertThat(firstDropzone.getLongitude()).isEqualByComparingTo(new BigDecimal("21.0122"));
        assertThat(firstDropzone.getIsWingsuitFriendly()).isTrue();

        Dropzone secondDropzone = result.get(1);
        assertThat(secondDropzone.getName()).isEqualTo("Skydive Krakow");
        assertThat(secondDropzone.getIsWingsuitFriendly()).isFalse();

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer " + JWT_TOKEN);
        assertThat(request.getPath()).isEqualTo("/api/locations/dropzones");
    }

    @Test
    void shouldHandleEmptyList() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody(loadMockwebserverDropzoneJson("empty-list-response.json"))
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200));

        // Act
        List<Dropzone> result = adapter.getDropzones(JWT_TOKEN);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void shouldHandleDropzonesWithNullableFields() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody(loadMockwebserverDropzoneJson("minimal-dropzone-response.json"))
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200));

        // Act
        List<Dropzone> result = adapter.getDropzones(JWT_TOKEN);

        // Assert
        assertThat(result).hasSize(1);
        Dropzone dropzone = result.get(0);
        assertThat(dropzone.getName()).isEqualTo("Minimal Dropzone");
        assertThat(dropzone.getIsWingsuitFriendly()).isNull();
    }

    @Test
    void shouldThrowExceptionWhenUnauthorized() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("Unauthorized"));

        // Act & Assert
        assertThatThrownBy(() -> adapter.getDropzones(JWT_TOKEN))
                .isInstanceOf(DropzoneClientException.class)
                .hasMessageContaining("Failed to fetch dropzones");
    }

    @Test
    void shouldThrowExceptionWhenNotFound() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("Not found"));

        // Act & Assert
        assertThatThrownBy(() -> adapter.getDropzones(JWT_TOKEN))
                .isInstanceOf(DropzoneClientException.class)
                .hasMessageContaining("Failed to fetch dropzones");
    }

    @Test
    void shouldThrowExceptionWhenInternalServerError() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal server error"));

        // Act & Assert
        assertThatThrownBy(() -> adapter.getDropzones(JWT_TOKEN))
                .isInstanceOf(DropzoneClientException.class)
                .hasMessageContaining("Failed to fetch dropzones");
    }

    @Test
    void shouldHandleWingsuitFriendlyDropzones() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody(loadMockwebserverDropzoneJson("wingsuit-mix-response.json"))
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200));

        // Act
        List<Dropzone> result = adapter.getDropzones(JWT_TOKEN);

        // Assert
        List<Dropzone> wingsuitFriendly = result.stream()
                .filter(d -> Boolean.TRUE.equals(d.getIsWingsuitFriendly()))
                .toList();

        assertThat(wingsuitFriendly).hasSize(1);
        assertThat(wingsuitFriendly.get(0).getName()).isEqualTo("Pro Dropzone");
    }
}