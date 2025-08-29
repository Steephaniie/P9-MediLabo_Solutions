package fr.medilabo.solutions.rapport.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Filtre d'authentification JWT qui extrait et valide les jetons JWT des cookies.
 * Ce filtre s'exécute une fois par requête et gère l'authentification basée sur les jetons JWT.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String JWT_COOKIE_NAME = "jwt";

    private final JwtUtil jwtUtil;

    /**
     * Traite la requête entrante pour extraire et valider le jeton JWT des cookies.
     * Configure le contexte d'authentification si le jeton est valide.
     *
     * @param request la requête HTTP servlet
     * @param response la réponse HTTP servlet
     * @param filterChain la chaîne de filtres pour continuer le traitement
     * @throws ServletException si une erreur de servlet survient
     * @throws IOException si une erreur d'E/S survient
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        final String requestURI = request.getRequestURI();

        String jwt = extractJwtFromCookies(request);
        String username = null;

        if (jwt != null) {
            try {
                username = jwtUtil.extractUsername(jwt);
                logger.debug("JWT trouvé dans le cookie pour l'utilisateur `{}`", username);
            } catch (Exception e) {
                logger.warn("Impossible d'extraire le nom d'utilisateur du JWT (cookie): {}", e.getMessage());
            }
        } else {
            logger.debug("Aucun cookie '{}' trouvé pour la requête {}", JWT_COOKIE_NAME, requestURI);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            if (jwtUtil.validateToken(jwt)) {
                logger.info("JWT valide pour `{}`", username);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                logger.debug("Authentification configurée pour `{}`", username);
            } else {
                logger.warn("JWT invalide ou expiré pour `{}`", username);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrait le jeton JWT des cookies de la requête.
     *
     * @param request la requête HTTP servlet
     * @return le jeton JWT s'il est trouvé, null sinon
     */
    private String extractJwtFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null)
            return null;

        Optional<Cookie> jwtCookie = java.util.Arrays.stream(cookies)
                .filter(c -> JWT_COOKIE_NAME.equals(c.getName()))
                .findFirst();
        return jwtCookie.map(Cookie::getValue).orElse(null);
    }

}
