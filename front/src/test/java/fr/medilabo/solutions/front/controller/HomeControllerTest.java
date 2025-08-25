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

@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WebMvcTest(HomeController.class)
@DisplayName("HomeController - Tests Web MVC")
class HomeControllerTest {
    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UrlConfiguration urlConfiguration;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientServiceClient patientServiceClient;

    @MockBean
    private CacheManager cacheManager;

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
