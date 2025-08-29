package fr.medilabo.solutions.note.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour le filtre d'authentification JWT.
 * Cette classe teste les différents scénarios de filtrage des requêtes
 * avec des jetons JWT valides et invalides.
 */
@SpringBootTest
class JwtAuthenticationFilterTest {

    @MockBean
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        // Assure un contexte propre avant chaque test
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        // Par sécurité, nettoie aussi après chaque test
        SecurityContextHolder.clearContext();
    }

    /**
     * Teste le traitement d'un jeton JWT valide.
     * Vérifie que l'authentification est correctement établie avec un JWT valide.
     */
    @Test
    void testDoFilterInternal_WithValidJwt() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil);

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain filterChain = Mockito.mock(FilterChain.class);

        String validJwt = "validJwtToken";
        String username = "testUser";

        Cookie jwtCookie = new Cookie("jwt", validJwt);
        when(request.getCookies()).thenReturn(new Cookie[]{jwtCookie});
        when(jwtUtil.extractUsername(validJwt)).thenReturn(username);
        when(jwtUtil.validateToken(validJwt)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtUtil).extractUsername(validJwt);
        verify(jwtUtil).validateToken(validJwt);
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Teste le traitement d'un jeton JWT invalide.
     * Vérifie que l'authentification est rejetée avec un JWT invalide.
     */
    @Test
    void testDoFilterInternal_WithInvalidJwt() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil);

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain filterChain = Mockito.mock(FilterChain.class);

        String invalidJwt = "invalidJwtToken";

        Cookie jwtCookie = new Cookie("jwt", invalidJwt);
        when(request.getCookies()).thenReturn(new Cookie[]{jwtCookie});
        when(jwtUtil.extractUsername(invalidJwt)).thenThrow(new RuntimeException("Invalid token"));
        when(jwtUtil.validateToken(invalidJwt)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtUtil).extractUsername(invalidJwt);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Teste le comportement lorsqu'aucun cookie JWT n'est présent.
     * Vérifie que la requête est traitée normalement sans authentification.
     */
    @Test
    void testDoFilterInternal_NoJwtCookie() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil);

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain filterChain = Mockito.mock(FilterChain.class);

        when(request.getCookies()).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Teste le traitement d'un jeton JWT expiré.
     * Vérifie que l'authentification est rejetée avec un JWT expiré.
     */
    @Test
    void testDoFilterInternal_WithExpiredJwt() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil);

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain filterChain = Mockito.mock(FilterChain.class);

        String expiredJwt = "expiredJwtToken";
        String username = "testUser";

        Cookie jwtCookie = new Cookie("jwt", expiredJwt);
        when(request.getCookies()).thenReturn(new Cookie[]{jwtCookie});
        when(jwtUtil.extractUsername(expiredJwt)).thenReturn(username);
        when(jwtUtil.validateToken(expiredJwt)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtUtil).extractUsername(expiredJwt);
        verify(jwtUtil).validateToken(expiredJwt);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}