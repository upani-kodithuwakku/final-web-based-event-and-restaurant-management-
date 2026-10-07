package com.group06.restaurantevent.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Optional SMTP delivery, without logging secret reset tokens. */
@Service
@Slf4j
@RequiredArgsConstructor
public class PasswordResetMailService {
    private final ObjectProvider<JavaMailSender> sender;

    @Value("${app.auth.mail-enabled:false}")
    private boolean enabled;

    @Value("${app.auth.mail-from:}")
    private String from;

    public void deliver(String email, String link, int minutes) {
        if (!enabled) {
            log.info("Password reset prepared; SMTP delivery is disabled");
            return;
        }
        JavaMailSender mail = sender.getIfAvailable();
        if (mail == null || from.isBlank()) {
            log.error("Password reset mail is enabled but SMTP host/from is not configured");
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Reset your Gather password");
        message.setText(
                "Use this link to reset your password within "
                        + minutes
                        + " minutes:\n"
                        + link
                        + "\nIf you did not request this, ignore this email.");
        try {
            mail.send(message);
        } catch (org.springframework.mail.MailException ex) {
            log.error("Password reset delivery failed ({})", ex.getClass().getSimpleName());
        }
    }
}
