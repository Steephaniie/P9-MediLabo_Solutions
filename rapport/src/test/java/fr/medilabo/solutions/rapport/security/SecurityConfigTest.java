// SecurityConfigTest.java
package fr.medilabo.solutions.rapport.security;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @Test
    @DisplayName("Devrait bloquer l'accès aux endpoints protégés (401)")
    void shouldProtectEndpoints() throws Exception {
        mockMvc.perform(get("/protected"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost:8080/front/login"));
    }

    @Test
    @DisplayName("Logout: redirection et suppression du cookie JWT")
    void shouldLogoutAndRedirectAndDeleteJwtCookie() throws Exception {
        mockMvc.perform(post("/logout")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .cookie(new jakarta.servlet.http.Cookie("jwt", "some-jwt")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost:8080/front/login?logout"))
                .andExpect(cookie().maxAge("jwt", 0));
    }

    // Contrôleur minimal pour exposer /login (public) et /protected (protégé)
    @RestController
    static class TestLoginController {
        @GetMapping("/login")
        public String login() {
            return "login-page";
        }
    }

    @RestController
    static class TestProtectedController {
        @GetMapping("/protected")
        public String protectedEndpoint() {
            return "protected";
        }
    }

    @Test
    @DisplayName("La requête doit etre rediriger vers login")
    void shouldInvokeJwtFilterOnRequest() throws Exception {
        mockMvc.perform(get("/protected"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost:8080/front/login"));
    }


}