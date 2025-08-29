package fr.medilabo.solutions.rapport.controller;

import fr.medilabo.solutions.rapport.dto.DiabeteNiveauRisqueEnum;
import fr.medilabo.solutions.rapport.security.JwtUtil;
import fr.medilabo.solutions.rapport.service.rapportService;
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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WebMvcTest(rapportController.class)
/**
 * Tests du contrôleur de rapport.
 * Vérifie le comportement des endpoints pour l'évaluation des risques de diabète.
 */
@DisplayName("Tests du Contrôleur de Rapport")
public class rapportControllerTest {
    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private rapportService rapportService;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Devrait retourner un niveau de risque NONE pour le patient")
    void getrapport_WithPatientHavingNoRisk_ShouldReturnNone() throws Exception {
        // Étant donné
        Long patientId = 1L;
        when(rapportService.assessDiabetesRisk(patientId.intValue()))
                .thenReturn(DiabeteNiveauRisqueEnum.NONE);

        // Quand & Alors
        mockMvc.perform(get("/api/rapport/{patId}", patientId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value("NONE"));

        verify(rapportService, times(1)).assessDiabetesRisk(patientId.intValue());
    }

    @Test
    @DisplayName("Devrait retourner un niveau de risque BORDERLINE pour le patient")
    void getrapport_WithPatientHavingBorderlineRisk_ShouldReturnBorderline() throws Exception {
        // Étant donné
        Long patientId = 2L;
        when(rapportService.assessDiabetesRisk(patientId.intValue()))
                .thenReturn(DiabeteNiveauRisqueEnum.BORDERLINE);

        // Quand & Alors
        mockMvc.perform(get("/api/rapport/{patId}", patientId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value("BORDERLINE"));

        verify(rapportService, times(1)).assessDiabetesRisk(patientId.intValue());
    }

    @Test
    @DisplayName("Devrait retourner un niveau de risque IN_DANGER pour le patient")
    void getrapport_WithPatientInDanger_ShouldReturnInDanger() throws Exception {
        // Étant donné
        Long patientId = 3L;
        when(rapportService.assessDiabetesRisk(patientId.intValue()))
                .thenReturn(DiabeteNiveauRisqueEnum.IN_DANGER);

        // Quand & Alors
        mockMvc.perform(get("/api/rapport/{patId}", patientId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value("IN_DANGER"));

        verify(rapportService, times(1)).assessDiabetesRisk(patientId.intValue());
    }

    @Test
    @DisplayName("Devrait retourner un niveau de risque EARLY_ONSET pour le patient")
    void getrapport_WithPatientHavingEarlyOnset_ShouldReturnEarlyOnset() throws Exception {
        // Étant donné
        Long patientId = 4L;
        when(rapportService.assessDiabetesRisk(patientId.intValue()))
                .thenReturn(DiabeteNiveauRisqueEnum.EARLY_ONSET);

        // Quand & Alors
        mockMvc.perform(get("/api/rapport/{patId}", patientId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value("EARLY_ONSET"));

        verify(rapportService, times(1)).assessDiabetesRisk(patientId.intValue());
    }

    @Test
    @DisplayName("Should handle different patient IDs correctly")
    void getrapport_WithDifferentPatientIds_ShouldCallServiceWithCorrectIds() throws Exception {
        Long[] patientIds = {1L, 5L, 100L, 999L, 1000L};

        for (Long patientId : patientIds) {
            // Given
            when(rapportService.assessDiabetesRisk(patientId.intValue()))
                    .thenReturn(DiabeteNiveauRisqueEnum.NONE);

            // When & Then
            mockMvc.perform(get("/api/rapport/{patId}", patientId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value("NONE"));

            verify(rapportService, times(1)).assessDiabetesRisk(patientId.intValue());
        }
    }

    @Test
    @DisplayName("Should handle large patient ID correctly")
    void getrapport_WithLargePatientId_ShouldReturnrapport() throws Exception {
        // Given
        Long largePatientId = 2147483647L;
        when(rapportService.assessDiabetesRisk(largePatientId.intValue()))
                .thenReturn(DiabeteNiveauRisqueEnum.BORDERLINE);

        // When & Then
        mockMvc.perform(get("/api/rapport/{patId}", largePatientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("BORDERLINE"));

        verify(rapportService, times(1)).assessDiabetesRisk(largePatientId.intValue());
    }

    @Test
    @DisplayName("Should return 400 when service throws IllegalArgumentException")
    void getrapport_WhenServiceThrowsIllegalArgumentException_ShouldReturn400() throws Exception {
        // Given
        Long patientId = -1L;
        when(rapportService.assessDiabetesRisk(patientId.intValue()))
                .thenThrow(new IllegalArgumentException("Invalid patient ID"));

        // When & Then
        mockMvc.perform(get("/api/rapport/{patId}", patientId))
                .andExpect(status().isBadRequest());

        verify(rapportService, times(1)).assessDiabetesRisk(patientId.intValue());
    }

    @Test
    @DisplayName("Should handle zero patient ID")
    void getrapport_WithZeroPatientId_ShouldCallService() throws Exception {
        // Given
        Long patientId = 0L;
        when(rapportService.assessDiabetesRisk(patientId.intValue()))
                .thenReturn(DiabeteNiveauRisqueEnum.NONE);

        // When & Then
        mockMvc.perform(get("/api/rapport/{patId}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("NONE"));

        verify(rapportService, times(1)).assessDiabetesRisk(patientId.intValue());
    }

    @Test
    @DisplayName("Should handle negative patient ID")
    void getrapport_WithNegativePatientId_ShouldCallService() throws Exception {
        // Given
        Long patientId = -5L;
        when(rapportService.assessDiabetesRisk(patientId.intValue()))
                .thenReturn(DiabeteNiveauRisqueEnum.NONE);

        // When & Then
        mockMvc.perform(get("/api/rapport/{patId}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("NONE"));

        verify(rapportService, times(1)).assessDiabetesRisk(patientId.intValue());
    }

    @Test
    @DisplayName("Should return 400 for invalid path parameter format")
    void getrapport_WithInvalidPathParameter_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/rapport/{patId}", "invalid"))
                .andExpect(status().isBadRequest());

        verify(rapportService, times(0)).assessDiabetesRisk(anyInt());
    }

    @Test
    @DisplayName("Should return 400 for non-numeric path parameter")
    void getrapport_WithNonNumericPathParameter_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/rapport/{patId}", "abc"))
                .andExpect(status().isBadRequest());

        verify(rapportService, times(0)).assessDiabetesRisk(anyInt());
    }

    @Test
    @DisplayName("Should return 400 for decimal path parameter")
    void getrapport_WithDecimalPathParameter_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/rapport/{patId}", "123.45"))
                .andExpect(status().isBadRequest());

        verify(rapportService, times(0)).assessDiabetesRisk(anyInt());
    }

    @Test
    @DisplayName("Should return 404 for missing path parameter")
    void getrapport_WithMissingPathParameter_ShouldReturn404() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/rapport/"))
                .andExpect(status().isBadRequest());

        verify(rapportService, times(0)).assessDiabetesRisk(anyInt());
    }

    @Test
    @DisplayName("Should log patient ID and risk level")
    void getrapport_ShouldLogCorrectInformation() throws Exception {
        // Given
        Long patientId = 42L;
        when(rapportService.assessDiabetesRisk(patientId.intValue()))
                .thenReturn(DiabeteNiveauRisqueEnum.IN_DANGER);

        // When & Then
        mockMvc.perform(get("/api/rapport/{patId}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("IN_DANGER"));

        verify(rapportService, times(1)).assessDiabetesRisk(42);
    }

    @Test
    @DisplayName("Should handle multiple consecutive requests correctly")
    void getrapport_WithMultipleConsecutiveRequests_ShouldHandleAllCorrectly() throws Exception {
        // Given
        Long patientId = 1L;
        when(rapportService.assessDiabetesRisk(patientId.intValue()))
                .thenReturn(DiabeteNiveauRisqueEnum.BORDERLINE);

        // When & Then
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/api/rapport/{patId}", patientId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value("BORDERLINE"));
        }

        verify(rapportService, times(3)).assessDiabetesRisk(patientId.intValue());
    }

    @Test
    @DisplayName("Should handle very large patient ID that causes integer overflow")
    void getrapport_WithVeryLargePatientId_ShouldHandleOverflow() throws Exception {
        // Given
        Long veryLargePatientId = 3000000000L;

        when(rapportService.assessDiabetesRisk(veryLargePatientId.intValue()))
                .thenReturn(DiabeteNiveauRisqueEnum.NONE);

        // When & Then
        mockMvc.perform(get("/api/rapport/{patId}", veryLargePatientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("NONE"));

        verify(rapportService, times(1)).assessDiabetesRisk(veryLargePatientId.intValue());
    }

    @Test
    @DisplayName("Should handle edge case where service returns null")
    void getrapport_WhenServiceReturnsNull_ShouldHandleGracefully() throws Exception {
        // Given
        Long patientId = 1L;
        when(rapportService.assessDiabetesRisk(patientId.intValue()))
                .thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/rapport/{patId}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());

        verify(rapportService, times(1)).assessDiabetesRisk(patientId.intValue());
    }

    @Test
    @DisplayName("Should correctly convert Long to int for service call")
    void getrapport_ShouldCorrectlyConvertLongToInt() throws Exception {
        // Given
        Long patientId = 123456L;
        when(rapportService.assessDiabetesRisk(123456))
                .thenReturn(DiabeteNiveauRisqueEnum.EARLY_ONSET);

        // When & Then
        mockMvc.perform(get("/api/rapport/{patId}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("EARLY_ONSET"));

        verify(rapportService, times(1)).assessDiabetesRisk(123456);
    }

    @Test
    @DisplayName("Should handle rapport for all risk levels in sequence")
    void getrapport_WithAllRiskLevels_ShouldReturnCorrectValues() throws Exception {
        DiabeteNiveauRisqueEnum[] riskLevels = DiabeteNiveauRisqueEnum.values();

        for (int i = 0; i < riskLevels.length; i++) {
            // Given
            Long patientId = (long) (i + 1);
            DiabeteNiveauRisqueEnum expectedRisk = riskLevels[i];

            when(rapportService.assessDiabetesRisk(patientId.intValue()))
                    .thenReturn(expectedRisk);

            // When & Then
            mockMvc.perform(get("/api/rapport/{patId}", patientId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(expectedRisk.name()));

            verify(rapportService, times(1)).assessDiabetesRisk(patientId.intValue());
        }
    }
}