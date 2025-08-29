package fr.medilabo.solutions.rapport.dto;

/**
 * Enumération des termes déclencheurs utilisés pour l'évaluation du risque de diabète.
 * 
 * Ces termes sont recherchés dans les notes médicales des patients pour déterminer
 * le niveau de risque de développer un diabète. Chaque terme représente un facteur
 * de risque ou un indicateur médical pertinent.
 */
public enum DiabeteTermeDeclencheurEnum {
    
    HEMOGLOBINE_A1C("hémoglobine a1c"),
    MICROALBUMINE("microalbumine"),
    TAILLE("taille"),
    POIDS("poids"),
    FUMEUR("fumeur"),
    FUMEUSE("fumeuse"),
    ANORMAL("anormal"),
    CHOLESTEROL("cholestérol"),
    VERTIGE("vertige"),
    RECHUTE("rechute"),
    REACTION("réaction"),
    ANTICORPS("anticorps");
    
    private final String term;
    
    /**
     * Constructeur de l'énumération.
     * 
     * @param term le terme médical déclencheur
     */
    DiabeteTermeDeclencheurEnum(String term) {
        this.term = term;
    }
    
    /**
     * Retourne la valeur textuelle du terme déclencheur.
     * 
     * @return le terme médical sous forme de chaîne de caractères
     */
    public String getTerm() {
        return term;
    }
    
    /**
     * Retourne tous les termes déclencheurs sous forme de liste de chaînes.
     * 
     * @return une liste contenant tous les termes déclencheurs
     */
    public static java.util.List<String> getAllTerms() {
        return java.util.Arrays.stream(values())
                .map(DiabeteTermeDeclencheurEnum::getTerm)
                .collect(java.util.stream.Collectors.toList());
    }
}
