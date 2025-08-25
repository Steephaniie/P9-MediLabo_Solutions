package fr.medilabo.solutions.rapport.config;

import feign.RequestTemplate;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtCookieFeignInterceptorTest {

    /**
     * Tests the JwtCookieFeignInterceptor class which is responsible for adding the JWT cookie to
     * the header of outgoing Feign client requests.
     */
    @Test
    void testApply_whenRequestContainsJwtCookie_shouldAddCookieToHeader() {
        // Arrange
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Cookie jwtCookie = new Cookie("jwt", "exampleToken");
        when(mockRequest.getCookies()).thenReturn(new Cookie[]{jwtCookie});

        ServletRequestAttributes mockAttributes = new ServletRequestAttributes(mockRequest);
        RequestContextHolder.setRequestAttributes(mockAttributes);

        RequestTemplate requestTemplate = new RequestTemplate();
        JwtCookieFeignInterceptor interceptor = new JwtCookieFeignInterceptor();

        // Act
        interceptor.apply(requestTemplate);

        // Assert
        String expectedHeaderValue = "jwt=exampleToken";
        assert requestTemplate.headers().containsKey("Cookie");
        assert requestTemplate.headers().get("Cookie").contains(expectedHeaderValue);
    }

    @Test
    void testApply_whenRequestDoesNotContainJwtCookie_shouldNotAddHeader() {
        // Arrange
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Cookie otherCookie = new Cookie("other", "otherValue");
        when(mockRequest.getCookies()).thenReturn(new Cookie[]{otherCookie});

        ServletRequestAttributes mockAttributes = new ServletRequestAttributes(mockRequest);
        RequestContextHolder.setRequestAttributes(mockAttributes);

        RequestTemplate requestTemplate = new RequestTemplate();
        JwtCookieFeignInterceptor interceptor = new JwtCookieFeignInterceptor();

        // Act
        interceptor.apply(requestTemplate);

        // Assert
        assert !requestTemplate.headers().containsKey("Cookie");
    }

    @Test
    void testApply_whenServletRequestAttributesIsNull_shouldDoNothing() {
        // Arrange
        RequestContextHolder.setRequestAttributes(null);

        RequestTemplate requestTemplate = new RequestTemplate();
        JwtCookieFeignInterceptor interceptor = new JwtCookieFeignInterceptor();

        // Act
        interceptor.apply(requestTemplate);

        // Assert
        assert !requestTemplate.headers().containsKey("Cookie");
    }

    @Test
    void testApply_whenRequestDoesNotContainAnyCookies_shouldDoNothing() {
        // Arrange
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getCookies()).thenReturn(null);

        ServletRequestAttributes mockAttributes = new ServletRequestAttributes(mockRequest);
        RequestContextHolder.setRequestAttributes(mockAttributes);

        RequestTemplate requestTemplate = new RequestTemplate();
        JwtCookieFeignInterceptor interceptor = new JwtCookieFeignInterceptor();

        // Act
        interceptor.apply(requestTemplate);

        // Assert
        assert !requestTemplate.headers().containsKey("Cookie");
    }
}