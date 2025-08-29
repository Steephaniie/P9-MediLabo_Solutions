// SecurityConfigTest.java
package fr.medilabo.solutions.front.security;

import fr.medilabo.solutions.front.config.UrlConfiguration;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour la configuration de sécurité de l'application.
 * Cette classe vérifie le bon fonctionnement des mécanismes d'authentification,
 * d'autorisation et de gestion des sessions JWT.
 */
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WebMvcTest(controllers = {
        SecurityConfigTest.TestLoginController.class,
        SecurityConfigTest.TestProtectedController.class
})
@Import({SecurityConfig.class, SecurityConfigTest.TestBeans.class})
@DisplayName("SecurityConfig Tests")
class SecurityConfigTest {
    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Vérifie que l'accès aux endpoints protégés est bien bloqué
     * et redirige vers la page de login pour les utilisateurs non authentifiés.
     */
    @Test
    @DisplayName("Devrait bloquer l'accès aux endpoints protégés (401)")
    void shouldProtectEndpoints() throws Exception {
        mockMvc.perform(get("/protected"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    /**
     * Vérifie que le processus de déconnexion :
     * - Redirige correctement vers la page de login
     * - Supprime le cookie JWT
     * - Invalide la session
     */
    @Test
    @DisplayName("Logout: redirection et suppression du cookie JWT")
    void shouldLogoutAndRedirectAndDeleteJwtCookie() throws Exception {
        mockMvc.perform(post("/logout")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .cookie(new jakarta.servlet.http.Cookie("jwt", "some-jwt")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login?logout"))
                .andExpect(cookie().maxAge("jwt", 0));
    }

    /**
     * Contrôleur de test simulant les endpoints d'authentification.
     * Expose l'endpoint public /login pour les tests.
     */
    @RestController
    static class TestLoginController {
        @GetMapping("/login")
        public String login() {
            return "login-page";
        }
    }

    /**
     * Contrôleur de test simulant un endpoint protégé.
     * Utilisé pour vérifier les mécanismes de sécurité.
     */
    @RestController
    static class TestProtectedController {
        @GetMapping("/protected")
        public String protectedEndpoint() {
            return "protected";
        }
    }

    /**
     * Vérifie que toute requête non authentifiée vers un endpoint protégé
     * est bien interceptée et redirigée vers la page de login.
     */
    @Test
    @DisplayName("La requête doit etre rediriger vers login")
    void shouldInvokeJwtFilterOnRequest() throws Exception {
        mockMvc.perform(get("/protected"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    /**
     * Configuration de test fournissant les beans nécessaires
     * pour simuler l'environnement de sécurité.
     */
    @TestConfiguration
    static class TestBeans {

        // Fournit l'URL publique utilisée par SecurityConfig pour la redirection de logout
        @Bean
        UrlConfiguration urlConfiguration() {
            return new UrlConfiguration() {
                @Override
                public String getUrlSitePublic() {
                    return "http://localhost";
                }
            };
        }

        // Entry point réel, en lui passant l'UrlConfiguration requise par son constructeur
        @Bean
        UnauthorizedAccessHandler unauthorizedAccessHandler(UrlConfiguration urlConfiguration) {
            return new UnauthorizedAccessHandler(urlConfiguration);
        }
    }
}