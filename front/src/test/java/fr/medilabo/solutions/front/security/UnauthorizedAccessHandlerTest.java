package fr.medilabo.solutions.front.security;

import fr.medilabo.solutions.front.config.UrlConfiguration;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 * Classe de test pour UnauthorizedAccessHandler qui gère les accès non autorisés
 * dans l'application. Ces tests vérifient le comportement de redirection lorsqu'un
 * utilisateur tente d'accéder à une ressource protégée sans autorisation.
 */
class UnauthorizedAccessHandlerTest {

    /**
     * Teste la redirection vers la page de connexion lors d'un accès non autorisé.
     * Ce test vérifie que le gestionnaire redirige correctement l'utilisateur vers
     * la page de connexion lorsqu'il tente d'accéder à une ressource protégée.
     *
     * @throws IOException si une erreur survient lors de la redirection
     */
    @Test
    void testCommence_RedirectsToLogin() throws IOException {
        // Préparation des mocks et configuration
        UrlConfiguration urlConfiguration = Mockito.mock(UrlConfiguration.class);
        when(urlConfiguration.getUrlSitePublic()).thenReturn("https://example.com");

        UnauthorizedAccessHandler unauthorizedAccessHandler = new UnauthorizedAccessHandler(urlConfiguration);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        AuthenticationException exception = Mockito.mock(AuthenticationException.class);

        // Configuration des comportements attendus des mocks
        when(request.getRequestURI()).thenReturn("/protected/resource");
        when(request.getMethod()).thenReturn("GET");
        when(exception.getMessage()).thenReturn("Access Denied");

        // Exécution de la méthode testée
        unauthorizedAccessHandler.commence(request, response, exception);

        // Vérification des interactions attendues
        verify(response, times(1)).sendRedirect("https://example.com/login");
        verify(request, times(1)).getRequestURI();
        verify(request, times(1)).getMethod();
        verify(exception, times(1)).getMessage();
    }
}