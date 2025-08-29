package fr.medilabo.solutions.note.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.medilabo.solutions.note.dto.NoteDto;
import fr.medilabo.solutions.note.security.JwtUtil;
import fr.medilabo.solutions.note.service.NoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests unitaires pour le contrôleur de notes.
 * Cette classe teste les différentes fonctionnalités du contrôleur REST
 * permettant la gestion des notes des patients.
 */
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WebMvcTest(NoteController.class)
@DisplayName("Note Controller Tests")
class NoteControllerTest {

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoteService noteService;

    @Autowired
    private ObjectMapper objectMapper;

    private NoteDto noteDto1;
    private NoteDto noteDto2;
    private List<NoteDto> noteList;

    /**
     * Configuration initiale avant chaque test.
     * Prépare les objets DTO de notes utilisés dans les tests.
     */
    @BeforeEach
    void setUp() {
        // Configuration de la première note
        noteDto1 = new NoteDto();
        noteDto1.setId("64a7b8c9d1e2f3g4h5i6j7k8");
        noteDto1.setPatId(1);
        noteDto1.setPatient("TestNone");
        noteDto1.setNote(
                "Le patient déclare qu'il 'se sent très bien' Poids égal ou inférieur au poids recommandé");

        // Configuration de la deuxième note
        noteDto2 = new NoteDto();
        noteDto2.setId("64a7b8c9d1e2f3g4h5i6j7k9");
        noteDto2.setPatId(1);
        noteDto2.setPatient("TestNone");
        noteDto2.setNote("Le patient déclare qu'il ressent beaucoup de stress au travail");

        noteList = Arrays.asList(noteDto1, noteDto2);
    }

    /**
     * Teste la récupération de toutes les notes d'un patient.
     * Vérifie que les notes sont correctement retournées pour un ID patient valide.
     */
    @Test
    @DisplayName("Devrait retourner toutes les notes d'un patient")
    void getNoteByPatientId_WithValidPatientId_ShouldReturnNotes() throws Exception {
        // Étant donné
        int patientId = 1;
        when(noteService.getNotesByPatientId(patientId)).thenReturn(noteList);

        // Quand & Alors
        mockMvc.perform(get("/api/note/{patId}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(noteDto1.getId()))
                .andExpect(jsonPath("$[0].patId").value(1))
                .andExpect(jsonPath("$[0].patient").value("TestNone"))
                .andExpect(jsonPath("$[0].note").value(
                        "Le patient déclare qu'il 'se sent très bien' Poids égal ou inférieur au poids recommandé"))
                .andExpect(jsonPath("$[1].id").value(noteDto2.getId()))
                .andExpect(jsonPath("$[1].note").value(
                        "Le patient déclare qu'il ressent beaucoup de stress au travail"));

        verify(noteService, times(1)).getNotesByPatientId(patientId);
    }

    @Test
    @DisplayName("Should return empty list when patient has no notes")
    void getNoteByPatientId_WithPatientHavingNoNotes_ShouldReturnEmptyList() throws Exception {
        // Given
        int patientId = 999;
        when(noteService.getNotesByPatientId(patientId)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/note/{patId}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(noteService, times(1)).getNotesByPatientId(patientId);
    }

    @Test
    @DisplayName("Should handle different patient IDs correctly")
    void getNoteByPatientId_WithDifferentPatientIds_ShouldReturnCorrectNotes() throws Exception {
        // Given
        int patientId = 2;
        NoteDto note = new NoteDto();
        note.setId("64a7b8c9d1e2f3g4h5i6j7ka");
        note.setPatId(2);
        note.setPatient("TestBorderline");
        note.setNote("Note pour le patient 2");

        when(noteService.getNotesByPatientId(patientId)).thenReturn(Arrays.asList(note));

        // When & Then
        mockMvc.perform(get("/api/note/{patId}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patId").value(2))
                .andExpect(jsonPath("$[0].patient").value("TestBorderline"));

        verify(noteService, times(1)).getNotesByPatientId(patientId);
    }

    @Test
    @DisplayName("Should create note successfully")
    void createNote_WithValidData_ShouldCreateNote() throws Exception {
        // Given
        NoteDto newNote = new NoteDto();
        newNote.setPatId(3);
        newNote.setPatient("TestInDanger");
        newNote.setNote("Le patient déclare qu'il fume depuis peu");

        NoteDto createdNote = new NoteDto();
        createdNote.setId("64a7b8c9d1e2f3g4h5i6j7kb");
        createdNote.setPatId(3);
        createdNote.setPatient("TestInDanger");
        createdNote.setNote("Le patient déclare qu'il fume depuis peu");

        when(noteService.create(any(NoteDto.class))).thenReturn(createdNote);

        // When & Then
        mockMvc.perform(post("/api/note")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newNote)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(createdNote.getId()))
                .andExpect(jsonPath("$.patId").value(3))
                .andExpect(jsonPath("$.patient").value("TestInDanger"))
                .andExpect(jsonPath("$.note").value("Le patient déclare qu'il fume depuis peu"));

        verify(noteService, times(1)).create(any(NoteDto.class));
    }

    @Test
    @DisplayName("Should return 400 for note with blank note field")
    void createNote_WithBlankNote_ShouldReturn400() throws Exception {
        // Given
        NoteDto invalidNote = new NoteDto();
        invalidNote.setPatId(1);
        invalidNote.setPatient("TestPatient");
        invalidNote.setNote(""); // Blank note - invalid

        // When & Then
        mockMvc.perform(post("/api/note")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidNote)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message").value("Note cannot be blank"));

        verify(noteService, times(0)).create(any(NoteDto.class));
    }

    @Test
    @DisplayName("Should return 400 for note with null note field")
    void createNote_WithNullNote_ShouldReturn400() throws Exception {
        // Given
        NoteDto invalidNote = new NoteDto();
        invalidNote.setPatId(1);
        invalidNote.setPatient("TestPatient");
        invalidNote.setNote(null); // Null note - invalid

        // When & Then
        mockMvc.perform(post("/api/note")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidNote)))
                .andExpect(status().isBadRequest());

        verify(noteService, times(0)).create(any(NoteDto.class));
    }

    @Test
    @DisplayName("Should create note with only required fields")
    void createNote_WithOnlyRequiredFields_ShouldCreateNote() throws Exception {
        // Given
        NoteDto minimalNote = new NoteDto();
        minimalNote.setNote("Note minimale valide");

        NoteDto createdNote = new NoteDto();
        createdNote.setId("64a7b8c9d1e2f3g4h5i6j7kc");
        createdNote.setNote("Note minimale valide");

        when(noteService.create(any(NoteDto.class))).thenReturn(createdNote);

        // When & Then
        mockMvc.perform(post("/api/note")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(minimalNote)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.note").value("Note minimale valide"));

        verify(noteService, times(1)).create(any(NoteDto.class));
    }

    @Test
    @DisplayName("Should handle special characters in note content")
    void createNote_WithSpecialCharacters_ShouldCreateNote() throws Exception {
        // Given
        NoteDto noteWithSpecialChars = new NoteDto();
        noteWithSpecialChars.setPatId(1);
        noteWithSpecialChars.setPatient("TestPatient");
        noteWithSpecialChars
                .setNote("Note avec des caractères spéciaux: éàùç, ponctuation!? Et des accents...");

        NoteDto createdNote = new NoteDto();
        createdNote.setId("64a7b8c9d1e2f3g4h5i6j7kd");
        createdNote.setPatId(1);
        createdNote.setPatient("TestPatient");
        createdNote.setNote("Note avec des caractères spéciaux: éàùç, ponctuation!? Et des accents...");

        when(noteService.create(any(NoteDto.class))).thenReturn(createdNote);

        // When & Then
        mockMvc.perform(post("/api/note")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteWithSpecialChars)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.note").value(
                        "Note avec des caractères spéciaux: éàùç, ponctuation!? Et des accents..."));

        verify(noteService, times(1)).create(any(NoteDto.class));
    }

    @Test
    @DisplayName("Should handle long note content")
    void createNote_WithLongNote_ShouldCreateNote() throws Exception {
        // Given
        String longNote = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. ".repeat(10);
        NoteDto noteWithLongContent = new NoteDto();
        noteWithLongContent.setPatId(1);
        noteWithLongContent.setPatient("TestPatient");
        noteWithLongContent.setNote(longNote);

        NoteDto createdNote = new NoteDto();
        createdNote.setId("64a7b8c9d1e2f3g4h5i6j7ke");
        createdNote.setPatId(1);
        createdNote.setPatient("TestPatient");
        createdNote.setNote(longNote);

        when(noteService.create(any(NoteDto.class))).thenReturn(createdNote);

        // When & Then
        mockMvc.perform(post("/api/note")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteWithLongContent)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.note").value(longNote));

        verify(noteService, times(1)).create(any(NoteDto.class));
    }

    @Test
    @DisplayName("Should handle zero and negative patient IDs")
    void getNoteByPatientId_WithZeroAndNegativeIds_ShouldReturnNotes() throws Exception {
        // Test avec ID = 0
        when(noteService.getNotesByPatientId(0)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/note/{patId}", 0))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        // Test avec ID négatif
        when(noteService.getNotesByPatientId(-1)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/note/{patId}", -1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(noteService, times(1)).getNotesByPatientId(0);
        verify(noteService, times(1)).getNotesByPatientId(-1);
    }

    @Test
    @DisplayName("Should handle large patient IDs")
    void getNoteByPatientId_WithLargePatientId_ShouldReturnNotes() throws Exception {
        // Given
        int largePatientId = Integer.MAX_VALUE;
        when(noteService.getNotesByPatientId(largePatientId)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/note/{patId}", largePatientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(noteService, times(1)).getNotesByPatientId(largePatientId);
    }

    @Test
    @DisplayName("Should handle malformed JSON in request body")
    void createNote_WithMalformedJson_ShouldReturn400() throws Exception {
        // Étant donné
        String malformedJson = "{\"patId\": 1, \"note\": \"test\", invalid}";

        // Quand & Alors
        mockMvc.perform(post("/api/note")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());

        verify(noteService, times(0)).create(any(NoteDto.class));
    }

    @Test
    @DisplayName("Should handle empty request body")
    void createNote_WithEmptyBody_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/note")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());

        verify(noteService, times(0)).create(any(NoteDto.class));
    }
}