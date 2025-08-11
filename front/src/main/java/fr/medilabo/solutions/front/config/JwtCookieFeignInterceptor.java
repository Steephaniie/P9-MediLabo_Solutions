package fr.medilabo.solutions.front.config;

import java.util.Collections;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link RequestInterceptor} Feign qui transfère le cookie JWT de la requête
 * HTTP entrante vers les requêtes client Feign sortantes.
 *
 * <p>
 * Cet intercepteur est utilisé pour propager le cookie "jwt" (contenant
 * généralement un jeton JWT) afin que les services en aval derrière l'API Gateway
 * puissent effectuer l'authentification ou l'autorisation sans nécessiter
 * de ré-authentification.
 * </p>
 *
 * <p>
 * Il accède à la requête HTTP actuelle en utilisant le
 * {@link RequestContextHolder} de Spring, extrait le cookie "jwt" et
 * l'attache à l'en-tête "Cookie" de la requête Feign.
 * </p>
 *
 * <p>
 * Si l'en-tête "Cookie" existe déjà, le JWT est ajouté en utilisant
 * un point-virgule comme séparateur.
 * </p>
 */
@Component
@Slf4j
public class JwtCookieFeignInterceptor implements RequestInterceptor {

    private static final String COOKIE_NAME = "jwt";

    /**
     * Intercepte et modifie la requête Feign sortante en ajoutant le cookie JWT,
     * s'il est présent dans la requête HTTP actuelle.
     *
     * @param template le {@link RequestTemplate} utilisé pour construire la requête Feign
     */
    @Override
    public void apply(RequestTemplate template) {

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs == null)
            return;

        HttpServletRequest request = attrs.getRequest();
        if (request == null || request.getCookies() == null)
            return;

        for (Cookie c : request.getCookies()) {
            if (COOKIE_NAME.equals(c.getName())) {

                String newCookie = COOKIE_NAME + '=' + c.getValue();

                if (template.headers().containsKey("Cookie")) {
                    String existing = template.headers()
                            .getOrDefault("Cookie", Collections.emptyList())
                            .stream().findFirst().orElse("");
                    template.header("Cookie", existing + "; " + newCookie);
                } else {
                    template.header("Cookie", newCookie);
                }
                break;
            }
        }
    }
}
