package com.mindcluster.safediary.iam.infrastructure.notifications;

import com.mindcluster.safediary.iam.application.internal.outboundservices.notifications.EmailNotificationClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import java.time.Instant;

/**
 * Sends the password recovery code through SMTP when spring.mail.host is configured. Without SMTP the code is
 * only written to the log in the dev profile, so it can be tested locally; it is never logged elsewhere.
 */
@Component
public class EmailNotificationAdapter implements EmailNotificationClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailNotificationAdapter.class);
    private final ObjectProvider<JavaMailSender> mailSender;
    private final Environment environment;
    private final String from;

    public EmailNotificationAdapter(ObjectProvider<JavaMailSender> mailSender, Environment environment,
                                    @Value("${iam.mail.from:no-reply@safediary.app}") String from) {
        this.mailSender = mailSender;
        this.environment = environment;
        this.from = from;
    }

    public void sendPasswordResetCode(String email, String code, Instant expiresAt) {
        var sender = mailSender.getIfAvailable();
        if (sender == null) {
            if (environment.matchesProfiles("dev & !prod")) LOGGER.info("[DEV] Password reset code for {}: {}", email, code);
            else LOGGER.warn("E-mail delivery is not configured; the password reset code could not be sent");
            return;
        }
        var message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("SafeDiary - Código de recuperación");
        message.setText("Tu código para restablecer la contraseña es " + code
                + ". Vence en 10 minutos. Si no lo solicitaste, ignora este correo.");
        try {
            sender.send(message);
        } catch (MailException ex) {
            LOGGER.error("The password reset e-mail could not be delivered: {}", ex.getMessage());
        }
    }
}
