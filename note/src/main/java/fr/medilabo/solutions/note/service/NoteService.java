package fr.medilabo.solutions.note.service;

import fr.medilabo.solutions.note.dto.NoteDto;
import fr.medilabo.solutions.note.exception.ResourceNotFoundException;
import fr.medilabo.solutions.note.mapper.NoteMapper;
import fr.medilabo.solutions.note.model.Note;
import fr.medilabo.solutions.note.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe de service pour la gestion des notes médicales des patients.
 * Ce service fournit des fonctionnalités pour récupérer, créer et supprimer
 * les notes associées aux patients.
 * 
 * @author LEULLIETTE Stéphanie
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;
    private static final Logger log = LoggerFactory.getLogger(NoteService.class);

    /**
     * Récupère toutes les notes associées à un patient spécifique.
     *
     * @param patientId L'identifiant unique du patient dont les notes doivent être
     *                  récupérées
     * @return Une liste d'objets NoteDto représentant les notes du patient
     */
    public List<NoteDto> getNotesByPatientId(int patientId) {
        log.debug("Récupération des notes pour le patient avec l'ID : " + patientId);
        List<Note> notes = noteRepository.findByPatId((Integer) patientId);
        List<NoteDto> noteDtos = new ArrayList<>();
        for (Note note : notes) {
            NoteDto noteDto = noteMapper.toDto(note);
            noteDtos.add(noteDto);
        }
        return noteDtos;
    }

    /**
     * Crée une nouvelle entrée Note en utilisant le DTO fourni.
     * <p>
     * Cette méthode convertit le NoteDto en entité Note, l'enregistre dans la base de données,
     * et retourne la note nouvellement créée sous forme de DTO.
     *
     * @param noteDto l'objet de transfert de données contenant les informations de la note à
     *                sauvegarder
     * @return l'objet de transfert de données représentant la note nouvellement créée avec
     *         l'ID généré
     */
    public NoteDto create(NoteDto noteDto) {
        Note note = noteMapper.toEntity(noteDto);
        Note savedNote = noteRepository.save(note);
        return noteMapper.toDto(savedNote);
    }

    /**
     * Supprime une note par son ID.
     *
     * @param noteId l'ID de la note à supprimer
     */
    public void delete(String noteId) {
        try {
            log.debug("Tentative de suppression de la note avec l'ID : " + noteId);
            noteRepository.deleteById(noteId);
        } catch (Exception e) {
            log.error(noteId + " non trouvée pour la suppression.");
            throw new ResourceNotFoundException("Note with ID " + noteId + " not found for deletion.");
        }

    }
}