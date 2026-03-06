package com.gaiotti.zenith.service.ai;

import com.gaiotti.zenith.config.AiProperties;
import com.gaiotti.zenith.exception.AccessDeniedException;
import com.gaiotti.zenith.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiAccessControlServiceTest {

    private Environment environment;
    private AiProperties aiProperties;
    private AiAccessControlService service;

    @BeforeEach
    void setUp() {
        environment = mock(Environment.class);
        aiProperties = new AiProperties();
        service = new AiAccessControlService(environment, aiProperties);
    }

    @Test
    void assertAiAccess_ProdUserWithoutAllowlist_ThrowsForbidden() {
        when(environment.acceptsProfiles(Profiles.of("prod"))).thenReturn(true);

        User user = User.builder()
                .id(1L)
                .email("blocked@example.com")
                .displayName("Blocked")
                .aiEnabled(false)
                .build();

        assertThatThrownBy(() -> service.assertAiAccess(user))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("AI access is not enabled for this user");
    }

    @Test
    void assertAiAccess_ProdUserInEmailAllowlist_Allows() {
        when(environment.acceptsProfiles(Profiles.of("prod"))).thenReturn(true);
        aiProperties.getAccess().setProductionAllowlistEmails("member@example.com, other@example.com");

        User user = User.builder()
                .id(1L)
                .email("member@example.com")
                .displayName("Allowed")
                .aiEnabled(false)
                .build();

        assertThatCode(() -> service.assertAiAccess(user)).doesNotThrowAnyException();
    }
}
