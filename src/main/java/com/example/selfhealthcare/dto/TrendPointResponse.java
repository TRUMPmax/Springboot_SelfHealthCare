package com.example.selfhealthcare.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TrendPointResponse(LocalDate recordDate, BigDecimal value) {
}
