package fr.medilabo.solutions.note.controller;

import fr.medilabo.solutions.note.dto.NoteDto;
import fr.medilabo.solutions.note.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing Note resources.
 * Provides endpoints for retrieving, creating, and deleting notes for patients.
 * 
 * Endpoints:
 * - GET /api/note/{patId}: Retrieves all notes for a specific patient
 * - POST /api/note: Creates a new note
 * - DELETE /api/note/{noteId}: Deletes a specific note by its ID
 * 
 * @see NoteService For the business logic implementation
 * @see NoteDto Data transfer object for Note entities
 */
@RestController
@RequestMapping("/api/note")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    /**
     * Retrieves all notes for a specific patient.
     *
     * @param patId the patient identifier
     * @return a ResponseEntity containing a list of NoteDto objects associated with
     *         the specified patient
     */
    @GetMapping("/{patId}")
    public ResponseEntity<List<NoteDto>> getNoteByPatientId(@PathVariable int patId) {
        List<NoteDto> notes = noteService.getNotesByPatientId(patId);
        return ResponseEntity.ok(notes);
    }

    /**
     * Creates a new note in the system.
     *
     * @param noteDto The note data transfer object containing the e information to
     *                be created.
     *                Must be valid according to the validation constraints.
     * @return ResponseEntity containing the created NoteDto with HTTP status 201
     *         (CREATED)
     */
    @PostMapping
    public ResponseEntity<NoteDto> createNote(@Valid @RequestBody NoteDto noteDto) {
        NoteDto createdNote = noteService.create(noteDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNote);
    }

}