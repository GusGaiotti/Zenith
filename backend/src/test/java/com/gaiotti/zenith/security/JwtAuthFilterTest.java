package com.gaiotti.zenith.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private FilterChain filterChain;

    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        jwtAuthFilter = new JwtAuthFilter(jwtService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_NoAuthorizationHeader_CallsFilterChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_InvalidHeaderPrefix_CallsFilterChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic somtoken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_ValidAccessToken_PopulatesSecurityContext() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validaccesstoken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isAccessToken("validaccesstoken")).thenReturn(true);
        when(jwtService.getUserIdFromToken("validaccesstoken")).thenReturn(1L);
        when(jwtService.getEmailFromToken("validaccesstoken")).thenReturn("test@example.com");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("test@example.com", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void doFilter_ValidTokenButRefreshToken_DoesNotPopulateSecurityContext() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validrefreshtoken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isAccessToken("validrefreshtoken")).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_ExpiredJwtException_SetsJwtErrorAttribute() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer expiredtoken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        io.jsonwebtoken.ExpiredJwtException ex = new io.jsonwebtoken.ExpiredJwtException(null, null, "Token expired");
        when(jwtService.isAccessToken("expiredtoken")).thenThrow(ex);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertEquals("Token has expired", request.getAttribute("jwt_error"));
    }

    @Test
    void doFilter_MalformedJwtException_SetsJwtErrorAttribute() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer malformedtoken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        io.jsonwebtoken.MalformedJwtException ex = new io.jsonwebtoken.MalformedJwtException("Invalid token");
        when(jwtService.isAccessToken("malformedtoken")).thenThrow(ex);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertEquals("Malformed token", request.getAttribute("jwt_error"));
    }

    @Test
    void doFilter_SignatureException_SetsJwtErrorAttribute() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer badsignaturetoken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        io.jsonwebtoken.security.SignatureException ex = new io.jsonwebtoken.security.SignatureException("Invalid signature");
        when(jwtService.isAccessToken("badsignaturetoken")).thenThrow(ex);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertEquals("Invalid token signature", request.getAttribute("jwt_error"));
    }

    @Test
    void doFilter_GenericJwtException_SetsJwtErrorAttribute() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalidtoken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        io.jsonwebtoken.UnsupportedJwtException ex = new io.jsonwebtoken.UnsupportedJwtException("Unsupported token");
        when(jwtService.isAccessToken("invalidtoken")).thenThrow(ex);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertEquals("Invalid token", request.getAttribute("jwt_error"));
    }
}
