package fr.medilabo.solutions.front.controller;

import fr.medilabo.solutions.front.config.UrlConfiguration;
import fr.medilabo.solutions.front.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour le LoginController.
 * Cette classe teste les fonctionnalités d'authentification
 * et de validation des formulaires de connexion.
 */
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("Patient Controller Tests")
@WebMvcTest(LoginController.class)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UrlConfiguration urlConfiguration;

    /**
     * Teste l'authentification avec des identifiants valides.
     * Vérifie que l'utilisateur est redirigé vers la page d'accueil
     * avec un JWT valide dans les cookies.
     */
    @Test
    void testAuthenticateWithValidCredentials() throws Exception {

        // Simulation d'un utilisateur valide
        UserDetails validUser = new User("testuser", "password", Collections.emptyList());
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(validUser);

        // Simulation de la génération du JWT
        when(jwtUtil.generateToken(validUser)).thenReturn("mocked-jwt");

        // Simulation de l'URL de redirection
        when(urlConfiguration.getUrlSitePublic()).thenReturn("http://localhost");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "testuser")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/home"))
                .andExpect(cookie().value("jwt", "mocked-jwt"));
    }

    /**
     * Teste l'authentification avec des identifiants invalides.
     * Vérifie que l'utilisateur reste sur la page de connexion
     * avec un message d'erreur approprié.
     */
    @Test
    void testAuthenticateWithInvalidCredentials() throws Exception {

        // Simulation d'une authentification échouée
        doThrow(new BadCredentialsException("Invalid credentials"))
                .when(authenticationManager)
                .authenticate(Mockito.any());

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "wronguser")
                        .param("password", "wrongpassword"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", containsString("Nom d'utilisateur ou mot de passe incorrect")));
    }

    /**
     * Teste la validation du formulaire de connexion.
     * Vérifie que les erreurs de validation sont correctement
     * gérées lorsque les champs obligatoires sont vides.
     */
    @Test
    void testAuthenticateWithValidationErrors() throws Exception {

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "") // Nom d'utilisateur manquant
                        .param("password", "")) // Mot de passe manquant
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("loginRequest", "username", "password"));
    }
}