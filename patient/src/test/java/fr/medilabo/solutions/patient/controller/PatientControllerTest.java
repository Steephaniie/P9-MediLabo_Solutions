package fr.medilabo.solutions.patient.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.medilabo.solutions.patient.dto.PatientDto;
import fr.medilabo.solutions.patient.exception.GlobalExceptionHandler;
import fr.medilabo.solutions.patient.exception.ResourceNotFoundException;
import fr.medilabo.solutions.patient.security.JwtAuthenticationFilter;
import fr.medilabo.solutions.patient.security.JwtUtil;
import fr.medilabo.solutions.patient.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Classe de tests pour PatientController.
 * Cette classe vérifie le bon fonctionnement des endpoints REST
 * pour la gestion des patients.
 */
@WebMvcTest(PatientController.class)
@Import({GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("Patient Controller Tests")
class PatientControllerTest {

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private MockMvc mockMvc;
    @MockBean

    private PatientService patientService;
    @Autowired
    private ObjectMapper objectMapper;
    private PatientDto patientDto;
    private List<PatientDto> listePatients;


    /**
     * Initialise les données de test avant chaque test.
     * Crée deux patients avec des données différentes.
     */
    @BeforeEach
    void initialiser() {
        patientDto = new PatientDto();
        patientDto.setId(1);
        patientDto.setFirstname("Jean");
        patientDto.setLastname("Dupont");
        patientDto.setBirthDate(LocalDate.of(1990, 1, 1));
        patientDto.setGender("M");
        patientDto.setAddress("123 Rue Principale");
        patientDto.setPhoneNumber("0123456789");

        PatientDto patientDto2 = new PatientDto();
        patientDto2.setId(2);
        patientDto2.setFirstname("Marie");
        patientDto2.setLastname("Martin");
        patientDto2.setBirthDate(LocalDate.of(1985, 5, 15));
        patientDto2.setGender("F");
        patientDto2.setAddress("456 Avenue des Chênes");
        patientDto2.setPhoneNumber("0987654321");

        listePatients = Arrays.asList(patientDto, patientDto2);
    }

    /**
     * Teste la récupération de tous les patients.
     * Vérifie que l'endpoint retourne la liste complète des patients.
     *
     * @throws Exception si une erreur survient pendant l'exécution du test
     */
    @Test
    @DisplayName("Devrait retourner tous les patients")
//    @WithMockUser(roles = "USER")
    void obtenirTousLesPatients_DevraitRetournerTousLesPatients() throws Exception {
        // Étant donné
        when(patientService.findAll()).thenReturn(listePatients);

        // When & Then
        mockMvc.perform(get("/api/patient"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].firstname").value("Jean"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].firstname").value("Marie"));
    }

    /**
     * Teste la récupération d'un patient par son ID.
     * Vérifie que l'endpoint retourne le bon patient pour un ID valide.
     *
     * @throws Exception si une erreur survient pendant l'exécution du test
     */
    @Test
    @DisplayName("Devrait retourner le patient par son ID")
    void obtenirPatient_AvecIdValide_DevraitRetournerPatient() throws Exception {
        // Given
        when(patientService.findById(1)).thenReturn(patientDto);

        // When & Then
        mockMvc.perform(get("/api/patient/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstname").value("Jean"))
                .andExpect(jsonPath("$.lastname").value("Dupont"));
    }

    /**
     * Teste la gestion des erreurs pour un ID de patient invalide.
     * Vérifie que l'endpoint retourne une erreur 404 quand le patient n'existe pas.
     *
     * @throws Exception si une erreur survient pendant l'exécution du test
     */
    @Test
    @DisplayName("Devrait retourner 404 quand le patient n'est pas trouvé")
    void obtenirPatient_AvecIdInvalide_DevraitRetourner404() throws Exception {
        // Given
        when(patientService.findById(999)).thenThrow(new ResourceNotFoundException("Patient not found with id: 999"));

        // When & Then
        ResultActions retour = mockMvc.perform(get("/api/patient/999"));
                retour.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Patient introuvable"))
                .andExpect(jsonPath("$.message").value("Patient not found with id: 999"));
    }

    /**
     * Teste la création d'un nouveau patient.
     * Vérifie que l'endpoint crée correctement un patient avec des données valides.
     *
     * @throws Exception si une erreur survient pendant l'exécution du test
     */
    @Test
    @DisplayName("Devrait créer un patient avec succès")
    void creerPatient_AvecDonneesValides_DevraitCreerPatient() throws Exception {
        // Given
        PatientDto newPatient = new PatientDto();
        newPatient.setFirstname("Alice");
        newPatient.setLastname("MonChaton");
        newPatient.setBirthDate(LocalDate.of(2008, 3, 20));
        newPatient.setGender("F");
        newPatient.setAddress("789 Pine St");
        newPatient.setPhoneNumber("0555123456");

        PatientDto createdPatient = new PatientDto();
        createdPatient.setId(3);
        createdPatient.setFirstname("Alice");
        createdPatient.setLastname("MonChaton");
        createdPatient.setBirthDate(LocalDate.of(2008, 3, 20));
        createdPatient.setGender("F");
        createdPatient.setAddress("789 Pine St");
        createdPatient.setPhoneNumber("0555123456");

        when(patientService.create(any(PatientDto.class))).thenReturn(createdPatient);

        // When & Then
        mockMvc.perform(post("/api/patient")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newPatient)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.firstname").value("Alice"));
    }

    /**
     * Teste la validation des données lors de la création d'un patient.
     * Vérifie que l'endpoint retourne une erreur 400 pour des données invalides.
     *
     * @throws Exception si une erreur survient pendant l'exécution du test
     */
    @Test
    @DisplayName("Devrait retourner 400 pour des données patient invalides")
    void creerPatient_AvecDonneesInvalides_DevraitRetourner400() throws Exception {
        // Given
        PatientDto invalidPatient = new PatientDto();
        invalidPatient.setFirstname(""); // Invalid: blank firstname
        invalidPatient.setLastname("Dupont");
        invalidPatient.setBirthDate(LocalDate.now().plusDays(1)); // Invalid: future date
        invalidPatient.setGender("X"); // Invalid: wrong gender

        // When & Then
        mockMvc.perform(post("/api/patient")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPatient)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Teste la mise à jour d'un patient existant.
     * Vérifie que l'endpoint met à jour correctement les données du patient.
     *
     * @throws Exception si une erreur survient pendant l'exécution du test
     */
    @Test
    @DisplayName("Devrait mettre à jour le patient avec succès")
    void mettreAJourPatient_AvecDonneesValides_DevraitMettreAJourPatient() throws Exception {
        // Given
        PatientDto updatedPatient = new PatientDto();
        updatedPatient.setId(1);
        updatedPatient.setFirstname("Jean Updated");
        updatedPatient.setLastname("Dupont Updated");
        updatedPatient.setBirthDate(LocalDate.of(1990, 1, 1));
        updatedPatient.setGender("M");
        updatedPatient.setAddress("123 Updated St");
        updatedPatient.setPhoneNumber("0123456789");

        when(patientService.findById(1)).thenReturn(patientDto);
        when(patientService.update(any(PatientDto.class))).thenReturn(updatedPatient);

        // When & Then
        mockMvc.perform(put("/api/patient/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedPatient)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstname").value("Jean Updated"))
                .andExpect(jsonPath("$.address").value("123 Updated St"));
    }

    /**
     * Teste la mise à jour d'un patient inexistant.
     * Vérifie que l'endpoint retourne une erreur 404 pour un ID invalide.
     *
     * @throws Exception si une erreur survient pendant l'exécution du test
     */
    @Test
    @DisplayName("Devrait retourner 404 lors de la mise à jour d'un patient inexistant")
    void mettreAJourPatient_AvecIdInvalide_DevraitRetourner404() throws Exception {
        // Given
        when(patientService.findById(999)).thenThrow(new ResourceNotFoundException("Patient not found with id: 999"));

        // When & Then
        mockMvc.perform(put("/api/patient/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patientDto)))
                .andExpect(status().isNotFound());
    }

    /**
     * Teste la suppression d'un patient existant.
     * Vérifie que l'endpoint supprime correctement le patient.
     *
     * @throws Exception si une erreur survient pendant l'exécution du test
     */
    @Test
    @DisplayName("Devrait supprimer le patient avec succès")
    void supprimerPatient_AvecIdValide_DevraitSupprimerPatient() throws Exception {
        // Given
        when(patientService.findById(1)).thenReturn(patientDto);
        doNothing().when(patientService).delete(any(PatientDto.class));

        // When & Then
        mockMvc.perform(delete("/api/patient/1"))
                .andExpect(status().isNoContent());
    }

    /**
     * Teste la suppression d'un patient inexistant.
     * Vérifie que l'endpoint retourne une erreur 404 pour un ID invalide.
     *
     * @throws Exception si une erreur survient pendant l'exécution du test
     */
    @Test
    @DisplayName("Devrait retourner 404 lors de la suppression d'un patient inexistant")
    void supprimerPatient_AvecIdInvalide_DevraitRetourner404() throws Exception {
        // Given
        when(patientService.findById(999)).thenThrow(new ResourceNotFoundException("Patient not found with id: 999"));

        // When & Then
        mockMvc.perform(delete("/api/patient/999"))
                .andExpect(status().isNotFound());
    }
}