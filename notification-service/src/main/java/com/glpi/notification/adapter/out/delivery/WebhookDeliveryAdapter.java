package com.glpi.notification.adapter.out.delivery;

import com.glpi.notification.domain.model.NotificationDeliveryException;
import com.glpi.notification.domain.model.QueuedNotification;
import com.glpi.notification.domain.port.out.DeliveryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Webhook delivery adapter — sends notifications via HTTP POST.
 * Spring Retry: max 3 attempts, exponential backoff 1s, 4s, 16s.
 * Requirements: 16.3, 16.6
 */
@Component("webhookDeliveryAdapter")
public class WebhookDeliveryAdapter implements DeliveryPort {

    private static final Logger log = LoggerFactory.getLogger(WebhookDeliveryAdapter.class);

    private final RestTemplate restTemplate;

    @Value("${notification.webhook-url:}")
    private String webhookUrl;

    public WebhookDeliveryAdapter() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    @Retryable(
            retryFor = NotificationDeliveryException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 4, maxDelay = 16000)
    )
    public void deliver(QueuedNotification notification) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.debug("No webhook URL configured, skipping webhook delivery");
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> payload = Map.of(
                    "eventType", notification.getEventType(),
                    "recipientId", notification.getRecipientId(),
                    "subject", notification.getSubject(),
                    "body", notification.getBody()
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(webhookUrl, request, String.class);
            log.info("Webhook delivered for event {}", notification.getEventType());
        } catch (RestClientException e) {
            throw new NotificationDeliveryException(
                    "Webhook delivery failed: " + e.getMessage(), e);
        }
    }
}
