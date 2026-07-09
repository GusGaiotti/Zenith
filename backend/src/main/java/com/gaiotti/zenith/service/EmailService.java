package com.gaiotti.zenith.service;

public interface EmailService {

    void sendPasswordResetEmail(String toEmail, String token);

    void sendInvitationEmail(String toEmail, String inviterName, String ledgerName, String token);
}
