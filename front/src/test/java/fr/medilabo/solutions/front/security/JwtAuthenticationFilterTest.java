package fr.medilabo.solutions.front.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class JwtAuthenticationFilterTest {

    @MockBean
    private JwtUtil jwtUtil;

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