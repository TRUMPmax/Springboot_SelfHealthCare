package com.example.selfhealthcare.domain;

public enum RiskLevel {
    LOW("低风险", 1),
    MEDIUM("中风险", 2),
    HIGH("高风险", 3),
    CRITICAL("极高风险", 4);

    private final String label;
    private final int weight;

    RiskLevel(String label, int weight) {
        this.label = label;
        this.weight = weight;
    }

    public String getLabel() {
        return label;
    }

    public int getWeight() {
        return weight;
    }
}
