package fr.medilabo.solutions.front.security;

import fr.medilabo.solutions.front.config.UrlConfiguration;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Point d'entrée d'authentification JWT qui gère les tentatives d'accès non autorisées.
 * <p>
 * Cette classe est appelée lorsqu'un utilisateur tente d'accéder à une ressource protégée
 * sans être authentifié ou avec un token JWT invalide.
 */
@Component
public class UnauthorizedAccessHandler implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(UnauthorizedAccessHandler.class);
    private final UrlConfiguration urlConfiguration;

    public UnauthorizedAccessHandler(UrlConfiguration urlConfiguration) {
        this.urlConfiguration = urlConfiguration;
    }

    /**
     * Méthode appelée lorsqu'une exception d'authentification est levée.
     *
     * @param request       la requête HTTP qui a causé l'exception
     *                      d'authentification
     * @param response      la réponse HTTP
     * @param authException l'exception d'authentification qui a été levée
     * @throws IOException      en cas d'erreur d'E/S
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        logger.warn("Unauthorized access to {} {} - {}", method, requestURI, authException.getMessage());
        response.sendRedirect(urlConfiguration.getUrlSitePublic()+"/login");

    }
}