package com.glpi.ticket.config;

import net.jqwik.api.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bug condition exploration test for Kafka consumer connectivity in Docker.
 *
 * This test encodes the EXPECTED behavior after the fix:
 * - consumerFactory() config map SHALL contain reconnection backoff properties
 * - application-docker.yml SHALL exist for both services
 *
 * On UNFIXED code, this test is EXPECTED TO FAIL — failure confirms the bug exists.
 *
 * Validates: Requirements 1.1, 1.2, 1.3, 1.4
 */
class KafkaConfigBugConditionTest {

    /**
     * Property 1: Bug Condition — Kafka Consumer Missing Reconnection Config
     *
     * For any bootstrap server string, the consumerFactory() config map
     * SHALL contain ALL reconnection and timeout properties:
     * - reconnect.backoff.ms
     * - reconnect.backoff.max.ms
     * - retry.backoff.ms
     * - request.timeout.ms
     * - connections.max.idle.ms
     *
     * Validates: Requirements 1.3, 1.4
     */
    @Property(tries = 50)
    void consumerFactory_shallContainReconnectionProperties(
            @ForAll("bootstrapServers") String bootstrapServer) {

        KafkaConfig kafkaConfig = new KafkaConfig();
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", bootstrapServer);

        ConsumerFactory<String, Map<String, Object>> factory = kafkaConfig.consumerFactory();
        Map<String, Object> configs = factory.getConfigurationProperties();

        assertAll(
                "consumerFactory() config must contain all reconnection properties",
                () -> assertTrue(configs.containsKey(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG),
                        "consumerFactory() config does not contain reconnect.backoff.ms"),
                () -> assertTrue(configs.containsKey(ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG),
                        "consumerFactory() config does not contain reconnect.backoff.max.ms"),
                () -> assertTrue(configs.containsKey(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG),
                        "consumerFactory() config does not contain retry.backoff.ms"),
                () -> assertTrue(configs.containsKey(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG),
                        "consumerFactory() config does not contain request.timeout.ms"),
                () -> assertTrue(configs.containsKey(ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG),
                        "consumerFactory() config does not contain connections.max.idle.ms")
        );
    }

    /**
     * Bug Condition — application-docker.yml missing for ticket-service.
     *
     * The Docker profile configuration file SHALL exist at
     * ticket-service/src/main/resources/application-docker.yml.
     *
     * Validates: Requirements 1.1, 1.2
     */
    @Example
    void ticketService_applicationDockerYml_shallExist() {
        File dockerYml = new File("src/main/resources/application-docker.yml");
        assertTrue(dockerYml.exists(),
                "application-docker.yml not found at ticket-service/src/main/resources/application-docker.yml");
    }

    /**
     * Bug Condition — application-docker.yml missing for notification-service.
     *
     * The Docker profile configuration file SHALL exist at
     * notification-service/src/main/resources/application-docker.yml.
     *
     * Validates: Requirements 1.1, 1.2
     */
    @Example
    void notificationService_applicationDockerYml_shallExist() {
        File dockerYml = new File("../notification-service/src/main/resources/application-docker.yml");
        assertTrue(dockerYml.exists(),
                "application-docker.yml not found at notification-service/src/main/resources/application-docker.yml");
    }

    /**
     * Generates random bootstrap server strings in the format host:port.
     * Covers various valid Kafka bootstrap server addresses.
     */
    @Provide
    Arbitrary<String> bootstrapServers() {
        Arbitrary<String> hosts = Arbitraries.of(
                "localhost", "kafka", "broker", "kafka-1", "kafka-2",
                "192.168.1.100", "10.0.0.5", "kafka.internal",
                "my-kafka-broker.example.com", "kafka-service.default.svc.cluster.local"
        );
        Arbitrary<Integer> ports = Arbitraries.integers().between(1024, 65535);
        return Combinators.combine(hosts, ports).as((host, port) -> host + ":" + port);
    }
}
