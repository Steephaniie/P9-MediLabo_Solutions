package fr.medilabo.solutions.front.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.test.util.ReflectionTestUtils;

/**
 * Classe de tests pour JwtUtil.
 * Cette classe vérifie les fonctionnalités de validation des tokens JWT.
 */
@SpringBootTest
class JwtUtilTest {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private Long expiration;
//    @Mock
//    private JwtUtil jwtUtil;
    /**
     * Test pour la méthode validateToken de JwtUtil.
     * Cette méthode vérifie si un token valide (non expiré) est correctement validé.
     */
    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        // Préparation
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        String username = "testuser";
        Map<String, Object> claims = new HashMap<>();
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", expiration);
        // Exécution
        boolean isValid = jwtUtil.validateToken(token);
        // Vérification
        assertTrue(isValid);
    }
//

    /**
     * Test pour vérifier qu'un token expiré est correctement rejeté.
     */
    @Test
    void validateToken_shouldReturnFalseForExpiredToken() {
        // Arrange
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        String username = "testuser";
        Map<String, Object> claims = new HashMap<>();
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis() - expiration - 10000))
                .setExpiration(new Date(System.currentTimeMillis() - 10000))
                .signWith(key)
                .compact();
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", expiration);
        // Act
        boolean isValid = jwtUtil.validateToken(token);
        // Assert
        assertFalse(isValid);
    }
//

    /**
     * Test pour vérifier qu'un token mal formé est correctement rejeté.
     */
    @Test
    void validateToken_shouldReturnFalseForMalformedToken() {
        // Arrange
        String malformedToken = "thisIsNotAValidJwtToken";
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", expiration);
        // Act
        boolean isValid = jwtUtil.validateToken(malformedToken);
        // Assert
        assertFalse(isValid);
    }
}