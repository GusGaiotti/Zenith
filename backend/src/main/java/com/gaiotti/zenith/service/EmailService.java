package com.gaiotti.zenith.service;

/**
 * Sends transactional emails. The active implementation is selected by the
 * {@code app.mail.enabled} property: {@link SmtpEmailService} when true,
 * {@link LogEmailService} otherwise.
 */
public interface EmailService {

    void sendPasswordResetEmail(String toEmail, String token);

    void sendInvitationEmail(String toEmail, String inviterName, String ledgerName, String token);
}
