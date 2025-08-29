package fr.medilabo.solutions.rapport.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.medilabo.solutions.rapport.client.NoteServiceClient;
import fr.medilabo.solutions.rapport.client.PatientServiceClient;
import fr.medilabo.solutions.rapport.dto.DiabeteNiveauRisqueEnum;
import fr.medilabo.solutions.rapport.dto.NoteDto;
import fr.medilabo.solutions.rapport.dto.PatientDto;

import feign.FeignException;

@ExtendWith(MockitoExtension.class)
@DisplayName("rapport Service Tests")
public class RapportServiceTest {

    @Mock
    private PatientServiceClient patientServiceClient;

    @Mock
    private NoteServiceClient noteServiceClient;

    @InjectMocks
    private rapportService rapportService;

    private PatientDto createPatient(int id, String firstname, String lastname, LocalDate birthDate, String gender) {
        PatientDto patient = new PatientDto();
        patient.setId(id);
        patient.setFirstname(firstname);
        patient.setLastname(lastname);
        patient.setBirthDate(birthDate);
        patient.setGender(gender);
        patient.setAddress("123 Test Street");
        patient.setPhoneNumber("0123456789");
        return patient;
    }

    private NoteDto createNote(String id, int patId, String noteText) {
        NoteDto note = new NoteDto();
        note.setId(id);
        note.setPatId(patId);
        note.setPatient("Test Patient");
        note.setNote(noteText);
        return note;
    }

    @Nested
    @DisplayName("Risk rapport for Patients Over 30")
    class PatientsOver30Tests {

        @Test
        @DisplayName("Should return NONE when no triggers present - Male over 30")
        void assessDiabetesRisk_MaleOver30WithNoTriggers_ShouldReturnNone() {
            // Given
            int patientId = 1;
            PatientDto patient = createPatient(1, "John", "Doe", LocalDate.now().minusYears(35), "M");
            List<NoteDto> notes = Arrays.asList(
                    createNote("1", patientId, "Patient is doing well"),
                    createNote("2", patientId, "Regular checkup completed"));

            when(patientServiceClient.getPatientById(1)).thenReturn(patient);
            when(noteServiceClient.getNoteByPatientId(patientId)).thenReturn(notes);

            // When
            DiabeteNiveauRisqueEnum result = rapportService.assessDiabetesRisk(patientId);

            // Then
            assertEquals(DiabeteNiveauRisqueEnum.NONE, result);
        }

        @Test
        @DisplayName("Should return BORDERLINE when 2-5 triggers present - Female over 30")
        void assessDiabetesRisk_FemaleOver30WithBorderlineTriggers_ShouldReturnBorderline() {
            // Given
            int patientId = 2;
            PatientDto patient = createPatient(2, "Jane", "Doe", LocalDate.now().minusYears(45), "F");
            List<NoteDto> notes = Arrays.asList(
                    createNote("1", patientId, "Patient shows signs of Hémoglobine A1C elevation"),
                    createNote("2", patientId, "Microalbumine levels are concerning"),
                    createNote("3", patientId, "Patient's poids has increased significantly"));

            when(patientServiceClient.getPatientById(2)).thenReturn(patient);
            when(noteServiceClient.getNoteByPatientId(patientId)).thenReturn(notes);

            // When
            DiabeteNiveauRisqueEnum result = rapportService.assessDiabetesRisk(patientId);

            // Then
            assertEquals(DiabeteNiveauRisqueEnum.BORDERLINE, result);
        }

        @Test
        @DisplayName("Should return IN_DANGER when 6-7 triggers present - Male over 30")
        void assessDiabetesRisk_MaleOver30WithInDangerTriggers_ShouldReturnInDanger() {
            // Given
            int patientId = 3;
            PatientDto patient = createPatient(3, "Bob", "Smith", LocalDate.now().minusYears(50), "M");
            List<NoteDto> notes = Arrays.asList(
                    createNote("1", patientId, "Hémoglobine A1C, Microalbumine and Taille are concerning"),
                    createNote("2", patientId, "Patient is Fumeur and has Anormal cholestérol"),
                    createNote("3", patientId, "Vertige episodes reported"));

            when(patientServiceClient.getPatientById(3)).thenReturn(patient);
            when(noteServiceClient.getNoteByPatientId(patientId)).thenReturn(notes);

            // When
            DiabeteNiveauRisqueEnum result = rapportService.assessDiabetesRisk(patientId);

            // Then
            assertEquals(DiabeteNiveauRisqueEnum.IN_DANGER, result);
        }

        @Test
        @DisplayName("Should return EARLY_ONSET when 8+ triggers present - Female over 30")
        void assessDiabetesRisk_FemaleOver30WithEarlyOnsetTriggers_ShouldReturnEarlyOnset() {
            // Given
            int patientId = 4;
            PatientDto patient = createPatient(4, "Alice", "Johnson", LocalDate.now().minusYears(40), "F");
            List<NoteDto> notes = Arrays.asList(
                    createNote("1", patientId, "Hémoglobine A1C, Microalbumine, Taille, Poids"),
                    createNote("2", patientId, "Fumeuse with Anormal cholestérol and Vertige"),
                    createNote("3", patientId, "Rechute and Réaction to treatment, Anticorps present"));

            when(patientServiceClient.getPatientById(4)).thenReturn(patient);
            when(noteServiceClient.getNoteByPatientId(patientId)).thenReturn(notes);

            // When
            DiabeteNiveauRisqueEnum result = rapportService.assessDiabetesRisk(patientId);

            // Then
            assertEquals(DiabeteNiveauRisqueEnum.EARLY_ONSET, result);
        }
    }

    @Nested
    @DisplayName("Risk rapport for Young Males (30 or under)")
    class YoungMalesTests {

        @Test
        @DisplayName("Should return NONE when less than 3 triggers - Young Male")
        void assessDiabetesRisk_YoungMaleWithFewTriggers_ShouldReturnNone() {
            // Given
            int patientId = 5;
            PatientDto patient = createPatient(5, "Mike", "Young", LocalDate.now().minusYears(25), "M");
            List<NoteDto> notes = Arrays.asList(
                    createNote("1", patientId, "Patient has Hémoglobine A1C issues"),
                    createNote("2", patientId, "Some Microalbumine detected"));

            when(patientServiceClient.getPatientById(5)).thenReturn(patient);
            when(noteServiceClient.getNoteByPatientId(patientId)).thenReturn(notes);

            // When
            DiabeteNiveauRisqueEnum result = rapportService.assessDiabetesRisk(patientId);

            // Then
            assertEquals(DiabeteNiveauRisqueEnum.NONE, result);
        }

        @Test
        @DisplayName("Should return IN_DANGER when 3-4 triggers - Young Male")
        void assessDiabetesRisk_YoungMaleWithInDangerTriggers_ShouldReturnInDanger() {
            // Given
            int patientId = 6;
            PatientDto patient = createPatient(6, "Tom", "Teen", LocalDate.now().minusYears(20), "M");
            List<NoteDto> notes = Arrays.asList(
                    createNote("1", patientId, "Hémoglobine A1C and Microalbumine detected"),
                    createNote("2", patientId, "Patient's Taille and Poids are concerning"));

            when(patientServiceClient.getPatientById(6)).thenReturn(patient);
            when(noteServiceClient.getNoteByPatientId(patientId)).thenReturn(notes);

            // When
            DiabeteNiveauRisqueEnum result = rapportService.assessDiabetesRisk(patientId);

            // Then
            assertEquals(DiabeteNiveauRisqueEnum.IN_DANGER, result);
        }

        @Test
        @DisplayName("Should return EARLY_ONSET when 5+ triggers - Young Male")
        void assessDiabetesRisk_YoungMaleWithEarlyOnsetTriggers_ShouldReturnEarlyOnset() {
            // Given
            int patientId = 7;
            PatientDto patient = createPatient(7, "Jack", "Youth", LocalDate.now().minusYears(18), "M");
            List<NoteDto> notes = Arrays.asList(
                    createNote("1", patientId, "Hémoglobine A1C, Microalbumine, Taille issues"),
                    createNote("2", patientId, "Fumeur with Anormal cholestérol levels"));

            when(patientServiceClient.getPatientById(7)).thenReturn(patient);
            when(noteServiceClient.getNoteByPatientId(patientId)).thenReturn(notes);

            // When
            DiabeteNiveauRisqueEnum result = rapportService.assessDiabetesRisk(patientId);

            // Then
            assertEquals(DiabeteNiveauRisqueEnum.EARLY_ONSET, result);
        }
    }

    @Nested
    @DisplayName("Risk rapport for Young Females (30 or under)")
    class YoungFemalesTests {

        @Test
        @DisplayName("Should return NONE when less than 4 triggers - Young Female")
        void assessDiabetesRisk_YoungFemaleWithFewTriggers_ShouldReturnNone() {
            // Given
            int patientId = 8;
            PatientDto patient = createPatient(8, "Emma", "Young", LocalDate.now().minusYears(22), "F");
            List<NoteDto> notes = Arrays.asList(
                    createNote("1", patientId, "Hémoglobine A1C elevated"),
                    createNote("2", patientId, "Microalbumine present"),
                    createNote("3", patientId, "Patient's Taille measured"));

            when(patientServiceClient.getPatientById(8)).thenReturn(patient);
            when(noteServiceClient.getNoteByPatientId(patientId)).thenReturn(notes);

            // When
            DiabeteNiveauRisqueEnum result = rapportService.assessDiabetesRisk(patientId);

            // Then
            assertEquals(DiabeteNiveauRisqueEnum.NONE, result);
        }

        @Test
        @DisplayName("Should return IN_DANGER when 4-6 triggers - Young Female")
        void assessDiabetesRisk_YoungFemaleWithInDangerTriggers_ShouldReturnInDanger() {
            // Given
            int patientId = 9;
            PatientDto patient = createPatient(9, "Sarah", "Teen", LocalDate.now().minusYears(28), "F");
            List<NoteDto> notes = Arrays.asList(
                    createNote("1", patientId, "Hémoglobine A1C, Microalbumine issues"),
                    createNote("2", patientId, "Taille, Poids, and Fumeuse status noted"),
                    createNote("3", patientId, "Anormal results detected"));

            when(patientServiceClient.getPatientById(9)).thenReturn(patient);
            when(noteServiceClient.getNoteByPatientId(patientId)).thenReturn(notes);

            // When
            DiabeteNiveauRisqueEnum result = rapportService.assessDiabetesRisk(patientId);

            // Then
            assertEquals(DiabeteNiveauRisqueEnum.IN_DANGER, result);
        }

        @Test
        @DisplayName("Should return EARLY_ONSET when 7+ triggers - Young Female")
        void assessDiabetesRisk_YoungFemaleWithEarlyOnsetTriggers_ShouldReturnEarlyOnset() {
            // Given
            int patientId = 10;
            PatientDto patient = createPatient(10, "Lisa", "Youth", LocalDate.now().minusYears(19), "F");
            List<NoteDto> notes = Arrays.asList(
                    createNote("1", patientId, "Hémoglobine A1C, Microalbumine, Taille, Poids"),
                    createNote("2", patientId, "Fumeuse with Anormal cholestérol"),
                    createNote("3", patientId, "Vertige and Rechute episodes, Réaction noted"));

            when(patientServiceClient.getPatientById(10)).thenReturn(patient);
            when(noteServiceClient.getNoteByPatientId(patientId)).thenReturn(notes);

            // When
            DiabeteNiveauRisqueEnum result = rapportService.assessDiabetesRisk(patientId);

            // Then
            assertEquals(DiabeteNiveauRisqueEnum.EARLY_ONSET, result);
        }
    }

    @Nested
    @DisplayName("Trigger Terms Detection Tests")
    class TriggerTermsTests {

        @Test
        @DisplayName("Should detect all trigger terms case-insensitively")
        void assessDiabetesRisk_WithAllTriggerTermsInDifferentCases_ShouldDetectAll() {
            // Given
            int patientId = 11;
            PatientDto patient = createPatient(11, "Test", "Patient", LocalDate.now().minusYears(35), "M");
            List<NoteDto> notes = Arrays.asList(
                    createNote("1", patientId, "HÉMOGLOBINE A1C and microalbumine detected"),
                    createNote("2", patientId, "Taille and POIDS measurements"),
                    createNote("3", patientId, "Patient is FUMEUR"),
                    createNote("4", patientId, "ANORMAL cholestérol levels"),
                    createNote("5", patientId, "vertige and RECHUTE reported"),
                    createNote("6", patientId, "Réaction to treatment and ANTICORPS found"));

            when(patientServiceClient.getPatientById(11)).thenReturn(patient);
            when(noteServiceClient.getNoteByPatientId(patientId)).thenReturn(notes);

            // When
            DiabeteNiveauRisqueEnum result = rapportService.assessDiabetesRisk(patientId);

            // Then
            // 10 triggers detected -> EARLY_ONSET for over 30
            assertEquals(DiabeteNiveauRisqueEnum.EARLY_ONSET, result);
        }

        @Test
        @DisplayName("Should count each trigger term only once even if mentioned multiple times")
        void assessDiabetesRisk_WithRepeatedTriggerTerms_ShouldCountOnlyOnce() {
            // Given
            int patientId = 12;
            PatientDto patient = createPatient(12, "Test", "Patient", LocalDate.now().minusYears(35), "M");
            List<NoteDto> notes = Arrays.asList(
                    createNote("1", patientId, "Hémoglobine A1C is high, Hémoglobine A1C needs monitoring"),
                    createNote("2", patientId, "Microalbumine detected, microalbumine levels rising"),
                    createNote("3", patientId, "Patient's poids is concerning, poids management needed"));

            when(patientServiceClient.getPatientById(12)).thenReturn(patient);
            when(noteServiceClient.getNoteByPatientId(patientId)).thenReturn(notes);

            // When
            DiabeteNiveauRisqueEnum result = rapportService.assessDiabetesRisk(patientId);

            // Then
            // Only 3 distinct triggers -> BORDERLINE for over 30
            assertEquals(DiabeteNiveauRisqueEnum.BORDERLINE, result);
        }

        @Test
        @DisplayName("Should handle female smoker term 'fumeuse'")
        void assessDiabetesRisk_WithFumeuseTerm_ShouldDetectSmokerTrigger() {
            // Given
            int patientId = 13;
            PatientDto patient = createPatient(13, "Marie", "Test", LocalDate.now().minusYears(35), "F");
            List<NoteDto> notes = Arrays.asList(
                    createNote("1", patientId, "Patient est fumeuse depuis 10 ans"),
                    createNote("2", patientId, "Hémoglobine A1C elevated"));

            when(patientServiceClient.getPatientById(13)).thenReturn(patient);
            when(noteServiceClient.getNoteByPatientId(patientId)).thenReturn(notes);

            // When
            DiabeteNiveauRisqueEnum result = rapportService.assessDiabetesRisk(patientId);

            // Then
            // 2 triggers -> BORDERLINE for over 30
            assertEquals(DiabeteNiveauRisqueEnum.BORDERLINE, result);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty notes list")
        void assessDiabetesRisk_WithEmptyNotes_ShouldReturnNone() {
            // Given
            int patientId = 14;
            PatientDto patient = createPatient(14, "Empty", "Notes", LocalDate.now().minusYears(35), "M");
            List<NoteDto> notes = Collections.emptyList();

            when(patientServiceClient.getPatientById(14)).thenReturn(patient);
            when(noteServiceClient.getNoteByPatientId(patientId)).thenReturn(notes);

            // When
            DiabeteNiveauRisqueEnum result = rapportService.assessDiabetesRisk(patientId);

            // Then
            assertEquals(DiabeteNiveauRisqueEnum.NONE, result);
        }

        @Test
        @DisplayName("Should handle notes with null content")
        void assessDiabetesRisk_WithNullNoteContent_ShouldHandleGracefully() {
            // Given
            int patientId = 15;
            PatientDto patient = createPatient(15, "Null", "Notes", LocalDate.now().minusYears(35), "M");
            NoteDto noteWithNull = new NoteDto();
            noteWithNull.setId("1");
            noteWithNull.setPatId(patientId);
            noteWithNull.setNote(null);
            List<NoteDto> notes = Arrays.asList(noteWithNull);

            when(patientServiceClient.getPatientById(15)).thenReturn(patient);
            when(noteServiceClient.getNoteByPatientId(patientId)).thenReturn(notes);

            // When & Then
            assertThrows(NullPointerException.class, () -> {
                rapportService.assessDiabetesRisk(patientId);
            });
        }

        @Test
        @DisplayName("Should handle patient exactly 30 years old")
        void assessDiabetesRisk_WithPatientExactly30_ShouldUseYoungRules() {
            // Given
            int patientId = 16;
            PatientDto patient = createPatient(16, "Thirty", "YearsOld", LocalDate.now().minusYears(30), "M");
            List<NoteDto> notes = Arrays.asList(
                    createNote("1", patientId, "Hémoglobine A1C, Microalbumine detected"),
                    createNote("2", patientId, "Taille measured"));

            when(patientServiceClient.getPatientById(16)).thenReturn(patient);
            when(noteServiceClient.getNoteByPatientId(patientId)).thenReturn(notes);

            // When
            DiabeteNiveauRisqueEnum result = rapportService.assessDiabetesRisk(patientId);

            // Then
            // 3 triggers, male 30 years old -> BORDERLINE (young rules)
            assertEquals(DiabeteNiveauRisqueEnum.BORDERLINE, result);
        }

        @Test
        @DisplayName("Should handle patient born today (0 years old)")
        void assessDiabetesRisk_WithNewbornPatient_ShouldCalculateCorrectly() {
            // Given
            int patientId = 17;
            PatientDto patient = createPatient(17, "New", "Born", LocalDate.now(), "M");
            List<NoteDto> notes = Arrays.asList(
                    createNote("1", patientId, "Hémoglobine A1C issues detected"));

            when(patientServiceClient.getPatientById(17)).thenReturn(patient);
            when(noteServiceClient.getNoteByPatientId(patientId)).thenReturn(notes);

            // When
            DiabeteNiveauRisqueEnum result = rapportService.assessDiabetesRisk(patientId);

            // Then
            // 1 trigger, male 0 years old -> NONE (need 3+ for IN_DANGER)
            assertEquals(DiabeteNiveauRisqueEnum.NONE, result);
        }

        @Test
        @DisplayName("Should throw exception when patient service fails")
        void assessDiabetesRisk_WhenPatientServiceFails_ShouldThrowException() {
            // Given
            int patientId = 18;
            when(patientServiceClient.getPatientById(18))
                    .thenThrow(FeignException.FeignClientException.class);

            // When & Then
            assertThrows(FeignException.FeignClientException.class, () -> {
                rapportService.assessDiabetesRisk(patientId);
            });

            verify(patientServiceClient, times(1)).getPatientById(18);
        }

        @Test
        @DisplayName("Should throw exception when note service fails")
        void assessDiabetesRisk_WhenNoteServiceFails_ShouldThrowException() {
            // Given
            int patientId = 19;
            PatientDto patient = createPatient(19, "Test", "Patient", LocalDate.now().minusYears(35), "M");

            when(patientServiceClient.getPatientById(19)).thenReturn(patient);
            when(noteServiceClient.getNoteByPatientId(patientId))
                    .thenThrow(FeignException.FeignClientException.class);

            // When & Then
            assertThrows(FeignException.FeignClientException.class, () -> {
                rapportService.assessDiabetesRisk(patientId);
            });

            verify(patientServiceClient, times(1)).getPatientById(19);
            verify(noteServiceClient, times(1)).getNoteByPatientId(patientId);
        }
    }

    @Nested
    @DisplayName("Service Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should call both services with correct parameters")
        void assessDiabetesRisk_ShouldCallServicesWithCorrectParameters() {
            // Given
            int patientId = 20;
            PatientDto patient = createPatient(20, "Integration", "Test", LocalDate.now().minusYears(35), "M");
            List<NoteDto> notes = Arrays.asList(
                    createNote("1", patientId, "Regular checkup"));

            when(patientServiceClient.getPatientById(20)).thenReturn(patient);
            when(noteServiceClient.getNoteByPatientId(patientId)).thenReturn(notes);

            // When
            DiabeteNiveauRisqueEnum result = rapportService.assessDiabetesRisk(patientId);

            // Then
            verify(patientServiceClient, times(1)).getPatientById(20);
            verify(noteServiceClient, times(1)).getNoteByPatientId(patientId);
            assertEquals(DiabeteNiveauRisqueEnum.NONE, result);
        }

        @Test
        @DisplayName("Should handle multiple rapport calls")
        void assessDiabetesRisk_WithMultipleCalls_ShouldHandleCorrectly() {
            // Given
            int patientId1 = 21;
            int patientId2 = 22;

            PatientDto patient1 = createPatient(21, "Patient", "One", LocalDate.now().minusYears(35), "M");
            PatientDto patient2 = createPatient(22, "Patient", "Two", LocalDate.now().minusYears(25), "F");

            List<NoteDto> notes1 = Arrays.asList(createNote("1", patientId1, "Normal checkup"));
            List<NoteDto> notes2 = Arrays.asList(createNote("2", patientId2, "Hémoglobine A1C detected"));

            when(patientServiceClient.getPatientById(21)).thenReturn(patient1);
            when(patientServiceClient.getPatientById(22)).thenReturn(patient2);
            when(noteServiceClient.getNoteByPatientId(patientId1)).thenReturn(notes1);
            when(noteServiceClient.getNoteByPatientId(patientId2)).thenReturn(notes2);

            // When
            DiabeteNiveauRisqueEnum result1 = rapportService.assessDiabetesRisk(patientId1);
            DiabeteNiveauRisqueEnum result2 = rapportService.assessDiabetesRisk(patientId2);

            // Then
            assertEquals(DiabeteNiveauRisqueEnum.NONE, result1);
            assertEquals(DiabeteNiveauRisqueEnum.NONE, result2);

            verify(patientServiceClient, times(1)).getPatientById(21);
            verify(patientServiceClient, times(1)).getPatientById(22);
            verify(noteServiceClient, times(1)).getNoteByPatientId(patientId1);
            verify(noteServiceClient, times(1)).getNoteByPatientId(patientId2);
        }
    }

    @Nested
    @DisplayName("Boundary Value Tests")
    class BoundaryValueTests {

        @Test
        @DisplayName("Should correctly handle age boundary at 31 years")
        void assessDiabetesRisk_WithPatient31YearsOld_ShouldUseAdultRules() {
            // Given
            int patientId = 23;
            PatientDto patient = createPatient(23, "Thirty", "One", LocalDate.now().minusYears(31), "M");
            List<NoteDto> notes = Arrays.asList(
                    createNote("1", patientId, "Hémoglobine A1C and Microalbumine detected"));

            when(patientServiceClient.getPatientById(23)).thenReturn(patient);
            when(noteServiceClient.getNoteByPatientId(patientId)).thenReturn(notes);

            // When
            DiabeteNiveauRisqueEnum result = rapportService.assessDiabetesRisk(patientId);

            // Then
            // 2 triggers, male 31 years old -> BORDERLINE (adult rules)
            assertEquals(DiabeteNiveauRisqueEnum.BORDERLINE, result);
        }

        @Test
        @DisplayName("Should handle exactly 5 triggers for male over 30")
        void assessDiabetesRisk_MaleOver30WithExactly5Triggers_ShouldReturnBorderline() {
            // Given
            int patientId = 24;
            PatientDto patient = createPatient(24, "Five", "Triggers", LocalDate.now().minusYears(35), "M");
            List<NoteDto> notes = Arrays.asList(
                    createNote("1", patientId, "Hémoglobine A1C, Microalbumine, Taille"),
                    createNote("2", patientId, "Poids and Fumeur status noted"));

            when(patientServiceClient.getPatientById(24)).thenReturn(patient);
            when(noteServiceClient.getNoteByPatientId(patientId)).thenReturn(notes);

            // When
            DiabeteNiveauRisqueEnum result = rapportService.assessDiabetesRisk(patientId);

            // Then
            assertEquals(DiabeteNiveauRisqueEnum.BORDERLINE, result);
        }

        @Test
        @DisplayName("Should handle exactly 6 triggers for female over 30")
        void assessDiabetesRisk_FemaleOver30WithExactly6Triggers_ShouldReturnInDanger() {
            // Given
            int patientId = 25;
            PatientDto patient = createPatient(25, "Six", "Triggers", LocalDate.now().minusYears(35), "F");
            List<NoteDto> notes = Arrays.asList(
                    createNote("1", patientId, "Hémoglobine A1C, Microalbumine, Taille"),
                    createNote("2", patientId, "Poids, Fumeuse and Anormal detected"));

            when(patientServiceClient.getPatientById(25)).thenReturn(patient);
            when(noteServiceClient.getNoteByPatientId(patientId)).thenReturn(notes);

            // When
            DiabeteNiveauRisqueEnum result = rapportService.assessDiabetesRisk(patientId);

            // Then
            assertEquals(DiabeteNiveauRisqueEnum.IN_DANGER, result);
        }
    }
}