package fr.medilabo.solutions.note.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour la classe UnauthorizedAccessHandler qui gère les accès non autorisés.
 */
class UnauthorizedAccessHandlerTest {

    /**
     * Teste si la méthode commence redirige correctement vers la page de connexion
     * lorsqu'un accès non autorisé est détecté.
     *
     * @throws IOException si une erreur d'E/S survient
     */
    @Test
    void testCommence_RedirectsToLogin() throws IOException {

        UnauthorizedAccessHandler unauthorizedAccessHandler = new UnauthorizedAccessHandler();
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        AuthenticationException exception = Mockito.mock(AuthenticationException.class);

        when(request.getRequestURI()).thenReturn("/protected/resource");
        when(request.getMethod()).thenReturn("GET");
        when(exception.getMessage()).thenReturn("Access Denied");

        // Exécution
        unauthorizedAccessHandler.commence(request, response, exception);

        // Vérification
        verify(response, times(1)).sendRedirect("null/front/login");
        verify(request, times(1)).getRequestURI();
        verify(request, times(1)).getMethod();
        verify(exception, times(1)).getMessage();
    }
}