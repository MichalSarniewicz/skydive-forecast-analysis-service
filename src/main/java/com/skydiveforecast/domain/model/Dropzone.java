package com.skydiveforecast.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dropzone {
    private Long id;
    private String name;
    private String city;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean isWingsuitFriendly;
}