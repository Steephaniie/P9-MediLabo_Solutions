package fr.medilabo.solutions.rapport.security;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de sécurité pour les tests.
 * Cette classe désactive la sécurité CSRF et autorise toutes les requêtes
 * pour faciliter les tests d'intégration.
 */

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    /**
     * Configure la chaîne de filtres de sécurité pour les tests.
     * Désactive CSRF et autorise toutes les requêtes entrantes.
     *
     * @param http la configuration de sécurité HTTP
     * @return la chaîne de filtres configurée
     * @throws Exception si une erreur survient lors de la configuration
     */
    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .build();
    }
}
