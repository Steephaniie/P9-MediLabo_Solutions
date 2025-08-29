package fr.medilabo.solutions.note.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.medilabo.solutions.note.dto.NoteDto;
import fr.medilabo.solutions.note.exception.ResourceNotFoundException;
import fr.medilabo.solutions.note.mapper.NoteMapper;
import fr.medilabo.solutions.note.model.Note;
import fr.medilabo.solutions.note.repository.NoteRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Note Service Tests")
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private NoteMapper noteMapper;

    @InjectMocks
    private NoteService noteService;

    private Note note1;
    private Note note2;
    private NoteDto noteDto1;
    private NoteDto noteDto2;
    private List<Note> noteList;

    @BeforeEach
    void setUp() {
        // Setup Note entities
        note1 = new Note();
        note1.setId("64a7b8c9d1e2f3g4h5i6j7k8");
        note1.setPatId(1);
        note1.setPatient("TestNone");
        note1.setNote("Le patient déclare qu'il 'se sent très bien' Poids égal ou inférieur au poids recommandé");

        note2 = new Note();
        note2.setId("64a7b8c9d1e2f3g4h5i6j7k9");
        note2.setPatId(1);
        note2.setPatient("TestNone");
        note2.setNote("Le patient déclare qu'il ressent beaucoup de stress au travail");

        // Setup Note DTOs
        noteDto1 = new NoteDto();
        noteDto1.setId("64a7b8c9d1e2f3g4h5i6j7k8");
        noteDto1.setPatId(1);
        noteDto1.setPatient("TestNone");
        noteDto1.setNote("Le patient déclare qu'il 'se sent très bien' Poids égal ou inférieur au poids recommandé");

        noteDto2 = new NoteDto();
        noteDto2.setId("64a7b8c9d1e2f3g4h5i6j7k9");
        noteDto2.setPatId(1);
        noteDto2.setPatient("TestNone");
        noteDto2.setNote("Le patient déclare qu'il ressent beaucoup de stress au travail");

        noteList = Arrays.asList(note1, note2);
    }

    @Test
    @DisplayName("Should get notes by patient ID successfully")
    void getNotesByPatientId_WithValidPatientId_ShouldReturnNotes() {
        // Given
        int patientId = 1;
        when(noteRepository.findByPatId(patientId)).thenReturn(noteList);
        when(noteMapper.toDto(note1)).thenReturn(noteDto1);
        when(noteMapper.toDto(note2)).thenReturn(noteDto2);

        // When
        List<NoteDto> result = noteService.getNotesByPatientId(patientId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(noteDto1.getId(), result.get(0).getId());
        assertEquals(noteDto1.getNote(), result.get(0).getNote());
        assertEquals(noteDto2.getId(), result.get(1).getId());
        assertEquals(noteDto2.getNote(), result.get(1).getNote());

        verify(noteRepository, times(1)).findByPatId(patientId);
        verify(noteMapper, times(1)).toDto(note1);
        verify(noteMapper, times(1)).toDto(note2);
    }

    @Test
    @DisplayName("Should return empty list when patient has no notes")
    void getNotesByPatientId_WithPatientHavingNoNotes_ShouldReturnEmptyList() {
        // Given
        int patientId = 999;
        when(noteRepository.findByPatId(patientId)).thenReturn(Collections.emptyList());

        // When
        List<NoteDto> result = noteService.getNotesByPatientId(patientId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(noteRepository, times(1)).findByPatId(patientId);
        verify(noteMapper, times(0)).toDto(any(Note.class));
    }

    @Test
    @DisplayName("Should handle single note for patient")
    void getNotesByPatientId_WithSingleNote_ShouldReturnSingleNote() {
        // Given
        int patientId = 2;
        List<Note> singleNoteList = Arrays.asList(note1);
        when(noteRepository.findByPatId(patientId)).thenReturn(singleNoteList);
        when(noteMapper.toDto(note1)).thenReturn(noteDto1);

        // When
        List<NoteDto> result = noteService.getNotesByPatientId(patientId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(noteDto1.getId(), result.get(0).getId());

        verify(noteRepository, times(1)).findByPatId(patientId);
        verify(noteMapper, times(1)).toDto(note1);
    }

    @Test
    @DisplayName("Should handle different patient IDs correctly")
    void getNotesByPatientId_WithDifferentPatientIds_ShouldCallRepositoryWithCorrectId() {
        // Test avec plusieurs IDs différents
        int[] patientIds = { 1, 5, 100, 999 };

        for (int patientId : patientIds) {
            // Given
            when(noteRepository.findByPatId(patientId)).thenReturn(Collections.emptyList());

            // When
            List<NoteDto> result = noteService.getNotesByPatientId(patientId);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(noteRepository, times(1)).findByPatId(patientId);
        }
    }

    @Test
    @DisplayName("Should handle zero and negative patient IDs")
    void getNotesByPatientId_WithZeroAndNegativeIds_ShouldReturnEmptyList() {
        // Test avec ID = 0
        when(noteRepository.findByPatId(0)).thenReturn(Collections.emptyList());
        List<NoteDto> result = noteService.getNotesByPatientId(0);
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Test avec ID négatif
        when(noteRepository.findByPatId(-1)).thenReturn(Collections.emptyList());
        result = noteService.getNotesByPatientId(-1);
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(noteRepository, times(1)).findByPatId(0);
        verify(noteRepository, times(1)).findByPatId(-1);
    }

    @Test
    @DisplayName("Should create note successfully")
    void create_WithValidNoteDto_ShouldReturnCreatedNote() {
        // Given
        NoteDto inputDto = new NoteDto();
        inputDto.setPatId(3);
        inputDto.setPatient("TestInDanger");
        inputDto.setNote("Le patient déclare qu'il fume depuis peu");

        Note inputNote = new Note();
        inputNote.setPatId(3);
        inputNote.setPatient("TestInDanger");
        inputNote.setNote("Le patient déclare qu'il fume depuis peu");

        Note savedNote = new Note();
        savedNote.setId("64a7b8c9d1e2f3g4h5i6j7ka");
        savedNote.setPatId(3);
        savedNote.setPatient("TestInDanger");
        savedNote.setNote("Le patient déclare qu'il fume depuis peu");

        NoteDto resultDto = new NoteDto();
        resultDto.setId("64a7b8c9d1e2f3g4h5i6j7ka");
        resultDto.setPatId(3);
        resultDto.setPatient("TestInDanger");
        resultDto.setNote("Le patient déclare qu'il fume depuis peu");

        when(noteMapper.toEntity(inputDto)).thenReturn(inputNote);
        when(noteRepository.save(inputNote)).thenReturn(savedNote);
        when(noteMapper.toDto(savedNote)).thenReturn(resultDto);

        // When
        NoteDto result = noteService.create(inputDto);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(3, result.getPatId());
        assertEquals("TestInDanger", result.getPatient());
        assertEquals("Le patient déclare qu'il fume depuis peu", result.getNote());

        verify(noteMapper, times(1)).toEntity(inputDto);
        verify(noteRepository, times(1)).save(inputNote);
        verify(noteMapper, times(1)).toDto(savedNote);
    }

    @Test
    @DisplayName("Should create note with minimal data")
    void create_WithMinimalData_ShouldReturnCreatedNote() {
        // Given
        NoteDto minimalDto = new NoteDto();
        minimalDto.setNote("Note minimale");

        Note minimalNote = new Note();
        minimalNote.setNote("Note minimale");

        Note savedNote = new Note();
        savedNote.setId("64a7b8c9d1e2f3g4h5i6j7kb");
        savedNote.setNote("Note minimale");

        NoteDto resultDto = new NoteDto();
        resultDto.setId("64a7b8c9d1e2f3g4h5i6j7kb");
        resultDto.setNote("Note minimale");

        when(noteMapper.toEntity(minimalDto)).thenReturn(minimalNote);
        when(noteRepository.save(minimalNote)).thenReturn(savedNote);
        when(noteMapper.toDto(savedNote)).thenReturn(resultDto);

        // When
        NoteDto result = noteService.create(minimalDto);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Note minimale", result.getNote());

        verify(noteMapper, times(1)).toEntity(minimalDto);
        verify(noteRepository, times(1)).save(minimalNote);
        verify(noteMapper, times(1)).toDto(savedNote);
    }

    @Test
    @DisplayName("Should create note with special characters")
    void create_WithSpecialCharacters_ShouldReturnCreatedNote() {
        // Given
        String specialNote = "Note avec caractères spéciaux: éàùç, ponctuation!? Et des accents...";
        NoteDto specialDto = new NoteDto();
        specialDto.setPatId(1);
        specialDto.setPatient("TestPatient");
        specialDto.setNote(specialNote);

        Note specialNoteEntity = new Note();
        specialNoteEntity.setPatId(1);
        specialNoteEntity.setPatient("TestPatient");
        specialNoteEntity.setNote(specialNote);

        Note savedNote = new Note();
        savedNote.setId("64a7b8c9d1e2f3g4h5i6j7kc");
        savedNote.setPatId(1);
        savedNote.setPatient("TestPatient");
        savedNote.setNote(specialNote);

        NoteDto resultDto = new NoteDto();
        resultDto.setId("64a7b8c9d1e2f3g4h5i6j7kc");
        resultDto.setPatId(1);
        resultDto.setPatient("TestPatient");
        resultDto.setNote(specialNote);

        when(noteMapper.toEntity(specialDto)).thenReturn(specialNoteEntity);
        when(noteRepository.save(specialNoteEntity)).thenReturn(savedNote);
        when(noteMapper.toDto(savedNote)).thenReturn(resultDto);

        // When
        NoteDto result = noteService.create(specialDto);

        // Then
        assertNotNull(result);
        assertEquals(specialNote, result.getNote());

        verify(noteMapper, times(1)).toEntity(specialDto);
        verify(noteRepository, times(1)).save(specialNoteEntity);
        verify(noteMapper, times(1)).toDto(savedNote);
    }

    @Test
    @DisplayName("Should create note with long content")
    void create_WithLongContent_ShouldReturnCreatedNote() {
        // Given
        String longNote = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. ".repeat(20);
        NoteDto longDto = new NoteDto();
        longDto.setPatId(1);
        longDto.setPatient("TestPatient");
        longDto.setNote(longNote);

        Note longNoteEntity = new Note();
        longNoteEntity.setPatId(1);
        longNoteEntity.setPatient("TestPatient");
        longNoteEntity.setNote(longNote);

        Note savedNote = new Note();
        savedNote.setId("64a7b8c9d1e2f3g4h5i6j7kd");
        savedNote.setPatId(1);
        savedNote.setPatient("TestPatient");
        savedNote.setNote(longNote);

        NoteDto resultDto = new NoteDto();
        resultDto.setId("64a7b8c9d1e2f3g4h5i6j7kd");
        resultDto.setPatId(1);
        resultDto.setPatient("TestPatient");
        resultDto.setNote(longNote);

        when(noteMapper.toEntity(longDto)).thenReturn(longNoteEntity);
        when(noteRepository.save(longNoteEntity)).thenReturn(savedNote);
        when(noteMapper.toDto(savedNote)).thenReturn(resultDto);

        // When
        NoteDto result = noteService.create(longDto);

        // Then
        assertNotNull(result);
        assertEquals(longNote, result.getNote());
        assertTrue(result.getNote().length() > 1000); // Vérifier que c'est vraiment long

        verify(noteMapper, times(1)).toEntity(longDto);
        verify(noteRepository, times(1)).save(longNoteEntity);
        verify(noteMapper, times(1)).toDto(savedNote);
    }

    @Test
    @DisplayName("Should delete note successfully")
    void delete_WithValidNoteId_ShouldDeleteNote() {
        // Given
        String noteId = "64a7b8c9d1e2f3g4h5i6j7k8";

        // When
        noteService.delete(noteId);

        // Then
        verify(noteRepository, times(1)).deleteById(noteId);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent note")
    void delete_WithNonExistentNoteId_ShouldThrowException() {
        // Given
        String noteId = "nonexistent123";
        doThrow(new RuntimeException("Note not found")).when(noteRepository).deleteById(noteId);

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> noteService.delete(noteId));

        assertTrue(exception.getMessage().contains("not found for deletion"));
        verify(noteRepository, times(1)).deleteById(noteId);
    }

    @Test
    @DisplayName("Should handle repository exception during deletion")
    void delete_WithRepositoryException_ShouldThrowResourceNotFoundException() {
        // Given
        String noteId = "problematic-id";
        doThrow(new RuntimeException("Database connection error")).when(noteRepository).deleteById(noteId);

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> noteService.delete(noteId));

        assertTrue(exception.getMessage().contains(noteId));
        assertTrue(exception.getMessage().contains("not found for deletion"));
        verify(noteRepository, times(1)).deleteById(noteId);
    }

    @Test
    @DisplayName("Should handle multiple consecutive calls correctly")
    void getNotesByPatientId_WithMultipleCalls_ShouldWorkCorrectly() {
        // Given
        int patientId = 1;
        when(noteRepository.findByPatId(patientId)).thenReturn(noteList);
        when(noteMapper.toDto(note1)).thenReturn(noteDto1);
        when(noteMapper.toDto(note2)).thenReturn(noteDto2);

        // When - Appels multiples
        List<NoteDto> result1 = noteService.getNotesByPatientId(patientId);
        List<NoteDto> result2 = noteService.getNotesByPatientId(patientId);
        List<NoteDto> result3 = noteService.getNotesByPatientId(patientId);

        // Then
        assertEquals(2, result1.size());
        assertEquals(2, result2.size());
        assertEquals(2, result3.size());

        // Vérifier que les appels ont bien été faits
        verify(noteRepository, times(3)).findByPatId(patientId);
        verify(noteMapper, times(3)).toDto(note1);
        verify(noteMapper, times(3)).toDto(note2);
    }

    @Test
    @DisplayName("Should handle edge case with very large patient ID")
    void getNotesByPatientId_WithLargePatientId_ShouldReturnEmptyList() {
        // Given
        int largePatientId = Integer.MAX_VALUE;
        when(noteRepository.findByPatId(largePatientId)).thenReturn(Collections.emptyList());

        // When
        List<NoteDto> result = noteService.getNotesByPatientId(largePatientId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(noteRepository, times(1)).findByPatId(largePatientId);
    }

    @Test
    @DisplayName("Should maintain data integrity during creation")
    void create_ShouldMaintainDataIntegrity() {
        // Given
        NoteDto originalDto = new NoteDto();
        originalDto.setPatId(42);
        originalDto.setPatient("Test Patient Original");
        originalDto.setNote("Original note content");

        Note entityFromMapper = new Note();
        entityFromMapper.setPatId(42);
        entityFromMapper.setPatient("Test Patient Original");
        entityFromMapper.setNote("Original note content");

        Note savedEntity = new Note();
        savedEntity.setId("generated-id-123");
        savedEntity.setPatId(42);
        savedEntity.setPatient("Test Patient Original");
        savedEntity.setNote("Original note content");

        NoteDto resultDto = new NoteDto();
        resultDto.setId("generated-id-123");
        resultDto.setPatId(42);
        resultDto.setPatient("Test Patient Original");
        resultDto.setNote("Original note content");

        when(noteMapper.toEntity(originalDto)).thenReturn(entityFromMapper);
        when(noteRepository.save(entityFromMapper)).thenReturn(savedEntity);
        when(noteMapper.toDto(savedEntity)).thenReturn(resultDto);

        // When
        NoteDto result = noteService.create(originalDto);

        // Then - Vérifier l'intégrité des données
        assertEquals(originalDto.getPatId(), result.getPatId());
        assertEquals(originalDto.getPatient(), result.getPatient());
        assertEquals(originalDto.getNote(), result.getNote());
        assertNotNull(result.getId()); // L'ID doit être généré

        verify(noteMapper, times(1)).toEntity(originalDto);
        verify(noteRepository, times(1)).save(entityFromMapper);
        verify(noteMapper, times(1)).toDto(savedEntity);
    }
}