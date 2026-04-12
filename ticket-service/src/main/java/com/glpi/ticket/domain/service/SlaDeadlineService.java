package com.glpi.ticket.domain.service;

import com.glpi.ticket.domain.model.SlaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.Map;

/**
 * Calls SLA Service to compute TTO/TTR deadlines for a ticket.
 * Requirements: 9.1, 9.2, 9.7, 9.8
 */
@Service
public class SlaDeadlineService {

    private static final Logger log = LoggerFactory.getLogger(SlaDeadlineService.class);

    private final RestClient restClient;

    public SlaDeadlineService(
            @Value("${sla-service.url:http://localhost:8086}") String slaServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(slaServiceUrl)
                .build();
    }

    /**
     * Compute TTO and TTR deadlines by calling SLA Service.
     *
     * @param slaId     the SLA identifier
     * @param createdAt ticket creation timestamp
     * @return SlaContext with computed deadlines, or empty context on failure
     */
    public SlaContext computeDeadlines(String slaId, Instant createdAt) {
        if (slaId == null || slaId.isBlank()) {
            return new SlaContext();
        }
        try {
            Map<?, ?> response = restClient.post()
                    .uri("/slas/compute-deadline")
                    .body(Map.of("slaId", slaId, "startDate", createdAt.toString()))
                    .retrieve()
                    .body(Map.class);

            SlaContext ctx = new SlaContext();
            ctx.setSlaId(slaId);
            if (response != null) {
                Object tto = response.get("ttoDeadline");
                Object ttr = response.get("ttrDeadline");
                if (tto != null) ctx.setTtoDeadline(Instant.parse(tto.toString()));
                if (ttr != null) ctx.setTtrDeadline(Instant.parse(ttr.toString()));
            }
            return ctx;
        } catch (RestClientException e) {
            log.warn("Failed to compute SLA deadlines for slaId={}: {}", slaId, e.getMessage());
            SlaContext ctx = new SlaContext();
            ctx.setSlaId(slaId);
            return ctx;
        }
    }
}
