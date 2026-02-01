package com.hotel.booking.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Configuration des topics Kafka
 */
@Configuration
public class KafkaTopicConfig {

    public static final String BOOKING_CREATED_TOPIC = "booking.created";
    public static final String BOOKING_CANCELLED_TOPIC = "booking.cancelled";
    public static final String BOOKING_COMPLETED_TOPIC = "booking.completed";

    @Bean
    public NewTopic bookingCreatedTopic() {
        return TopicBuilder.name(BOOKING_CREATED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic bookingCancelledTopic() {
        return TopicBuilder.name(BOOKING_CANCELLED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic bookingCompletedTopic() {
        return TopicBuilder.name(BOOKING_COMPLETED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
