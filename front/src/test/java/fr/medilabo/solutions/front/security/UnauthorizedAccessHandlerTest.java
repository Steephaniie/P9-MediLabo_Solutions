package fr.medilabo.solutions.front.security;

import fr.medilabo.solutions.front.config.UrlConfiguration;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

import static org.mockito.Mockito.*;

class UnauthorizedAccessHandlerTest {

    @Test
    void testCommence_RedirectsToLogin() throws IOException {
        // Arrange
        UrlConfiguration urlConfiguration = Mockito.mock(UrlConfiguration.class);
        when(urlConfiguration.getUrlSitePublic()).thenReturn("https://example.com");

        UnauthorizedAccessHandler unauthorizedAccessHandler = new UnauthorizedAccessHandler(urlConfiguration);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        AuthenticationException exception = Mockito.mock(AuthenticationException.class);

        when(request.getRequestURI()).thenReturn("/protected/resource");
        when(request.getMethod()).thenReturn("GET");
        when(exception.getMessage()).thenReturn("Access Denied");

        // Act
        unauthorizedAccessHandler.commence(request, response, exception);

        // Assert
        verify(response, times(1)).sendRedirect("https://example.com/login");
        verify(request, times(1)).getRequestURI();
        verify(request, times(1)).getMethod();
        verify(exception, times(1)).getMessage();
    }
}