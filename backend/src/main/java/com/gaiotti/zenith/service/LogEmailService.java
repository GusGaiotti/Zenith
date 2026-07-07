package com.gaiotti.zenith.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Fallback used when {@code app.mail.enabled} is false (default). It does not
 * send anything; it logs the action and the link so the flow can be exercised
 * without an SMTP server configured.
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "app.mail", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LogEmailService implements EmailService {

    @Value("${app.base-url}")
    private String appBaseUrl;

    @Override
    public void sendPasswordResetEmail(String toEmail, String token) {
        log.info("[mail disabled] password reset for {} -> {}/reset-password?token={}", toEmail, appBaseUrl, token);
    }

    @Override
    public void sendInvitationEmail(String toEmail, String inviterName, String ledgerName, String token) {
        log.info("[mail disabled] invitation for {} to ledger \"{}\" -> {}/ledger/join/{}",
                toEmail, ledgerName, appBaseUrl, token);
    }
}
