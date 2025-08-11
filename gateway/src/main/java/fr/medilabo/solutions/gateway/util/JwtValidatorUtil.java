package fr.medilabo.solutions.gateway.util;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Utility component for validating JWT (JSON Web Token) signatures.
 * 
 * This component provides functionality to verify the integrity and
 * authenticity
 * of JWT tokens using HMAC-SHA algorithm with a secret key configured in the
 * application.
 * 
 * The secret key is injected from application properties using the key
 * "jwt.secret".
 */
@Component
public class JwtValidatorUtil {

    @Value("${jwt.secret}")
    private String secret;

    /**
     * @param token Complete JWT (header.payload.signature)
     * @return true if the token is not expired, false if expired or invalid
     */
    public boolean isValid(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extracts a specific claim from a JWT (JSON Web Token) as a String.
     *
     * @param jwt the JWT from which to extract the claim
     * @param string the name of the claim to extract
     * @return the value of the specified claim as a String, or null if the claim is not present
     * @throws io.jsonwebtoken.security.SecurityException if the JWT signature is invalid
     * @throws io.jsonwebtoken.JwtException if the JWT is invalid or cannot be parsed
     */
    public String extractClaim(String jwt, String string) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(jwt)
                .getPayload()
                .get(string, String.class);
    }
}
