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
 * Contrôleur REST pour la gestion des ressources Note.
 * Fournit des points d'accès pour récupérer, créer et supprimer les notes des patients.
 *
 * Points d'accès:
 * - GET /api/note/{patId}: Récupère toutes les notes d'un patient spécifique
 * - POST /api/note: Crée une nouvelle note
 * - DELETE /api/note/{noteId}: Supprime une note spécifique par son ID
 *
 * @see NoteService Pour l'implémentation de la logique métier
 * @see NoteDto Objet de transfert de données pour les entités Note
 */
@RestController
@RequestMapping("/api/note")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    /**
     * Récupère toutes les notes pour un patient spécifique.
     *
     * @param patId l'identifiant du patient
     * @return un ResponseEntity contenant une liste d'objets NoteDto associés au
     *         patient spécifié
     */
    @GetMapping("/{patId}")
    public ResponseEntity<List<NoteDto>> getNoteByPatientId(@PathVariable int patId) {
        List<NoteDto> notes = noteService.getNotesByPatientId(patId);
        return ResponseEntity.ok(notes);
    }

    /**
     * Crée une nouvelle note dans le système.
     *
     * @param noteDto L'objet de transfert de données note contenant les informations à
     *                créer.
     *                Doit être valide selon les contraintes de validation.
     * @return ResponseEntity contenant le NoteDto créé avec le statut HTTP 201
     *         (CREATED)
     */
    @PostMapping
    public ResponseEntity<NoteDto> createNote(@Valid @RequestBody NoteDto noteDto) {
        NoteDto createdNote = noteService.create(noteDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNote);
    }

}