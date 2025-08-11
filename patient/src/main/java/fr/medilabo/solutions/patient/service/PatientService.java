package fr.medilabo.solutions.patient.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fr.medilabo.solutions.patient.dto.PatientDto;
import fr.medilabo.solutions.patient.exception.ResourceNotFoundException;
import fr.medilabo.solutions.patient.mapper.PatientMapper;
import fr.medilabo.solutions.patient.model.Patient;
import fr.medilabo.solutions.patient.repository.PatientRepository;


/**
 * Service pour la gestion des opérations de données patient.
 *
 * Cette classe implémente l'interface {@link CrudService} pour gérer les opérations
 * liées à {@link PatientDto}. Elle fournit la logique métier pour créer, récupérer,
 * mettre à jour et supprimer les dossiers patients. La classe s'intègre avec la couche
 * repository pour interagir avec la source de données en utilisant {@link PatientRepository}
 * et fait le mapping entre les couches entité et DTO en utilisant {@link PatientMapper}.
 *
 * Les méthodes prennent en charge les opérations telles que la création d'un nouveau patient,
 * la recherche de tous les patients, la récupération d'un patient par ID, la mise à jour
 * d'un patient et la suppression d'un patient.
 */
@Service
public class PatientService implements CrudService<PatientDto> {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    public PatientService(PatientRepository patientRepository, PatientMapper patientMapper) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
    }

    /**
     * Crée un nouveau patient.
     *
     * Cette méthode convertit le PatientDto fourni en entité Patient,
     * l'enregistre dans le repository, puis retourne le patient sauvegardé
     * reconverti en DTO.
     *
     * @param patientDto L'objet de transfert de données patient contenant les informations
     *                   pour créer un nouveau patient
     * @return Le PatientDto créé avec les informations mises à jour (comme l'ID attribué)
     */
    @Override
    public PatientDto create(PatientDto patientDto) {
        Patient patient = patientMapper.toEntity(patientDto);
        return patientMapper.toDto(patientRepository.save(patient));
    }

    /**
     * Récupère tous les patients de la base de données.
     *
     * Cette méthode récupère tous les enregistrements patients, les convertit en DTOs
     * en utilisant le patient mapper, et les retourne sous forme de liste.
     *
     * @return Une liste d'objets PatientDto représentant tous les patients dans la
     *         base de données
     */
    @Override
    public List<PatientDto> findAll() {
        return patientRepository.findAll().stream()
                .map(patientMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Récupère un patient par son ID.
     *
     * @param id l'identifiant unique du patient à récupérer
     * @return l'objet PatientDto contenant les informations du patient
     * @throws ResourceNotFoundException si aucun patient n'est trouvé avec l'ID donné
     */
    @Override
    public PatientDto findById(int id) {
        return patientMapper.toDto(patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id)));
    }

    /**
     * Met à jour une entité Patient existante avec les informations fournies dans
     * PatientDto.
     *
     * @param patientDto L'objet de transfert de données contenant les informations
     *                   mises à jour du patient
     * @return L'objet de transfert de données représentant le patient mis à jour
     * @throws IllegalArgumentException si le patient n'est pas trouvé
     * @throws DataAccessException      si une erreur survient pendant l'opération
     *                                  en base de données
     */
    @Override
    public PatientDto update(PatientDto patientDto) {
        Patient patient = patientMapper.toEntity(patientDto);
        return patientMapper.toDto(patientRepository.save(patient));
    }

    /**
     * Supprime un patient de la base de données.
     *
     * @param patientDto l'objet de transfert de données du patient à supprimer
     */
    @Override
    public void delete(PatientDto patientDto) {
        Patient patient = patientMapper.toEntity(patientDto);
        patientRepository.delete(patient);
    }

}
