package com.glpi.notification.config;

import net.jqwik.api.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Preservation property tests for notification-service KafkaConfig.
 *
 * These tests verify that existing consumer, producer, and DLQ configuration
 * remains unchanged regardless of the bootstrap server value.
 * They must PASS on UNFIXED code to establish the baseline behavior.
 *
 * Validates: Requirements 3.1, 3.2, 3.4, 3.5, 3.6
 */
class KafkaConfigPreservationTest {

    // ---------------------------------------------------------------
    // Property: Consumer config preserved for any bootstrap server
    // ---------------------------------------------------------------

    /**
     * Property 2: Preservation — Consumer group ID is always "notification-service"
     * regardless of bootstrap server value.
     *
     * Validates: Requirements 3.1, 3.2
     */
    @Property(tries = 50)
    void consumerFactory_groupId_isAlwaysNotificationService(
            @ForAll("bootstrapServers") String bootstrapServer) {

        KafkaConfig kafkaConfig = new KafkaConfig();
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", bootstrapServer);

        ConsumerFactory<String, Object> factory = kafkaConfig.consumerFactory();
        Map<String, Object> configs = factory.getConfigurationProperties();

        assertEquals("notification-service", configs.get(ConsumerConfig.GROUP_ID_CONFIG),
                "Consumer group ID must be 'notification-service'");
    }

    /**
     * Property 2: Preservation — Consumer key deserializer is always StringDeserializer
     * regardless of bootstrap server value.
     *
     * Validates: Requirements 3.2
     */
    @Property(tries = 50)
    void consumerFactory_keyDeserializer_isAlwaysStringDeserializer(
            @ForAll("bootstrapServers") String bootstrapServer) {

        KafkaConfig kafkaConfig = new KafkaConfig();
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", bootstrapServer);

        ConsumerFactory<String, Object> factory = kafkaConfig.consumerFactory();
        Map<String, Object> configs = factory.getConfigurationProperties();

        assertEquals(StringDeserializer.class, configs.get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG),
                "Consumer key deserializer must be StringDeserializer");
    }

    /**
     * Property 2: Preservation — Consumer value deserializer is always JsonDeserializer
     * regardless of bootstrap server value.
     *
     * Validates: Requirements 3.2
     */
    @Property(tries = 50)
    void consumerFactory_valueDeserializer_isAlwaysJsonDeserializer(
            @ForAll("bootstrapServers") String bootstrapServer) {

        KafkaConfig kafkaConfig = new KafkaConfig();
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", bootstrapServer);

        ConsumerFactory<String, Object> factory = kafkaConfig.consumerFactory();
        Map<String, Object> configs = factory.getConfigurationProperties();

        assertEquals(JsonDeserializer.class, configs.get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG),
                "Consumer value deserializer must be JsonDeserializer");
    }

    /**
     * Property 2: Preservation — Consumer trusted packages is always "*"
     * regardless of bootstrap server value.
     *
     * Validates: Requirements 3.2
     */
    @Property(tries = 50)
    void consumerFactory_trustedPackages_isAlwaysWildcard(
            @ForAll("bootstrapServers") String bootstrapServer) {

        KafkaConfig kafkaConfig = new KafkaConfig();
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", bootstrapServer);

        ConsumerFactory<String, Object> factory = kafkaConfig.consumerFactory();
        Map<String, Object> configs = factory.getConfigurationProperties();

        assertEquals("*", configs.get(JsonDeserializer.TRUSTED_PACKAGES),
                "Consumer trusted packages must be '*'");
    }

    /**
     * Property 2: Preservation — Consumer auto offset reset is always "earliest"
     * regardless of bootstrap server value.
     *
     * Validates: Requirements 3.2
     */
    @Property(tries = 50)
    void consumerFactory_autoOffsetReset_isAlwaysEarliest(
            @ForAll("bootstrapServers") String bootstrapServer) {

        KafkaConfig kafkaConfig = new KafkaConfig();
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", bootstrapServer);

        ConsumerFactory<String, Object> factory = kafkaConfig.consumerFactory();
        Map<String, Object> configs = factory.getConfigurationProperties();

        assertEquals("earliest", configs.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG),
                "Consumer auto offset reset must be 'earliest'");
    }

    /**
     * Property 2: Preservation — Consumer bootstrap.servers matches the injected value
     * regardless of what that value is.
     *
     * Validates: Requirements 3.1, 3.5
     */
    @Property(tries = 50)
    void consumerFactory_bootstrapServers_matchesInjectedValue(
            @ForAll("bootstrapServers") String bootstrapServer) {

        KafkaConfig kafkaConfig = new KafkaConfig();
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", bootstrapServer);

        ConsumerFactory<String, Object> factory = kafkaConfig.consumerFactory();
        Map<String, Object> configs = factory.getConfigurationProperties();

        assertEquals(bootstrapServer, configs.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG),
                "Consumer bootstrap.servers must match the injected @Value");
    }

    // ---------------------------------------------------------------
    // Property: Producer config preserved for any bootstrap server
    // ---------------------------------------------------------------

    /**
     * Property 2: Preservation — Producer key serializer is always StringSerializer
     * regardless of bootstrap server value.
     *
     * Validates: Requirements 3.6
     */
    @Property(tries = 50)
    void producerFactory_keySerializer_isAlwaysStringSerializer(
            @ForAll("bootstrapServers") String bootstrapServer) {

        KafkaConfig kafkaConfig = new KafkaConfig();
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", bootstrapServer);

        ProducerFactory<String, Object> factory = kafkaConfig.producerFactory();
        Map<String, Object> configs = factory.getConfigurationProperties();

        assertEquals(StringSerializer.class, configs.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG),
                "Producer key serializer must be StringSerializer");
    }

    /**
     * Property 2: Preservation — Producer value serializer is always JsonSerializer
     * regardless of bootstrap server value.
     *
     * Validates: Requirements 3.6
     */
    @Property(tries = 50)
    void producerFactory_valueSerializer_isAlwaysJsonSerializer(
            @ForAll("bootstrapServers") String bootstrapServer) {

        KafkaConfig kafkaConfig = new KafkaConfig();
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", bootstrapServer);

        ProducerFactory<String, Object> factory = kafkaConfig.producerFactory();
        Map<String, Object> configs = factory.getConfigurationProperties();

        assertEquals(JsonSerializer.class, configs.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG),
                "Producer value serializer must be JsonSerializer");
    }

    // ---------------------------------------------------------------
    // Property: @Value default fallback is localhost:9092
    // ---------------------------------------------------------------

    /**
     * Property 2: Preservation — The @Value annotation default fallback is localhost:9092.
     * This preserves local development connectivity.
     *
     * Validates: Requirements 3.1
     */
    @Example
    void valueAnnotation_defaultFallback_isLocalhost9092() throws NoSuchFieldException {
        var field = KafkaConfig.class.getDeclaredField("bootstrapServers");
        var valueAnnotation = field.getAnnotation(
                org.springframework.beans.factory.annotation.Value.class);

        assertNotNull(valueAnnotation, "@Value annotation must be present on bootstrapServers field");
        assertEquals("${spring.kafka.bootstrap-servers:localhost:9092}", valueAnnotation.value(),
                "@Value default fallback must be localhost:9092");
    }

    // ---------------------------------------------------------------
    // Example-based: DLQ configuration preservation
    // ---------------------------------------------------------------

    /**
     * Extracts the ExponentialBackOff from a DefaultErrorHandler by searching
     * through the entire class hierarchy for any field that holds a BackOff instance.
     */
    private ExponentialBackOff extractBackOff(DefaultErrorHandler handler) {
        Class<?> clazz = handler.getClass();
        while (clazz != null) {
            for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(handler);
                    if (value instanceof ExponentialBackOff) {
                        return (ExponentialBackOff) value;
                    }
                    // The backoff may be nested inside another object (e.g., FailureTracker)
                    if (value != null && !field.getType().isPrimitive()
                            && !field.getType().getName().startsWith("java.lang")) {
                        ExponentialBackOff nested = findBackOffInObject(value);
                        if (nested != null) return nested;
                    }
                } catch (IllegalAccessException e) {
                    // continue searching
                }
            }
            clazz = clazz.getSuperclass();
        }
        fail("Could not find ExponentialBackOff in DefaultErrorHandler hierarchy");
        return null;
    }

    /**
     * Recursively searches an object's fields for an ExponentialBackOff instance.
     */
    private ExponentialBackOff findBackOffInObject(Object obj) {
        if (obj == null) return null;
        Class<?> clazz = obj.getClass();
        while (clazz != null && !clazz.getName().startsWith("java.")) {
            for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(obj);
                    if (value instanceof ExponentialBackOff) {
                        return (ExponentialBackOff) value;
                    }
                } catch (IllegalAccessException e) {
                    // continue
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    /**
     * Example: DLQ error handler uses ExponentialBackOff with 1000ms initial interval
     * and multiplier 4.0.
     *
     * Validates: Requirements 3.4
     */
    @Example
    void kafkaListenerContainerFactory_dlqBackoff_hasCorrectInitialIntervalAndMultiplier() {
        KafkaConfig kafkaConfig = new KafkaConfig();
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                kafkaConfig.kafkaListenerContainerFactory();

        DefaultErrorHandler handler = (DefaultErrorHandler) ReflectionTestUtils.getField(factory, "commonErrorHandler");
        assertNotNull(handler, "DLQ error handler must be configured");

        ExponentialBackOff backOff = extractBackOff(handler);
        assertNotNull(backOff, "ExponentialBackOff must be configured in the error handler");

        assertEquals(1000L, backOff.getInitialInterval(),
                "DLQ backoff initial interval must be 1000ms (1 second)");
        assertEquals(4.0, backOff.getMultiplier(),
                "DLQ backoff multiplier must be 4.0");
    }

    /**
     * Example: DLQ error handler uses maxAttempts=3.
     *
     * Validates: Requirements 3.4
     */
    @Example
    void kafkaListenerContainerFactory_dlqBackoff_hasMaxAttempts3() {
        KafkaConfig kafkaConfig = new KafkaConfig();
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                kafkaConfig.kafkaListenerContainerFactory();

        DefaultErrorHandler handler = (DefaultErrorHandler) ReflectionTestUtils.getField(factory, "commonErrorHandler");
        assertNotNull(handler, "DLQ error handler must be configured");

        ExponentialBackOff backOff = extractBackOff(handler);
        assertNotNull(backOff, "ExponentialBackOff must be configured in the error handler");

        assertEquals(3L, backOff.getMaxAttempts(),
                "DLQ backoff max attempts must be 3");
    }

    /**
     * Example: kafkaListenerContainerFactory uses AckMode.RECORD.
     *
     * Validates: Requirements 3.2
     */
    @Example
    void kafkaListenerContainerFactory_ackMode_isRecord() {
        KafkaConfig kafkaConfig = new KafkaConfig();
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                kafkaConfig.kafkaListenerContainerFactory();

        assertEquals(ContainerProperties.AckMode.RECORD,
                factory.getContainerProperties().getAckMode(),
                "kafkaListenerContainerFactory must use AckMode.RECORD");
    }

    // ---------------------------------------------------------------
    // Generators
    // ---------------------------------------------------------------

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
