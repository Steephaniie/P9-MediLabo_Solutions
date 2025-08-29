package fr.medilabo.solutions.rapport.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * Utilitaire pour la gestion des JWT (JSON Web Token).
 *
 * Cette classe fournit des méthodes pour :
 * - Générer des tokens JWT
 * - Valider des tokens JWT
 * - Extraire des informations des tokens (nom d'utilisateur, date d'expiration, etc.)
 * - Vérifier l'expiration des tokens
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Génère la clé secrète utilisée pour signer les tokens JWT.
     *
     * @return SecretKey la clé secrète générée à partir de la configuration
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Extrait le nom d'utilisateur du token JWT.
     *
     * @param token le token JWT
     * @return String le nom d'utilisateur contenu dans le token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrait la date d'expiration du token JWT.
     *
     * @param token le token JWT
     * @return Date la date d'expiration du token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrait une revendication spécifique du token JWT.
     *
     * @param <T>            le type de revendication à extraire
     * @param token          le token JWT
     * @param claimsResolver fonction pour résoudre la revendication
     * @return T la valeur de la revendication extraite
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrait toutes les revendications du token JWT.
     *
     * @param token le token JWT
     * @return Claims toutes les revendications contenues dans le token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Vérifie si le token JWT est expiré.
     *
     * @param token le token JWT à vérifier
     * @return boolean vrai si le token est expiré, faux sinon
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }


    /**
     * Valide un token JWT en vérifiant uniquement son format et son expiration.
     *
     * @param token le token JWT à valider
     * @return Boolean vrai si le token est valide, faux sinon
     */
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}