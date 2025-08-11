package fr.medilabo.solutions.front.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;

/**
 * Classe de configuration pour personnaliser les requêtes du client Feign dans le service
 * frontend.
 *
 * <p>
 * Cette configuration ajoute un en-tête HTTP personnalisé {@code X-Internal-Front: true}
 * à chaque requête Feign sortante. Cela permet aux services backend (comme la passerelle
 * API) d'identifier et de valider que la requête provient d'un composant frontend de
 * confiance.
 * </p>
 *
 * <p>
 * Ceci est généralement utilisé pour autoriser les jetons JWT uniquement lorsque le
 * frontend est la source de l'appel.
 * </p>
 */
@Configuration
public class FrontFeignConfig {

    /**
     * Définit un {@link RequestInterceptor} qui ajoute l'en-tête "X-Internal-Front"
     * à toutes les requêtes Feign.
     *
     * @return le {@link RequestInterceptor} configuré
     */
    @Bean
    public RequestInterceptor frontInternalHeader() {
        return template -> template.header("X-Internal-Front", "true");
    }
}
