package fr.medilabo.solutions.front.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Point d'entrée d'authentification JWT qui gère les tentatives d'accès non autorisées.
 *
 * Cette classe est appelée lorsqu'un utilisateur tente d'accéder à une ressource protégée
 * sans être authentifié ou avec un token JWT invalide.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    /**
     * Détermine si la requête est une requête API.
     *
     * @param request la requête HTTP
     * @return vrai si c'est une requête API, faux sinon
     */
    private boolean isApiRequest(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return requestURI.startsWith("/api/");
    }

    /**
     * Méthode appelée lorsqu'une exception d'authentification est levée.
     *
     * @param request       la requête HTTP qui a causé l'exception
     *                      d'authentification
     * @param response      la réponse HTTP
     * @param authException l'exception d'authentification qui a été levée
     * @throws IOException      en cas d'erreur d'E/S
     * @throws ServletException en cas d'erreur de servlet
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        logger.warn("Unauthorized access to {} {} - {}", method, requestURI, authException.getMessage());

        // For API requests, return JSON
        if (isApiRequest(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            response.sendRedirect("/login");
        }
    }
}