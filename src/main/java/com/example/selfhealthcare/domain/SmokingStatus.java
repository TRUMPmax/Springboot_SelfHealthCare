package com.example.selfhealthcare.domain;

public enum SmokingStatus {
    NEVER("从不吸烟"),
    FORMER("已戒烟"),
    OCCASIONAL("偶尔吸烟"),
    CURRENT("经常吸烟");

    private final String label;

    SmokingStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
