package com.gaiotti.zenith.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @Value("${app.mail.from}")
    private String fromAddress;

    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetLink = appBaseUrl + "/reset-password?token=" + token;
        String body = """
                Você solicitou a redefinição de senha da sua conta Zenith.

                Clique no link abaixo para criar uma nova senha. O link é válido por 1 hora.

                %s

                Se você não solicitou isso, ignore este email — sua senha não será alterada.
                """.formatted(resetLink);

        send(toEmail, "Redefinição de senha — Zenith", body);
    }

    public void sendInvitationEmail(String toEmail, String inviterName, String ledgerName, String token) {
        String joinLink = appBaseUrl + "/ledger/join/" + token;
        String body = """
                %s te convidou para compartilhar o ledger "%s" no Zenith.

                Acesse o link abaixo para aceitar o convite:

                %s

                O convite expira em 7 dias. Se você não tem uma conta, será necessário criar uma antes de aceitar.
                """.formatted(inviterName, ledgerName, joinLink);

        send(toEmail, inviterName + " te convidou para o Zenith", body);
    }

    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (MailException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
