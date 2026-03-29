package com.example.selfhealthcare.dto;

import java.math.BigDecimal;
import java.util.List;

public record VisualizationSeriesResponse(
        String metricCode,
        String metricName,
        String unit,
        BigDecimal latestValue,
        BigDecimal averageValue,
        List<TrendPointResponse> points) {
}
