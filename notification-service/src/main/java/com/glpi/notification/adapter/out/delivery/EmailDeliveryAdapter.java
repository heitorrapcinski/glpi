package com.glpi.notification.adapter.out.delivery;

import com.glpi.notification.domain.model.NotificationDeliveryException;
import com.glpi.notification.domain.model.QueuedNotification;
import com.glpi.notification.domain.port.out.DeliveryPort;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

/**
 * Email delivery adapter — sends notifications via SMTP using JavaMail.
 * Spring Retry: max 3 attempts, exponential backoff 1s, 4s, 16s.
 * Requirements: 16.3, 16.6
 */
@Component("emailDeliveryAdapter")
public class EmailDeliveryAdapter implements DeliveryPort {

    private static final Logger log = LoggerFactory.getLogger(EmailDeliveryAdapter.class);

    private final JavaMailSender mailSender;

    @Value("${notification.sender-email:noreply@glpi.local}")
    private String senderEmail;

    public EmailDeliveryAdapter(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Retryable(
            retryFor = NotificationDeliveryException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 4, maxDelay = 16000)
    )
    public void deliver(QueuedNotification notification) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(notification.getRecipientAddress());
            helper.setSubject(notification.getSubject());
            helper.setText(notification.getBody(), true);
            mailSender.send(message);
            log.info("Email sent to {}", notification.getRecipientAddress());
        } catch (MessagingException e) {
            throw new NotificationDeliveryException(
                    "Failed to send email to " + notification.getRecipientAddress(), e);
        }
    }
}
