package com.medilinktunisia.patientservice.model.enums;

public enum SeverityLevel {
    LOW("Faible"),
    MEDIUM("Modéré"),
    HIGH("Élevé"),
    CRITICAL("Critique");

    private final String displayName;

    SeverityLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
