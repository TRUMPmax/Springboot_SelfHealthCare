package com.example.selfhealthcare.domain;

public enum Gender {
    MALE("男"),
    FEMALE("女"),
    OTHER("其他");

    private final String label;

    Gender(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
