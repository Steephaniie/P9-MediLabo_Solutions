package fr.medilabo.solutions.front.dto;

public enum DiabetesRiskLevelEnum {
    NONE("None"),
    BORDERLINE("Borderline"),
    IN_DANGER("InDanger"),
    EARLY_ONSET("EarlyOnset");

    private final String description;

    DiabetesRiskLevelEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
