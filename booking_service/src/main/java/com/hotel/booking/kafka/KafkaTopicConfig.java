package com.hotel.booking.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Configuration des topics Kafka pour le Booking Service.
 * 
 * Cette classe définit les topics utilisés pour la communication asynchrone
 * avec les autres microservices du système.
 */
@Configuration
public class KafkaTopicConfig {

    /**
     * Nom du topic pour les événements de création de réservation
     */
    public static final String BOOKING_CREATED_TOPIC = "booking.created";

    /**
     * Nom du topic pour les événements d'annulation de réservation
     */
    public static final String BOOKING_CANCELLED_TOPIC = "booking.cancelled";

    /**
     * Nom du topic pour les événements de complétion de séjour
     */
    public static final String BOOKING_COMPLETED_TOPIC = "booking.completed";

    /**
     * Configuration du topic booking.created
     * 
     * @return Topic avec 1 partition et 1 réplica (dev)
     */
    @Bean
    public NewTopic bookingCreatedTopic() {
        return TopicBuilder
                .name(BOOKING_CREATED_TOPIC)
                .partitions(1)
                .replicas(1)
                .compact()
                .build();
    }

    /**
     * Configuration du topic booking.cancelled
     * 
     * @return Topic avec 1 partition et 1 réplica (dev)
     */
    @Bean
    public NewTopic bookingCancelledTopic() {
        return TopicBuilder
                .name(BOOKING_CANCELLED_TOPIC)
                .partitions(1)
                .replicas(1)
                .compact()
                .build();
    }

    /**
     * Configuration du topic booking.completed
     * 
     * @return Topic avec 1 partition et 1 réplica (dev)
     */
    @Bean
    public NewTopic bookingCompletedTopic() {
        return TopicBuilder
                .name(BOOKING_COMPLETED_TOPIC)
                .partitions(1)
                .replicas(1)
                .compact()
                .build();
    }
}
