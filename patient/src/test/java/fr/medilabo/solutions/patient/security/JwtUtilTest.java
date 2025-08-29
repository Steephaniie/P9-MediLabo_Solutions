package fr.medilabo.solutions.patient.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class JwtUtilTest {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private Long expiration;
//    @Mock
//    private JwtUtil jwtUtil;
    /**
     * Tests pour la méthode validateToken de JwtUtil.
     * Cette méthode vérifie si un token est valide (non expiré).
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
    @Test
    void validateToken_shouldReturnFalseForExpiredToken() {
        // Préparation
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
        // Exécution
        boolean isValid = jwtUtil.validateToken(token);
        // Vérification
        assertFalse(isValid);
    }
//
    @Test
    void validateToken_shouldReturnFalseForMalformedToken() {
        // Préparation
        String malformedToken = "thisIsNotAValidJwtToken";
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", expiration);
        // Exécution
        boolean isValid = jwtUtil.validateToken(malformedToken);
        // Vérification
        assertFalse(isValid);
    }
}