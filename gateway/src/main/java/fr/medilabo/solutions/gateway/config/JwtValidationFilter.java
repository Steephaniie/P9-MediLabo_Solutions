package fr.medilabo.solutions.gateway.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.gateway.url:http://localhost:8080}")
    private String gatewayUrl;

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

        // Exclure les routes d'authentification de la validation JWT
        if (isAuthenticationPath(path)) {
            log.debug("Skipping JWT validation for authentication path: {}", path);
            return chain.filter(exchange);
        }

        String jwt = extractJwtFromCookies(req);
        if (jwt != null) {

            if (jwtValidator.isValid(jwt) ) {
                String username = jwtValidator.extractClaim(jwt, "sub");
                log.info("JWT valide pour `{}`", username);
                var auth = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));

                var ctx = new SecurityContextImpl(auth);

                return chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(ctx)));
            }

            log.warn("Invalid JWT signature for request: {}", path);
            return redirect(exchange, gatewayUrl+"/front/login");
        }

        log.warn("No JWT token found in cookies for request: {}", path);
        return chain.filter(exchange);
    }

    /**
     * Vérifie si le chemin correspond à une route d'authentification.
     *
     * @param path le chemin de la requête
     * @return true si c'est une route d'authentification
     */
    private boolean isAuthenticationPath(String path) {
        return path.startsWith("/login") || 
           path.startsWith("/auth/") || 
           path.startsWith("/actuator/");
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
