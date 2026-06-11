package com.medilinktunisia.patientservice.model.enums;

public enum AppointmentStatus {
    SCHEDULED("Planifié"),
    CONFIRMED("Confirmé"),
    COMPLETED("Terminé"),
    CANCELLED("Annulé"),
    NO_SHOW("Absent");

    private final String displayName;

    AppointmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
