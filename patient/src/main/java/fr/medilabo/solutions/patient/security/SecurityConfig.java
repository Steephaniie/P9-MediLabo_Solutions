package fr.medilabo.solutions.patient.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Classe de configuration de sécurité pour le framework Spring Security.
 * <p>
 * Cette classe configure les paramètres de sécurité pour l'application web
 * incluant :
 * - Intégration du filtre d'authentification JWT
 * - Règles d'autorisation des requêtes
 * - Gestion des exceptions pour les échecs d'authentification
 * - Fonctionnalité de déconnexion avec gestion des cookies JWT
 * - Encodage des mots de passe avec BCrypt
 * - Service de détails utilisateur en mémoire pour le développement/test
 * - Configuration du gestionnaire d'authentification
 * <p>
 * La configuration désactive la protection CSRF car les jetons JWT sont utilisés pour
 * l'authentification.
 * Les points d'accès publics comme login, logout, ressources CSS, endpoints actuator et
 * pages d'erreur sont accessibles sans authentification, tandis que toutes les autres
 * requêtes nécessitent une authentification.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private UnauthorizedAccessHandler unauthorizedAccessHandler;
    @Value("${app.gateway.url:http://localhost:8080}")
    private String gatewayUrl;


    /**
     * Configuration principale de la chaîne de filtres de sécurité.
     *
     * @param http l'objet HttpSecurity pour configurer la sécurité
     * @return SecurityFilterChain la chaîne de filtres configurée
     * @throws Exception en cas d'erreur de configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().authenticated())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl(gatewayUrl+"/front/login?logout") // Redirection après déconnexion
                        .deleteCookies("jwt"))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorizedAccessHandler));
        return http.build();
    }

    /**
     * Encodeur de mot de passe BCrypt.
     *
     * @return PasswordEncoder l'encodeur BCrypt configuré
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Service de détails utilisateur en mémoire pour les tests.
     * En production, vous devriez remplacer ceci par une implémentation
     * qui récupère les utilisateurs depuis une base de données.
     *
     * @return UserDetailsService le service de détails utilisateur
     */
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
                .username("stef")
                .password(passwordEncoder().encode("stef"))
                .roles("USER", "ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }

    /**
     * Gestionnaire d'authentification.
     *
     * @param authConfig la configuration d'authentification
     * @return AuthenticationManager le gestionnaire d'authentification
     * @throws Exception en cas d'erreur de configuration
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}