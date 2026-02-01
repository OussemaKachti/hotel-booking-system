package com.hotel.booking.kafka;

import com.hotel.booking.config.KafkaTopicConfig;
import com.hotel.booking.event.BookingCancelledEvent;
import com.hotel.booking.event.BookingCompletedEvent;
import com.hotel.booking.event.BookingCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service pour publier des événements Kafka
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publier l'événement "Réservation créée"
     */
    public void publishBookingCreated(BookingCreatedEvent event) {
        log.info("Publishing BookingCreatedEvent for booking ID: {}", event.getBookingId());
        
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                KafkaTopicConfig.BOOKING_CREATED_TOPIC,
                event.getBookingId().toString(),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✓ BookingCreatedEvent published successfully: bookingId={}, offset={}",
                        event.getBookingId(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("✗ Failed to publish BookingCreatedEvent: bookingId={}, error={}",
                        event.getBookingId(),
                        ex.getMessage());
            }
        });
    }

    /**
     * Publier l'événement "Réservation annulée"
     */
    public void publishBookingCancelled(BookingCancelledEvent event) {
        log.info("Publishing BookingCancelledEvent for booking ID: {}", event.getBookingId());
        
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                KafkaTopicConfig.BOOKING_CANCELLED_TOPIC,
                event.getBookingId().toString(),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✓ BookingCancelledEvent published successfully: bookingId={}, offset={}",
                        event.getBookingId(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("✗ Failed to publish BookingCancelledEvent: bookingId={}, error={}",
                        event.getBookingId(),
                        ex.getMessage());
            }
        });
    }

    /**
     * Publier l'événement "Séjour terminé"
     */
    public void publishBookingCompleted(BookingCompletedEvent event) {
        log.info("Publishing BookingCompletedEvent for booking ID: {}", event.getBookingId());
        
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                KafkaTopicConfig.BOOKING_COMPLETED_TOPIC,
                event.getBookingId().toString(),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✓ BookingCompletedEvent published successfully: bookingId={}, offset={}",
                        event.getBookingId(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("✗ Failed to publish BookingCompletedEvent: bookingId={}, error={}",
                        event.getBookingId(),
                        ex.getMessage());
            }
        });
    }
}
