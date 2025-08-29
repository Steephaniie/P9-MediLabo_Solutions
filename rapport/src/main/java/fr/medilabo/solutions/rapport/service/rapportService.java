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
     * Assesses the diabetes risk level for a given patient based on their age,
     * gender, and medical notes.
     * 
     * This method retrieves patient information and medical notes, then calculates
     * the diabetes risk
     * by analyzing trigger terms in the notes and applying risk rapport rules
     * based on age and gender.
     * 
     * @param patId the unique identifier of the patient to assess
     * @return the calculated diabetes risk level as a DiabetesRiskLevelEnum
     * @throws RuntimeException if patient data cannot be retrieved or if the
     *                          patient ID is invalid
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
     * Counts the number of distinct trigger terms found in the provided list of
     * notes.
     * 
     * This method performs a case-insensitive search through all notes to identify
     * trigger terms that are present. Each trigger term is counted only once,
     * regardless of how many times it appears across all notes.
     * 
     * @param notes the list of note strings to search through for trigger terms
     * @return the count of distinct trigger terms found in the notes
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
     * Calculates the diabetes risk level based on patient demographics and trigger
     * count.
     * 
     * The risk rapport follows different criteria based on age and gender:
     * - For patients over 30: Risk is determined solely by trigger count
     * - For patients 30 or under: Risk is determined by trigger count with
     * different thresholds for males and females
     * 
     * Risk levels are determined as follows:
     * - NONE: No triggers present, or trigger count doesn't meet minimum thresholds
     * - BORDERLINE: Only applies to patients over 30 with 2-5 triggers
     * - IN_DANGER:
     * - Patients over 30: 6-7 triggers
     * - Males 30 or under: 3-4 triggers
     * - Females 30 or under: 4-6 triggers
     * - EARLY_ONSET:
     * - Patients over 30: 8 or more triggers
     * - Males 30 or under: 5 or more triggers
     * - Females 30 or under: 7 or more triggers
     * 
     * @param age          the patient's age in years
     * @param isFemme      true if the patient is female
     * @param termesDeclencheurs the number of diabetes risk factor triggers identified
     * @return the calculated diabetes risk level as a DiabetesRiskLevelEnum
     */
    private DiabeteNiveauRisqueEnum determineDiabeteNiveauRisque(int age, boolean isFemme, int termesDeclencheurs) {
        if (termesDeclencheurs == 0) {
            return DiabeteNiveauRisqueEnum.NONE;
        }

        if (age >= 30) {
            // patient plus de 30 ans
            //entre 2 et 5 termes declencheurs -> borderline
            if (termesDeclencheurs >= 2 && termesDeclencheurs <= 5) {
                return DiabeteNiveauRisqueEnum.BORDERLINE;
                // entre 6 et 7 termes declencheurs -> in danger
            } else if (termesDeclencheurs >= 6 && termesDeclencheurs <= 7) {
                return DiabeteNiveauRisqueEnum.IN_DANGER;
                //plus de 7 termes declencheurs -> early onset
            } else if (termesDeclencheurs > 7) {
                return DiabeteNiveauRisqueEnum.EARLY_ONSET;
            }
        } else {
            if (isFemme) {
                // femme de moins de 30 ans
                if (termesDeclencheurs >= 4 && termesDeclencheurs < 7) {
                    return DiabeteNiveauRisqueEnum.IN_DANGER;
                } else if (termesDeclencheurs >= 7) {
                    return DiabeteNiveauRisqueEnum.EARLY_ONSET;
                }
            } else {
                // homme de moins de 30 ans
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