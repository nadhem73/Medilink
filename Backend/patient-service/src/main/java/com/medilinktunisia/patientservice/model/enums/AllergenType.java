package com.medilinktunisia.patientservice.model.enums;

public enum AllergenType {
    MEDICATION("Médicament"),
    FOOD("Aliment"),
    ENVIRONMENTAL("Environnemental"),
    LATEX("Latex"),
    OTHER("Autre");

    private final String displayName;

    AllergenType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
