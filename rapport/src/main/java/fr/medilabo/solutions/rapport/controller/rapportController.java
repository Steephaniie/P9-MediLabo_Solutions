package fr.medilabo.solutions.rapport.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.medilabo.solutions.rapport.dto.DiabeteNiveauRisqueEnum;
import fr.medilabo.solutions.rapport.service.rapportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rapport")
public class rapportController {

    private final rapportService rapportService;

    /**
     * Retrieves the diabetes risk rapport for a specific patient.
     * 
     * @param patId the unique identifier of the patient for whom to assess diabetes
     *              risk
     * @return ResponseEntity containing the DiabetesRiskLevelEnum representing the
     *         patient's diabetes risk level
     * @throws IllegalArgumentException if the patient ID is invalid or patient not
     *                                  found
     */
    @GetMapping("/{patId}")
    public ResponseEntity<DiabeteNiveauRisqueEnum> getrapport(@PathVariable Long patId) {
        log.info("Requesting diabetes risk rapport for patient ID: {}", patId);
        DiabeteNiveauRisqueEnum niveauRisque = rapportService.assessDiabetesRisk(patId.intValue());
        log.info("Risk level for patient {}: {}", patId, niveauRisque);
        return ResponseEntity.ok(niveauRisque);

    }
}