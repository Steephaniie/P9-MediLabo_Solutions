package fr.medilabo.solutions.front.controller;

import fr.medilabo.solutions.front.client.NoteServiceClient;
import fr.medilabo.solutions.front.client.PatientServiceClient;
import fr.medilabo.solutions.front.client.RapportPatientServiceClient;
import fr.medilabo.solutions.front.config.UrlConfiguration;
import fr.medilabo.solutions.front.dto.DiabeteNiveauRisqueEnum;
import fr.medilabo.solutions.front.dto.NoteDto;
import fr.medilabo.solutions.front.dto.PatientDto;
import fr.medilabo.solutions.front.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour le NoteController utilisant Spring MVC Test.
 * Cette classe vérifie le comportement des points d'entrée REST liés aux notes des patients.
 * Les filtres de sécurité sont désactivés pour se concentrer sur la logique métier.
 */
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WebMvcTest(NoteController.class)
@DisplayName("NoteController - Tests Web MVC")
class NoteControllerTest {
    /**
     * Mock du service d'authentification JWT
     */
    @MockBean
    private JwtUtil jwtUtil;

    /**
     * Mock de la configuration des URLs
     */
    @MockBean
    private UrlConfiguration urlConfiguration;

    /**
     * Client MockMvc pour simuler les requêtes HTTP
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Mock du client service pour les notes
     */
    @MockBean
    private NoteServiceClient noteServiceClient;

    /**
     * Mock du client service pour les patients
     */
    @MockBean
    private PatientServiceClient patientServiceClient;

    /**
     * Mock du client service pour les rapports patients
     */
    @MockBean
    private RapportPatientServiceClient rapportPatientServiceClient;

    /**
     * Teste la récupération des notes d'un patient avec succès.
     * Vérifie que la vue retournée contient :
     * - Les informations du patient
     * - La liste des notes associées
     * - Le niveau de risque calculé
     * - Un objet note vide pour la création
     *
     * @throws Exception en cas d'erreur lors de l'exécution du test
     */
    @Test
    @DisplayName("GET /note/{id} - succès: vue patientNote avec patient, notes, niveauRisque et newNote")
    void getPatientNote_shouldReturnViewWithModel_onSuccess() throws Exception {
        int patientId = 1;

        PatientDto patient = new PatientDto();
        patient.setId(patientId);
        patient.setFirstname("Jean");
        patient.setLastname("Dupont");

        NoteDto n1 = new NoteDto();
        n1.setId("n1");
        n1.setPatId(patientId);
        n1.setPatient("Jean");
        n1.setNote("Note A");

        NoteDto n2 = new NoteDto();
        n2.setId("n2");
        n2.setPatId(patientId);
        n2.setPatient("Jean");
        n2.setNote("Note B");

        when(patientServiceClient.getPatientById((long) patientId)).thenReturn(patient);
        when(noteServiceClient.getNoteByPatientId(patientId)).thenReturn(List.of(n1, n2));
        when(rapportPatientServiceClient.getRapportByIdPatient((long) patientId))
                .thenReturn(DiabeteNiveauRisqueEnum.NONE);

        mockMvc.perform(get("/note/{id}", patientId))
                .andExpect(status().isOk())
                .andExpect(view().name("patientNote"))
                .andExpect(model().attributeExists("patient", "notes", "niveauRisque", "newNote"))
                .andExpect(model().attribute("patient", hasProperty("firstname", is("Jean"))))
                .andExpect(model().attribute("notes", hasSize(2)))
                .andExpect(model().attribute("niveauRisque", is("NONE")))
                .andExpect(model().attribute("newNote", hasProperty("patId", is(patientId))));
    }

    /**
     * Teste le comportement en cas d'erreur lors de la récupération des notes.
     * Vérifie que la vue affiche un message d'erreur approprié.
     *
     * @throws Exception en cas d'erreur lors de l'exécution du test
     */
    @Test
    @DisplayName("GET /note/{id} - erreur: vue patientNote avec attribut error")
    void getPatientNote_shouldReturnError_onFailure() throws Exception {
        Long patientId = 2L;

        when(patientServiceClient.getPatientById(patientId))
                .thenThrow(new RuntimeException("backend down"));

        mockMvc.perform(get("/note/{id}", patientId))
                .andExpect(status().isOk())
                .andExpect(view().name("patientNote"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", containsString("Erreur")));
    }

    /**
     * Teste l'ajout d'une nouvelle note avec succès.
     * Vérifie que :
     * - La note est créée correctement
     * - La redirection est effectuée vers la bonne URL
     * - Un message de succès est ajouté aux attributs flash
     *
     * @throws Exception en cas d'erreur lors de l'exécution du test
     */
    @Test
    @DisplayName("POST /note/{id} - succès: ajoute la note et redirige avec flash 'success'")
    void addNote_shouldRedirectWithSuccess_onValidData() throws Exception {
        int patientId = 3;

        PatientDto patient = new PatientDto();
        patient.setId(patientId);
        patient.setFirstname("Alice");

        when(patientServiceClient.getPatientById((long) patientId)).thenReturn(patient);
        when(urlConfiguration.getUrlSitePublic()).thenReturn("http://localhost");

        mockMvc.perform(post("/note/{id}", patientId)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        // Le contrôleur reconstruit id/patId/patient; on fournit seulement le contenu de la note
                        .param("note", "Contenu de note"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/note/" + patientId))
                .andExpect(flash().attribute("success", containsString("Note ajoutée avec succès")));
    }

    /**
     * Teste le comportement en cas d'erreur lors de l'ajout d'une note.
     * Vérifie que :
     * - La redirection est effectuée
     * - Un message d'erreur approprié est ajouté aux attributs flash
     *
     * @throws Exception en cas d'erreur lors de l'exécution du test
     */
    @Test
    @DisplayName("POST /note/{id} - erreur service: redirige avec flash 'noteError'")
    void addNote_shouldRedirectWithError_onServiceFailure() throws Exception {
        Long patientId = 4L;

        PatientDto patient = new PatientDto();
        patient.setId(Math.toIntExact(patientId));
        patient.setFirstname("Bob");

        when(patientServiceClient.getPatientById(patientId)).thenReturn(patient);
        when(urlConfiguration.getUrlSitePublic()).thenReturn("http://localhost");
        when(noteServiceClient.createNote(any(NoteDto.class)))
                .thenThrow(new RuntimeException("cannot create"));

        mockMvc.perform(post("/note/{id}", patientId)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("note", "Une note"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/note/" + patientId))
                .andExpect(flash().attribute("noteError", containsString("Erreur")));
    }
}
