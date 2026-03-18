package com.example.selfhealthcare.dto;

import java.math.BigDecimal;
import java.util.List;

public record TrendMetricResponse(
        String metricCode,
        String metricName,
        String unit,
        BigDecimal latestValue,
        BigDecimal previousValue,
        BigDecimal changeValue,
        String direction,
        String interpretation,
        List<TrendPointResponse> points) {
}
