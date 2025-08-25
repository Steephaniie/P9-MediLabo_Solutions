package fr.medilabo.solutions.note.security;

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
     * Tests for the validateToken method of JwtUtil.
     * This method checks whether a token is valid (not expired).
     */
    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        // Arrange
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
        // Act
        boolean isValid = jwtUtil.validateToken(token);
        // Assert
        assertTrue(isValid);
    }
//
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