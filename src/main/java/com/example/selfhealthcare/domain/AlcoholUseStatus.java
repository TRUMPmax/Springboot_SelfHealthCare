package com.example.selfhealthcare.domain;

public enum AlcoholUseStatus {
    NEVER("不饮酒"),
    OCCASIONAL("偶尔饮酒"),
    WEEKLY("每周饮酒"),
    FREQUENT("频繁饮酒");

    private final String label;

    AlcoholUseStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
