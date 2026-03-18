package com.example.selfhealthcare.domain;

public enum AlertSeverity {
    LOW("轻度"),
    MEDIUM("中度"),
    HIGH("重度"),
    CRITICAL("危急");

    private final String label;

    AlertSeverity(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public RiskLevel toRiskLevel() {
        return switch (this) {
            case LOW -> RiskLevel.LOW;
            case MEDIUM -> RiskLevel.MEDIUM;
            case HIGH -> RiskLevel.HIGH;
            case CRITICAL -> RiskLevel.CRITICAL;
        };
    }
}
