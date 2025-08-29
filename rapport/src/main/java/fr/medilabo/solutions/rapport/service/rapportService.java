package fr.medilabo.solutions.rapport.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import fr.medilabo.solutions.rapport.dto.DiabeteTermeDeclencheurEnum;
import org.springframework.stereotype.Service;

import fr.medilabo.solutions.rapport.client.NoteServiceClient;
import fr.medilabo.solutions.rapport.client.PatientServiceClient;
import fr.medilabo.solutions.rapport.dto.DiabeteNiveauRisqueEnum;
import fr.medilabo.solutions.rapport.dto.NoteDto;
import fr.medilabo.solutions.rapport.dto.PatientDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class rapportService {

    private final PatientServiceClient patientServiceClient;
    private final NoteServiceClient noteServiceClient;

    /**
     * Évalue le niveau de risque de diabète pour un patient donné en fonction de son âge,
     * son genre et ses notes médicales.
     *
     * Cette méthode récupère les informations du patient et les notes médicales, puis calcule
     * le risque de diabète en analysant les termes déclencheurs dans les notes et en appliquant
     * les règles de rapport de risque basées sur l'âge et le genre.
     *
     * @param patId l'identifiant unique du patient à évaluer
     * @return le niveau de risque de diabète calculé sous forme de DiabeteNiveauRisqueEnum
     * @throws RuntimeException si les données du patient ne peuvent pas être récupérées ou si
     *                          l'identifiant du patient est invalide
     * 
     * @see DiabeteNiveauRisqueEnum
     * @see PatientDto
     * @see NoteDto
     */
    public DiabeteNiveauRisqueEnum assessDiabetesRisk(int patId) {
        log.info("Creating rapport for patient ID: {}", patId);

        PatientDto patientDto = patientServiceClient.getPatientById(patId);
        List<NoteDto> notes = noteServiceClient.getNoteByPatientId(patId);

        int age = calculateAge(patientDto.getBirthDate());
        boolean isFemme = "F".equals(patientDto.getGender());
        List<String> noteTexts = notes.stream().map(NoteDto::getNote).toList();
        int termesDeclencheurs = calculeTermesDeclencheurs(noteTexts);

        DiabeteNiveauRisqueEnum riskLevel = determineDiabeteNiveauRisque(age, isFemme, termesDeclencheurs);

        log.info("rapport completed - Patient ID: {}, Age: {}, Gender: {}, Triggers: {}, Risk: {}",
                patId, age, patientDto.getGender(), termesDeclencheurs, riskLevel);

        return riskLevel;
    }

    private int calculateAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * Compte le nombre de termes déclencheurs distincts trouvés dans la liste
     * des notes fournie.
     *
     * Cette méthode effectue une recherche insensible à la casse dans toutes les notes
     * pour identifier les termes déclencheurs présents. Chaque terme déclencheur n'est
     * compté qu'une seule fois, peu importe le nombre de fois qu'il apparaît dans les notes.
     *
     * @param notes la liste des notes à analyser pour les termes déclencheurs
     * @return le nombre de termes déclencheurs distincts trouvés dans les notes
     */
    private int calculeTermesDeclencheurs(List<String> notes) {
        return (int) notes.stream()
                .flatMap(note -> DiabeteTermeDeclencheurEnum.getAllTerms()
                        .stream()
                        .filter(term -> note.toLowerCase().contains(term.toLowerCase())))
                .distinct()
                .count();
    }

    /**
     * Calcule le niveau de risque de diabète en fonction des données démographiques
     * du patient et du nombre de termes déclencheurs.
     *
     * Le rapport de risque suit différents critères selon l'âge et le genre :
     * - Pour les patients de plus de 30 ans : Le risque est déterminé uniquement par le nombre de déclencheurs
     * - Pour les patients de 30 ans ou moins : Le risque est déterminé par le nombre de déclencheurs avec
     * des seuils différents pour les hommes et les femmes
     *
     * Les niveaux de risque sont déterminés comme suit :
     * - NONE : Aucun déclencheur présent ou nombre de déclencheurs insuffisant
     * - BORDERLINE : S'applique uniquement aux patients de plus de 30 ans avec 2-5 déclencheurs
     * - IN_DANGER :
     * - Patients de plus de 30 ans : 6-7 déclencheurs
     * - Hommes de 30 ans ou moins : 3-4 déclencheurs
     * - Femmes de 30 ans ou moins : 4-6 déclencheurs
     * - EARLY_ONSET :
     * - Patients de plus de 30 ans : 8 déclencheurs ou plus
     * - Hommes de 30 ans ou moins : 5 déclencheurs ou plus
     * - Femmes de 30 ans ou moins : 7 déclencheurs ou plus
     *
     * @param age          l'âge du patient en années
     * @param isFemme      vrai si le patient est une femme
     * @param termesDeclencheurs le nombre de facteurs de risque de diabète identifiés
     * @return le niveau de risque de diabète calculé sous forme de DiabeteNiveauRisqueEnum
     */
    private DiabeteNiveauRisqueEnum determineDiabeteNiveauRisque(int age, boolean isFemme, int termesDeclencheurs) {
        if (termesDeclencheurs == 0) {
            return DiabeteNiveauRisqueEnum.NONE;
        }

        if (age >= 30) {
            // Patient de plus de 30 ans
            // Entre 2 et 5 termes déclencheurs -> Borderline
            if (termesDeclencheurs >= 2 && termesDeclencheurs <= 5) {
                return DiabeteNiveauRisqueEnum.BORDERLINE;
                // Entre 6 et 7 termes déclencheurs -> En danger
            } else if (termesDeclencheurs >= 6 && termesDeclencheurs <= 7) {
                return DiabeteNiveauRisqueEnum.IN_DANGER;
                // Plus de 7 termes déclencheurs -> Apparition précoce
            } else if (termesDeclencheurs > 7) {
                return DiabeteNiveauRisqueEnum.EARLY_ONSET;
            }
        } else {
            if (isFemme) {
                // Femme de moins de 30 ans
                if (termesDeclencheurs >= 4 && termesDeclencheurs < 7) {
                    return DiabeteNiveauRisqueEnum.IN_DANGER;
                } else if (termesDeclencheurs >= 7) {
                    return DiabeteNiveauRisqueEnum.EARLY_ONSET;
                }
            } else {
                // Homme de moins de 30 ans
                if (termesDeclencheurs >= 3 && termesDeclencheurs <= 4) {
                    return DiabeteNiveauRisqueEnum.IN_DANGER;
                } else if (termesDeclencheurs >= 5) {
                    return DiabeteNiveauRisqueEnum.EARLY_ONSET;
                }
            }
        }
        return DiabeteNiveauRisqueEnum.NONE;
    }
}