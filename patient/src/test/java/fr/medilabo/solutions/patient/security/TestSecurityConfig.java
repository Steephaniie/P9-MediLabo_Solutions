package fr.medilabo.solutions.patient.security;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de sécurité pour les tests.
 * Désactive la protection CSRF et autorise toutes les requêtes pour faciliter les tests.
 */
@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    /**
     * Configure la chaîne de filtres de sécurité pour les tests.
     *
     * @param http La configuration de sécurité HTTP à modifier
     * @return La chaîne de filtres de sécurité configurée
     * @throws Exception Si une erreur survient lors de la configuration
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
