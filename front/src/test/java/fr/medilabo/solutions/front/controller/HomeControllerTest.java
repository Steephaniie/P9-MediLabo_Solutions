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
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour le HomeController utilisant Spring MVC Test.
 * <p>
 * Cette classe teste les fonctionnalités du contrôleur principal de l'application,
 * notamment la récupération et l'affichage des patients sur la page d'accueil.
 * <p>
 * Configuration:
 * - Désactivation des filtres de sécurité pour les tests
 * - Profil de test actif
 * - Test limité au HomeController
 */
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WebMvcTest(HomeController.class)
@DisplayName("HomeController - Tests Web MVC")
class HomeControllerTest {
    // Utilitaire de gestion des JWT mocké
    @MockBean
    private JwtUtil jwtUtil;

    // Configuration des URLs de l'application mockée
    @MockBean
    private UrlConfiguration urlConfiguration;

    // Client Mock MVC pour simuler les requêtes HTTP
    @Autowired
    private MockMvc mockMvc;

    // Client de service patient mocké
    @MockBean
    private PatientServiceClient patientServiceClient;

    // Gestionnaire de cache mocké
    @MockBean
    private CacheManager cacheManager;

    /**
     * Teste l'affichage de la page d'accueil avec la liste des patients.
     * <p>
     * Ce test vérifie que :
     * - La requête GET vers /home retourne un statut 200 (OK)
     * - La vue "home" est bien retournée
     * - Le modèle contient l'attribut "patients"
     * - La liste des patients contient exactement 2 éléments
     *
     * @throws Exception en cas d'erreur lors de l'exécution du test
     */
    @Test
    @DisplayName("GET /home - doit retourner la vue 'home' avec la liste des patients")
    void shouldReturnHomeViewWithPatients() throws Exception {
        // On renvoie 2 patients factices (contenu non vérifié ici)
        when(patientServiceClient.getAllPatients()).thenReturn(List.of(new PatientDto(), new PatientDto()));

        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("patients"))
                .andExpect(model().attribute("patients", hasSize(2)));
    }

    /**
     * Teste le comportement de la page d'accueil en cas d'erreur.
     * <p>
     * Ce test vérifie que :
     * - En cas d'erreur du service patient
     * - La vue "home" est toujours retournée
     * - Un message d'erreur est ajouté au modèle
     * - Le message contient bien le mot "Erreur"
     *
     * @throws Exception en cas d'erreur lors de l'exécution du test
     */
    @Test
    @DisplayName("GET /home - en cas d'erreur, doit retourner la vue 'home' avec le message d'erreur")
    void shouldReturnHomeViewWithErrorWhenClientFails() throws Exception {
        when(patientServiceClient.getAllPatients()).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", containsString("Erreur")));
    }
}
