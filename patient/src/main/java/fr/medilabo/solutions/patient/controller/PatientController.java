package fr.medilabo.solutions.patient.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.medilabo.solutions.patient.dto.PatientDto;
import fr.medilabo.solutions.patient.exception.ResourceNotFoundException;
import fr.medilabo.solutions.patient.service.PatientService;

import jakarta.validation.Valid;

/**
 * Contrôleur REST pour gérer les opérations CRUD sur les patients.
 * Expose les endpoints pour la gestion des patients via l'API.
 */
@RestController
@RequestMapping("/api/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;

    /**
     * Récupère la liste complète des patients.
     *
     * @return ResponseEntity contenant la liste des patients
     */
    @GetMapping
    public ResponseEntity<List<PatientDto>> getAllPatients() {
        List<PatientDto> patients = patientService.findAll();
        return ResponseEntity.ok(patients);
    }


    /**
     * Récupère un patient par son identifiant.
     *
     * @param id identifiant du patient
     * @return ResponseEntity contenant les informations du patient
     * @throws ResourceNotFoundException si le patient n'est pas trouvé
     */
    @GetMapping("/{id}")
    public ResponseEntity<PatientDto> getPatient(@PathVariable int id) {
        PatientDto patient = patientService.findById(id);
        return ResponseEntity.ok(patient);
    }


    /**
     * Crée un nouveau patient.
     *
     * @param patientDto données du patient à créer
     * @return ResponseEntity contenant le patient créé
     */
    @PostMapping
    public ResponseEntity<PatientDto> createPatient(@Valid @RequestBody PatientDto patientDto) {
        PatientDto createdPatient = patientService.create(patientDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPatient);
    }


    /**
     * Met à jour les informations d'un patient existant.
     *
     * @param id         identifiant du patient à modifier
     * @param patientDto nouvelles données du patient
     * @return ResponseEntity contenant le patient mis à jour
     * @throws ResourceNotFoundException si le patient n'est pas trouvé
     */
    @PutMapping("/{id}")
    public ResponseEntity<PatientDto> updatePatient(@PathVariable int id, @Valid @RequestBody PatientDto patientDto) {
        patientService.findById(id);

        patientDto.setId(id);

        PatientDto updatedPatient = patientService.update(patientDto);
        return ResponseEntity.ok(updatedPatient);
    }


    /**
     * Supprime un patient par son identifiant.
     *
     * @param id identifiant du patient à supprimer
     * @return ResponseEntity sans contenu
     * @throws ResourceNotFoundException si le patient n'est pas trouvé
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable int id) {
        PatientDto patientToDelete = patientService.findById(id);
        patientService.delete(patientToDelete);
        return ResponseEntity.noContent().build();
    }
}