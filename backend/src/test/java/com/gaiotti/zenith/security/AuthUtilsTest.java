package com.gaiotti.zenith.security;

import com.gaiotti.zenith.exception.ResourceNotFoundException;
import com.gaiotti.zenith.model.User;
import com.gaiotti.zenith.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthUtilsTest {

    @Mock
    private UserRepository userRepository;

    private AuthUtils authUtils;

    @BeforeEach
    void setUp() {
        authUtils = new AuthUtils(userRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAuthenticatedUser_NoAuthentication_ThrowsResourceNotFoundException() {
        SecurityContextHolder.clearContext();

        assertThrows(ResourceNotFoundException.class, () -> authUtils.getAuthenticatedUser());
    }

    @Test
    void getAuthenticatedUser_UnauthenticatedObject_ThrowsResourceNotFoundException() {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                null, null, java.util.Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThrows(ResourceNotFoundException.class, () -> authUtils.getAuthenticatedUser());
    }

    @Test
    void getAuthenticatedUser_PrincipalIsNull_ThrowsResourceNotFoundException() {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                null, null, java.util.Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThrows(ResourceNotFoundException.class, () -> authUtils.getAuthenticatedUser());
    }

    @Test
    void getAuthenticatedUser_EmailNotFoundInDB_ThrowsResourceNotFoundException() {
        CustomUserDetails userDetails = new CustomUserDetails(
                1L, "nonexistent@example.com", java.util.Collections.emptyList()
        );

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authUtils.getAuthenticatedUser());
    }

    @Test
    void getAuthenticatedUser_ValidAuthentication_ReturnsUserEntity() {
        User expectedUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("Test User")
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(
                1L, "test@example.com", java.util.Collections.emptyList()
        );

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.findById(1L)).thenReturn(Optional.of(expectedUser));

        User result = authUtils.getAuthenticatedUser();

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals(1L, result.getId());
    }
}
