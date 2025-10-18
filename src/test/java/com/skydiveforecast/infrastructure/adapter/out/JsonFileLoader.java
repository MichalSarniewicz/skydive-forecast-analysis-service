package com.skydiveforecast.infrastructure.adapter.out;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JsonFileLoader {

    private static final String WIREMOCK_BASE_PATH = "wiremock/dropzones/";
    private static final String MOCKWEBSERVER_BASE_PATH = "mockwebserver/dropzones/";

    public static String loadWiremockDropzoneJson(String fileName) {
        return loadJson(WIREMOCK_BASE_PATH + fileName);
    }

    public static String loadMockwebserverDropzoneJson(String fileName) {
        return loadJson(MOCKWEBSERVER_BASE_PATH + fileName);
    }

    public static String loadJson(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load JSON file: " + path, e);
        }
    }
}