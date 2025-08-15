package fr.medilabo.solutions.front.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * {@link RequestInterceptor} Feign qui transfère le cookie JWT de la requête
 * HTTP entrante vers les requêtes client Feign sortantes.
 *
 * <p>
 * Il accède à la requête HTTP actuelle en utilisant le
 * {@link RequestContextHolder} de Spring, extrait le cookie "jwt" et
 * l'attache à l'en-tête "Cookie" de la requête Feign.
 * </p>
 *
 */
@Component
@Slf4j
public class JwtCookieFeignInterceptor implements RequestInterceptor {

    private static final String COOKIE_NAME = "jwt";

    /**
     * Intercepte et modifie la requête Feign sortante en ajoutant le cookie JWT
     *
     * @param template le {@link RequestTemplate} utilisé pour construire la requête Feign
     */
    @Override
    public void apply(RequestTemplate template) {

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null)
            return;
        // on recupère la requete HTTP actuelle
        HttpServletRequest request = attrs.getRequest();
        // on identifie le cookie jwt
        for (Cookie c : request.getCookies()) {
            if (COOKIE_NAME.equals(c.getName())) {
                // on transfert le cookie jwt dans la requete Feign
                String newCookie = COOKIE_NAME + '=' + c.getValue();
                template.header("Cookie", newCookie);
                break;
            }
        }
    }
}
