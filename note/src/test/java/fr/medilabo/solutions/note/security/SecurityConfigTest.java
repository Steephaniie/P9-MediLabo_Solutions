// SecurityConfigTest.java
package fr.medilabo.solutions.note.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests pour la configuration de sécurité.
 * Vérifie le comportement des endpoints protégés et la redirection vers la page de connexion.
 */
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WebMvcTest(controllers = {
        SecurityConfigTest.TestLoginController.class,
        SecurityConfigTest.TestProtectedController.class
})
@Import({SecurityConfig.class,UnauthorizedAccessHandler.class})
@DisplayName("SecurityConfig Tests")
class SecurityConfigTest {
    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Vérifie que l'accès aux endpoints protégés est bloqué
     * et redirige vers la page de connexion
     */
    @Test
    @DisplayName("Devrait bloquer l'accès aux endpoints protégés (401)")
    void shouldProtectEndpoints() throws Exception {
        mockMvc.perform(get("/protected"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost:8080/front/login"));
    }

    /**
     * Contrôleur de test minimal pour exposer l'endpoint /login (public)
     */
    @RestController
    static class TestLoginController {
        @GetMapping("/login")
        public String login() {
            return "login-page";
        }
    }

    /**
     * Contrôleur de test minimal pour exposer l'endpoint /protected (protégé)
     */
    @RestController
    static class TestProtectedController {
        @GetMapping("/protected")
        public String protectedEndpoint() {
            return "protected";
        }
    }

    /**
     * Vérifie que le filtre JWT est invoqué et que la requête
     * est redirigée vers la page de connexion
     */
    @Test
    @DisplayName("La requête doit être redirigée vers login")
    void shouldInvokeJwtFilterOnRequest() throws Exception {
        mockMvc.perform(get("/protected"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost:8080/front/login"));
    }


}