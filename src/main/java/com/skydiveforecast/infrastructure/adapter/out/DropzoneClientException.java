package com.skydiveforecast.infrastructure.adapter.out;

public class DropzoneClientException extends RuntimeException {
    public DropzoneClientException(String message, Throwable cause) {
        super(message, cause);
    }
}