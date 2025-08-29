package fr.medilabo.solutions.rapport.dto;

public enum DiabeteNiveauRisqueEnum {
    NONE("None"),
    BORDERLINE("Borderline"),
    IN_DANGER("InDanger"),
    EARLY_ONSET("EarlyOnset");

    private final String description;

    DiabeteNiveauRisqueEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
