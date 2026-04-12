package com.glpi.gateway.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Aggregates health status from all downstream services.
 * Polls each service's /actuator/health endpoint and returns a combined status.
 */
@RestController
@RequestMapping("/actuator/health")
public class HealthAggregatorController {

    private static final Logger log = LoggerFactory.getLogger(HealthAggregatorController.class);
    private static final Duration HEALTH_TIMEOUT = Duration.ofSeconds(3);

    private final WebClient webClient;
    private final DownstreamServiceConfig serviceConfig;

    public HealthAggregatorController(WebClient.Builder webClientBuilder,
                                      DownstreamServiceConfig serviceConfig) {
        this.webClient = webClientBuilder.build();
        this.serviceConfig = serviceConfig;
    }

    @GetMapping
    public Mono<Map<String, Object>> aggregateHealth() {
        return Flux.fromIterable(serviceConfig.getDownstreamServices())
                .flatMap(service -> checkServiceHealth(service.getName(), service.getUrl()))
                .collectMap(ServiceHealth::name, ServiceHealth::status)
                .map(serviceStatuses -> {
                    boolean allUp = serviceStatuses.values().stream()
                            .allMatch("UP"::equals);

                    Map<String, Object> response = new HashMap<>();
                    response.put("status", allUp ? "UP" : "DEGRADED");
                    response.put("components", serviceStatuses);
                    return response;
                });
    }

    @SuppressWarnings("unchecked")
    private Mono<ServiceHealth> checkServiceHealth(String name, String baseUrl) {
        return webClient.get()
                .uri(baseUrl + "/actuator/health")
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(HEALTH_TIMEOUT)
                .map(body -> {
                    String status = (String) body.getOrDefault("status", "UNKNOWN");
                    return new ServiceHealth(name, status);
                })
                .onErrorReturn(new ServiceHealth(name, "DOWN"));
    }

    record ServiceHealth(String name, String status) {}
}
