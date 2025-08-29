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
 * Service class for managing patient medical notes.
 * This service provides functionality for retrieving, creating, and deleting
 * notes associated with patients.
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
     * Retrieves all notes associated with a specific patient.
     * 
     * @param patientId The unique identifier of the patient whose notes are to be
     *                  retrieved
     * @return A list of NoteDto objects representing the patient's notes
     */
    public List<NoteDto> getNotesByPatientId(int patientId) {
        log.debug("Retrieving notes for patient with ID: " + patientId);
        List<Note> notes = noteRepository.findByPatId((Integer) patientId);
        List<NoteDto> noteDtos = new ArrayList<>();
        for (Note note : notes) {
            NoteDto noteDto = noteMapper.toDto(note);
            noteDtos.add(noteDto);
        }
        return noteDtos;
    }

    /**
     * Creates a new Note entry using the provided DTO.
     * <p>
     * This method converts the NoteDto to a Note entity, saves it to the database,
     * and returns the newly created note as a DTO.
     * 
     * @param noteDto the Data Transfer Object containing the note information to be
     *                saved
     * @return the Data Transfer Object representing the newly created note with
     *         generated ID
     */
    public NoteDto create(NoteDto noteDto) {
        Note note = noteMapper.toEntity(noteDto);
        Note savedNote = noteRepository.save(note);
        return noteMapper.toDto(savedNote);
    }

    /**
     * Deletes a note by its ID.
     *
     * @param noteId the ID of the note to be deleted
     */
    public void delete(String noteId) {
        try {
            log.debug("Attempting to delete note with ID: " + noteId);
            noteRepository.deleteById(noteId);
        } catch (Exception e) {
            log.error(noteId + " not found for deletion.");
            throw new ResourceNotFoundException("Note with ID " + noteId + " not found for deletion.");
        }

    }
}