package com.skydiveforecast.domain.port.out;

import com.skydiveforecast.domain.model.Dropzone;

import java.util.List;

public interface DropzoneClient {
    List<Dropzone> getDropzones(String jwtToken);
}