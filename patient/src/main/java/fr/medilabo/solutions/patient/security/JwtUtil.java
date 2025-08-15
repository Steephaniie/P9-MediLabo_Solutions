package fr.medilabo.solutions.patient.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility for JWT (JSON Web Token) management.
 * 
 * This class provides methods to:
 * - Generate JWT tokens
 * - Validate JWT tokens
 * - Extract information from tokens (username, expiration date, etc.)
 * - Check token expiration
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Generates the secret key used to sign JWT tokens.
     * 
     * @return SecretKey the secret key generated from configuration
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Extracts the username from the JWT token.
     * 
     * @param token the JWT token
     * @return String the username contained in the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from the JWT token.
     * 
     * @param token the JWT token
     * @return Date the expiration date of the token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from the JWT token.
     * 
     * @param <T>            the type of claim to extract
     * @param token          the JWT token
     * @param claimsResolver function to resolve the claim
     * @return T the value of the extracted claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from the JWT token.
     *
     * @param token the JWT token
     * @return Claims all claims contained in the token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Checks if the JWT token is expired.
     * 
     * @param token the JWT token to verify
     * @return boolean true if the token is expired, false otherwise
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }


    /**
     * Validates a JWT token by checking only its format and expiration.
     * 
     * @param token the JWT token to validate
     * @return Boolean true if the token is valid, false otherwise
     */
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}