package fr.medilabo.solutions.gateway.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import fr.medilabo.solutions.gateway.util.JwtValidatorUtil;

import reactor.core.publisher.Mono;

/**
 * WebFilter réactif qui intercepte les requêtes HTTP entrantes pour valider les
 * jetons JWT.
 *
 * <p>
 * Ce filtre effectue les tâches suivantes :
 * <ul>
 * <li>Extrait le jeton JWT du cookie "jwt"</li>
 * <li>Valide le jeton en utilisant {@link JwtValidatorUtil}</li>
 * <li>Vérifie que la requête provient du frontend en utilisant un en-tête
 * personnalisé</li>
 * <li>Si valide, définit le contexte de sécurité avec un {@code ROLE_USER}</li>
 * <li>Si invalide, redirige l'utilisateur vers la page de connexion</li>
 * </ul>
 * </p>
 *
 * <p>
 * Ce filtre est généralement ajouté avant l'étape d'authentification dans la
 * chaîne de filtres Spring Security.
 * </p>
 */
@Component
public class JwtValidationFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtValidationFilter.class);

    private static final String INTERNAL_HEADER = "X-Internal-Front";

    @Autowired
    private JwtValidatorUtil jwtValidator;

    /**
     * Logique principale du filtre qui traite les requêtes entrantes.
     *
     * @param exchange le {@link ServerWebExchange} courant
     * @param chain    la {@link WebFilterChain} pour déléguer au filtre suivant
     * @return un {@link Mono} qui se termine lorsque la chaîne de filtres a été
     *         traitée
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        ServerHttpRequest req = exchange.getRequest();
        String path = req.getURI().getPath();

        String jwt = extractJwtFromCookies(req);
        if (jwt != null) {

            if (jwtValidator.isValid(jwt) && isFromFront(req)) {
                log.info("Valid JWT signature for request: {}", path);

                String username = jwtValidator.extractClaim(jwt, "sub");

                var auth = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));

                var ctx = new SecurityContextImpl(auth);

                return chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(ctx)));
            }

            log.warn("Invalid JWT signature for request: {}", path);
            return redirect(exchange, "http://localhost:8084/login");
        }

        log.warn("No JWT token found in cookies for request: {}", path);
        return chain.filter(exchange);
    }

    /**
     * Extrait le jeton JWT des cookies.
     *
     * @param req la {@link ServerHttpRequest} courante
     * @return la chaîne du jeton JWT si présente, ou {@code null} si non trouvée
     */
    private String extractJwtFromCookies(ServerHttpRequest req) {
        return req.getCookies().getFirst("jwt") != null
                ? req.getCookies().getFirst("jwt").getValue()
                : null;
    }

    /**
     * Vérifie si la requête provient du frontend.
     *
     * @param req la {@link ServerHttpRequest} courante
     * @return {@code true} si l'en-tête interne est présent, {@code false}
     *         sinon
     */
    private boolean isFromFront(ServerHttpRequest req) {
        return req.getHeaders().containsKey(INTERNAL_HEADER);
    }

    /**
     * Redirige l'utilisateur vers une URL spécifiée en utilisant HTTP 303 SEE_OTHER.
     *
     * @param exchange le {@link ServerWebExchange} courant
     * @param url      l'URL cible vers laquelle rediriger
     * @return un {@link Mono} qui se termine lorsque la réponse est terminée
     */
    private Mono<Void> redirect(ServerWebExchange exchange, String url) {
        exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
        exchange.getResponse().getHeaders().set(HttpHeaders.LOCATION, url);
        return exchange.getResponse().setComplete();
    }
}
