package com.hotel.booking.kafka.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration professionnelle du Producer Kafka.
 * 
 * Implémente les best practices :
 * - Idempotence activée
 * - Compression des messages
 * - Retry mechanism configuré
 * - Timeouts optimisés
 * - Acks pour garantir la durabilité
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.acks:all}")
    private String acks;

    @Value("${spring.kafka.producer.retries:3}")
    private Integer retries;

    @Value("${spring.kafka.producer.compression-type:snappy}")
    private String compressionType;

    @Value("${spring.kafka.producer.max-in-flight-requests-per-connection:5}")
    private Integer maxInFlightRequests;

    @Value("${spring.kafka.producer.enable-idempotence:true}")
    private Boolean enableIdempotence;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // Configuration de base
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // Idempotence : évite les duplications en cas de retry
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, enableIdempotence);
        
        // Acks : garantit que tous les replicas ont reçu le message
        configProps.put(ProducerConfig.ACKS_CONFIG, acks);
        
        // Retry : nombre de tentatives en cas d'échec
        configProps.put(ProducerConfig.RETRIES_CONFIG, retries);
        
        // Max in-flight requests : nombre de requêtes non confirmées autorisées
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, maxInFlightRequests);
        
        // Compression : réduit la taille des messages (snappy est rapide et efficace)
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType);
        
        // Timeouts
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);
        
        // Batching pour optimiser les performances
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        
        // Configuration JSON Serializer
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
