package com.gaiotti.zenith.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AllowedOriginsProviderTest {

    @Test
    void initialize_NonProdWithoutOrigins_UsesLocalhostDefault() {
        Environment environment = mock(Environment.class);
        when(environment.acceptsProfiles((org.springframework.core.env.Profiles) any())).thenReturn(false);

        AllowedOriginsProvider provider = new AllowedOriginsProvider(environment);
        ReflectionTestUtils.setField(provider, "corsAllowedOrigins", "");
        provider.initialize();

        assertEquals(java.util.List.of("http://localhost:3000"), provider.getAllowedOrigins());
        assertTrue(provider.isAllowedOrigin("http://localhost:3000"));
    }

    @Test
    void initialize_WithTrailingSlash_NormalizesOrigins() {
        Environment environment = mock(Environment.class);
        when(environment.acceptsProfiles((org.springframework.core.env.Profiles) any())).thenReturn(false);

        AllowedOriginsProvider provider = new AllowedOriginsProvider(environment);
        ReflectionTestUtils.setField(provider, "corsAllowedOrigins", " https://app.example.com/ , http://localhost:3000 ");
        provider.initialize();

        assertTrue(provider.isAllowedOrigin("https://app.example.com"));
        assertTrue(provider.isAllowedOrigin("https://app.example.com/"));
        assertTrue(provider.isAllowedOrigin("http://localhost:3000"));
    }

    @Test
    void initialize_ProdWithoutOrigins_Throws() {
        Environment environment = mock(Environment.class);
        when(environment.acceptsProfiles((org.springframework.core.env.Profiles) any())).thenReturn(true);

        AllowedOriginsProvider provider = new AllowedOriginsProvider(environment);
        ReflectionTestUtils.setField(provider, "corsAllowedOrigins", "");

        assertThrows(IllegalStateException.class, provider::initialize);
    }

    @Test
    void extractOrigin_ValidReferer_ReturnsOrigin() {
        Environment environment = mock(Environment.class);
        when(environment.acceptsProfiles((org.springframework.core.env.Profiles) any())).thenReturn(false);

        AllowedOriginsProvider provider = new AllowedOriginsProvider(environment);
        ReflectionTestUtils.setField(provider, "corsAllowedOrigins", "http://localhost:3000");
        provider.initialize();

        assertEquals("https://app.example.com:8443", provider.extractOrigin("https://app.example.com:8443/path?q=1"));
        assertNull(provider.extractOrigin("not-a-url"));
    }
}
