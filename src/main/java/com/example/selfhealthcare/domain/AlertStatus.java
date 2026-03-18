package com.example.selfhealthcare.domain;

public enum AlertStatus {
    PENDING("待处理"),
    REVIEWED("已查看"),
    RESOLVED("已处置");

    private final String label;

    AlertStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
