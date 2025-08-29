package fr.medilabo.solutions.front.controller;

import fr.medilabo.solutions.front.client.PatientServiceClient;
import fr.medilabo.solutions.front.config.UrlConfiguration;
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

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour le contrôleur PatientController.
 * Ces tests vérifient le comportement des endpoints REST et la logique de navigation
 * en utilisant MockMvc pour simuler les requêtes HTTP.
 */
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WebMvcTest(PatientController.class)
@DisplayName("PatientController - Tests Web MVC")
class PatientControllerTest {
    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientServiceClient patientServiceClient;

    @MockBean
    private UrlConfiguration urlConfiguration;

    /**
     * Teste l'affichage du formulaire de création d'un nouveau patient.
     * Vérifie que la vue 'patient' est retournée avec les bons attributs du modèle.
     */
    @Test
    @DisplayName("GET /patient/new - doit afficher la vue 'patient' en mode création")
    void showNewPatientForm_shouldReturnPatientView() throws Exception {
        mockMvc.perform(get("/patient/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("patient"))
                .andExpect(model().attributeExists("patient", "isEdit", "pageTitle"))
                .andExpect(model().attribute("isEdit", is(false)))
                .andExpect(model().attribute("pageTitle", is("Nouveau Patient")));
    }

    /**
     * Teste l'affichage du formulaire d'édition d'un patient existant.
     * Vérifie le comportement en cas de succès avec un patient trouvé.
     */
    @Test
    @DisplayName("GET /patient/{id}/edit - succès: vue 'patient' en mode édition")
    void showEditPatientForm_shouldReturnPatientView_onSuccess() throws Exception {
        // Préparation des données de test
        long patientId = 12L;
        PatientDto patient = new PatientDto();
        patient.setId((int) patientId);
        patient.setFirstname("Jean");
        patient.setLastname("Dupont");

        when(patientServiceClient.getPatientById(patientId)).thenReturn(patient);

        mockMvc.perform(get("/patient/{id}/edit", patientId))
                .andExpect(status().isOk())
                .andExpect(view().name("patient"))
                .andExpect(model().attributeExists("patient", "isEdit", "pageTitle"))
                .andExpect(model().attribute("isEdit", is(true)))
                .andExpect(model().attribute("pageTitle", is("Modifier Patient")))
                .andExpect(model().attribute("patient", hasProperty("firstname", is("Jean"))));
    }

    @Test
    @DisplayName("GET /patient/{id}/edit - erreur: redirige vers /home avec message d'erreur")
    void showEditPatientForm_shouldRedirectHome_onError() throws Exception {
        long patientId = 99L;
        when(patientServiceClient.getPatientById(patientId)).thenThrow(new RuntimeException("not found"));
        when(urlConfiguration.getUrlSitePublic()).thenReturn("http://localhost");

        mockMvc.perform(get("/patient/{id}/edit", patientId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/home"));
    }

    @Test
    @DisplayName("POST /patient/save - création: redirige vers /home avec flash 'success'")
    void savePatient_shouldCreateAndRedirect_onValidCreate() throws Exception {
        when(urlConfiguration.getUrlSitePublic()).thenReturn("http://localhost");
        PatientDto created = new PatientDto();
        created.setId(101);
        created.setFirstname("Alice");
        when(patientServiceClient.createPatient(any(PatientDto.class))).thenReturn(created);

        mockMvc.perform(post("/patient/save")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", "0")
                        .param("firstname", "Alice")
                        .param("lastname", "Martin")
                        .param("birthDate", LocalDate.of(1995, 5, 20).toString())
                        .param("gender", "F")
                        .param("address", "10 rue de Paris")
                        .param("phoneNumber", "0102030405"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/home"))
                .andExpect(flash().attribute("success", containsString("créé")));
    }

    @Test
    @DisplayName("POST /patient/save - mise à jour: redirige vers /home avec flash 'success'")
    void savePatient_shouldUpdateAndRedirect_onValidUpdate() throws Exception {
        when(urlConfiguration.getUrlSitePublic()).thenReturn("http://localhost");

        mockMvc.perform(post("/patient/save")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", "5")
                        .param("firstname", "Bob")
                        .param("lastname", "Updated")
                        .param("birthDate", LocalDate.of(1980, 1, 1).toString())
                        .param("gender", "M")
                        .param("address", "Adresse MAJ")
                        .param("phoneNumber", "0600000000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/home"))
                .andExpect(flash().attribute("success", containsString("mis à jour")));
    }

    /**
     * Teste la validation du formulaire de sauvegarde d'un patient.
     * Vérifie que les erreurs de validation sont correctement gérées et que l'utilisateur
     * reste sur le formulaire avec les messages d'erreur appropriés.
     */
    @Test
    @DisplayName("POST /patient/save - erreurs de validation: reste sur la vue 'patient' avec isEdit/pageTitle cohérents")
    void savePatient_shouldStayOnForm_onValidationErrors() throws Exception {
        // Configuration du test avec des données invalides
        // id=0 -> création; firstname vide pour déclencher la validation
        mockMvc.perform(post("/patient/save")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", "0")
                        .param("firstname", "")
                        .param("lastname", "Doe")
                        .param("birthDate", LocalDate.of(2030, 1, 1).toString()) // date future probable invalide
                        .param("gender", "X")
                        .param("address", "")
                        .param("phoneNumber", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("patient"))
                .andExpect(model().attribute("isEdit", is(false)))
                .andExpect(model().attribute("pageTitle", is("Nouveau Patient")))
                .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("POST /patient/save - erreur service: reste sur 'patient' avec message d'erreur")
    void savePatient_shouldStayOnFormWithError_onServiceFailure() throws Exception {
        when(patientServiceClient.createPatient(any(PatientDto.class)))
                .thenThrow(new RuntimeException("backend KO"));

        mockMvc.perform(post("/patient/save")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", "0")
                        .param("firstname", "Charles")
                        .param("lastname", "Durand")
                        .param("birthDate", LocalDate.of(1999, 12, 1).toString())
                        .param("gender", "M")
                        .param("address", "1 rue Test")
                        .param("phoneNumber", "0700000000"))
                .andExpect(status().isOk())
                .andExpect(view().name("patient"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", containsString("Erreur")));
    }
}
