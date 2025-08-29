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
     * Récupère le rapport de risque de diabète pour un patient spécifique.
     *
     * @param patId l'identifiant unique du patient pour lequel évaluer le risque
     *              de diabète
     * @return ResponseEntity contenant le DiabetesRiskLevelEnum représentant le
     *         niveau de risque de diabète du patient
     * @throws IllegalArgumentException si l'ID du patient est invalide ou si le patient
     *                                  n'est pas trouvé
     */
    @GetMapping("/{patId}")
    public ResponseEntity<DiabeteNiveauRisqueEnum> getrapport(@PathVariable Long patId) {
        log.info("Requesting diabetes risk rapport for patient ID: {}", patId);
        DiabeteNiveauRisqueEnum niveauRisque = rapportService.assessDiabetesRisk(patId.intValue());
        log.info("Risk level for patient {}: {}", patId, niveauRisque);
        return ResponseEntity.ok(niveauRisque);

    }
}