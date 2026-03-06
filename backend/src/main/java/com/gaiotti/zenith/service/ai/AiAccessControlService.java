package com.gaiotti.zenith.service.ai;

import com.gaiotti.zenith.config.AiProperties;
import com.gaiotti.zenith.exception.AccessDeniedException;
import com.gaiotti.zenith.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AiAccessControlService {

    private final Environment environment;
    private final AiProperties aiProperties;

    public void assertAiAccess(User user) {
        if (!isAiAllowed(user)) {
            throw new AccessDeniedException("AI access is not enabled for this user");
        }
    }

    public boolean isAiAllowed(User user) {
        if (!environment.acceptsProfiles(Profiles.of("prod"))) {
            return true;
        }

        if (user.isAiEnabled()) {
            return true;
        }

        Set<String> allowlistedEmails = Arrays.stream(aiProperties.getAccess().getProductionAllowlistEmails().split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return allowlistedEmails.contains(user.getEmail().toLowerCase());
    }
}
