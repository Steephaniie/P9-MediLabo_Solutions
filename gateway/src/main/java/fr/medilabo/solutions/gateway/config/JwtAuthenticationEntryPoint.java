package fr.medilabo.solutions.gateway.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Point d'entrée personnalisé utilisé par Spring Security pour gérer les accès non autorisés
 * (HTTP 401).
 *
 * <p>
 * Ce composant fait la distinction entre les requêtes frontend et API :
 * <ul>
 * <li>Pour les requêtes API (URLs contenant "/api"), il redirige l'utilisateur vers la
 * page d'accueil.</li>
 * <li>Pour les requêtes non-API, il renvoie directement une réponse 401 Non autorisé.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Cette logique permet d'intégrer la redirection frontend avec un backend sans état
 * basé sur JWT.
 * </p>
 */
@Component
public class JwtAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    /**
     * Détermine si la requête cible un point d'accès API.
     *
     * @param req la {@link ServerHttpRequest} à inspecter
     * @return {@code true} si le chemin de la requête contient "/api", {@code false}
     *         sinon
     */
    private boolean isApiRequest(ServerHttpRequest req) {
        return req.getPath().value().contains("/api");
    }

    /**
     * Gère les tentatives d'accès non autorisées.
     *
     * <p>
     * Si la requête concerne une route API, redirige vers la page d'accueil.
     * Sinon, répond avec HTTP 401 Non autorisé.
     * </p>
     *
     * @param exchange      le {@link ServerWebExchange} représentant la
     *                      requête/réponse courante
     * @param authException l'exception qui a déclenché le point d'entrée
     * @return un {@link Mono} qui se termine lorsque la réponse est envoyée
     */
    @Override
    public Mono<Void> commence(ServerWebExchange exchange,
            AuthenticationException authException) {

        ServerHttpRequest req = exchange.getRequest();
        String path = req.getURI().getPath();
        HttpMethod method = req.getMethod();

        log.warn("Unauthorized access to {} {} - {}", method, path, authException.getMessage());

        if (!isApiRequest(req)) {
            log.warn("Unauthorized access to non-API endpoint: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        log.info("Redirecting to home page for unauthorized access from front: {}", path);
        return redirect(exchange, "http://localhost:8084/home");
    }

    /**
     * Envoie une redirection HTTP (303 See Other) vers l'URL spécifiée.
     *
     * @param exchange le {@link ServerWebExchange}
     * @param url      la cible de redirection
     * @return un {@link Mono} indiquant la fin de la réponse
     */
    private Mono<Void> redirect(ServerWebExchange exchange, String url) {
        exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
        exchange.getResponse().getHeaders().set(HttpHeaders.LOCATION, url);
        return exchange.getResponse().setComplete();
    }
}
