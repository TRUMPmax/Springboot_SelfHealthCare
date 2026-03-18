package com.example.selfhealthcare.domain;

public enum BloodType {
    A("A型"),
    B("B型"),
    AB("AB型"),
    O("O型"),
    UNKNOWN("未知");

    private final String label;

    BloodType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
