package com.hotel.booking.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration professionnelle des topics Kafka pour le Booking Service.
 * 
 * Best practices implémentées :
 * - Partitions multiples pour scalabilité
 * - Replicas pour fault tolerance
 * - Retention policy configurée
 * - Dead Letter Queue pour messages en échec
 * - Configuration par environment (dev/prod)
 */
@Configuration
public class KafkaTopicConfig {

    // Noms des topics
    public static final String BOOKING_CREATED_TOPIC = "booking.created";
    public static final String BOOKING_CANCELLED_TOPIC = "booking.cancelled";
    public static final String BOOKING_COMPLETED_TOPIC = "booking.completed";
    public static final String BOOKING_DLQ_TOPIC = "booking.dlq"; // Dead Letter Queue

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    /**
     * Topic pour les événements de création de réservation.
     * Consommé par : Room Service, Analytics Service
     * 
     * Production : 3 partitions, 2 replicas
     * Dev : 1 partition, 1 replica
     */
    @Bean
    public NewTopic bookingCreatedTopic() {
        int partitions = isProduction() ? 3 : 1;
        int replicas = isProduction() ? 2 : 1;
        
        return TopicBuilder.name(BOOKING_CREATED_TOPIC)
                .partitions(partitions)
                .replicas(replicas)
                .configs(getTopicConfigs())
                .build();
    }

    /**
     * Topic pour les événements d'annulation de réservation.
     * Consommé par : Room Service, User Service, Analytics Service
     * 
     * Production : 3 partitions, 2 replicas
     * Dev : 1 partition, 1 replica
     */
    @Bean
    public NewTopic bookingCancelledTopic() {
        int partitions = isProduction() ? 3 : 1;
        int replicas = isProduction() ? 2 : 1;
        
        return TopicBuilder.name(BOOKING_CANCELLED_TOPIC)
                .partitions(partitions)
                .replicas(replicas)
                .configs(getTopicConfigs())
                .build();
    }

    /**
     * Topic pour les événements de complétion de réservation.
     * Consommé par : Review Service, Analytics Service
     * 
     * Production : 3 partitions, 2 replicas
     * Dev : 1 partition, 1 replica
     */
    @Bean
    public NewTopic bookingCompletedTopic() {
        int partitions = isProduction() ? 3 : 1;
        int replicas = isProduction() ? 2 : 1;
        
        return TopicBuilder.name(BOOKING_COMPLETED_TOPIC)
                .partitions(partitions)
                .replicas(replicas)
                .configs(getTopicConfigs())
                .build();
    }

    /**
     * Dead Letter Queue : topic pour stocker les messages qui ont échoué après tous les retries.
     * Permet d'analyser les erreurs sans perdre de données.
     * 
     * Production : 2 partitions, 2 replicas
     * Dev : 1 partition, 1 replica
     */
    @Bean
    public NewTopic bookingDlqTopic() {
        int partitions = isProduction() ? 2 : 1;
        int replicas = isProduction() ? 2 : 1;
        
        Map<String, String> dlqConfigs = new HashMap<>(getTopicConfigs());
        // Retention plus longue pour la DLQ (30 jours vs 7 jours)
        dlqConfigs.put("retention.ms", String.valueOf(30L * 24 * 60 * 60 * 1000)); // 30 jours
        
        return TopicBuilder.name(BOOKING_DLQ_TOPIC)
                .partitions(partitions)
                .replicas(replicas)
                .configs(dlqConfigs)
                .build();
    }

    /**
     * Configuration commune pour tous les topics.
     */
    private Map<String, String> getTopicConfigs() {
        Map<String, String> configs = new HashMap<>();
        
        // Retention : 7 jours en production, 1 jour en dev
        long retentionMs = isProduction() 
                ? 7L * 24 * 60 * 60 * 1000  // 7 jours
                : 1L * 24 * 60 * 60 * 1000; // 1 jour
        configs.put("retention.ms", String.valueOf(retentionMs));
        
        // Compression (correspond à la config du producer)
        configs.put("compression.type", "snappy");
        
        // Segment size : 1GB
        configs.put("segment.ms", String.valueOf(7L * 24 * 60 * 60 * 1000)); // 7 jours
        
        // Min in-sync replicas (prod only)
        if (isProduction()) {
            configs.put("min.insync.replicas", "2");
        }
        
        return configs;
    }

    private boolean isProduction() {
        return "prod".equalsIgnoreCase(activeProfile) || "production".equalsIgnoreCase(activeProfile);
    }
}

