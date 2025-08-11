package fr.medilabo.solutions.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;

/**
 * Classe de configuration pour sécuriser l'application Spring Cloud Gateway
 * réactive.
 *
 * <p>
 * Cette classe définit la chaîne de filtres de sécurité utilisée pour :
 * <ul>
 * <li>Désactiver la protection CSRF et les mécanismes d'authentification
 * basic/form</li>
 * <li>Restreindre l'accès aux points d'accès "/api/**" aux utilisateurs
 * authentifiés</li>
 * <li>Permettre l'accès sans restriction aux points d'accès actuator
 * ("/actuator/**")</li>
 * <li>Appliquer un filtre personnalisé de validation JWT pour
 * l'authentification</li>
 * <li>Gérer élégamment les requêtes non autorisées et les accès refusés</li>
 * </ul>
 * </p>
 */
@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Autowired
    private ServerAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtValidationFilter jwtValidationFilter;

    /**
     * Définit la chaîne de filtres de sécurité réactive pour l'application.
     *
     * @param http le {@link ServerHttpSecurity} à configurer
     * @return la {@link SecurityWebFilterChain} configurée
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        ServerAccessDeniedHandler accessDeniedRedirect = (ServerAccessDeniedHandler) (exchange, denied) -> {
            exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
            exchange.getResponse().getHeaders()
                    .set(HttpHeaders.LOCATION, "http://localhost:8084/home");
            return exchange.getResponse().setComplete();
        };

        return http
                .csrf(csrf -> csrf.disable())
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/api/**").authenticated())
                .exceptionHandling(exceptionHandlingSpec -> exceptionHandlingSpec
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedRedirect))
                .addFilterAt(jwtValidationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
