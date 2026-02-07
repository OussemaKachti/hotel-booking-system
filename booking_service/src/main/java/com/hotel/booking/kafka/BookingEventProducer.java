package com.hotel.booking.kafka;

import com.hotel.booking.event.BookingCancelledEvent;
import com.hotel.booking.event.BookingCompletedEvent;
import com.hotel.booking.event.BookingCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service responsable de la publication des événements Kafka
 * pour le Booking Service.
 * 
 * Ce producer envoie des événements asynchrones aux autres microservices
 * sans bloquer le flux transactionnel principal.
 */
@Service
@Slf4j
public class BookingEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public BookingEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publie un événement de création de réservation.
     * 
     * @param event L'événement BookingCreatedEvent à publier
     */
    public void publishBookingCreated(BookingCreatedEvent event) {
        try {
            log.info("📤 Publishing event to topic: {} - BookingId: {}", 
                    KafkaTopicConfig.BOOKING_CREATED_TOPIC, event.getBookingId());

            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                    KafkaTopicConfig.BOOKING_CREATED_TOPIC,
                    event.getBookingId().toString(),
                    event
            );

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("✅ Event published successfully: {} - Offset: {}", 
                            KafkaTopicConfig.BOOKING_CREATED_TOPIC, 
                            result.getRecordMetadata().offset());
                } else {
                    log.error("❌ Failed to publish event to {}: {}", 
                            KafkaTopicConfig.BOOKING_CREATED_TOPIC, 
                            ex.getMessage(), ex);
                }
            });

        } catch (Exception e) {
            log.error("❌ Error publishing BookingCreatedEvent: {}", e.getMessage(), e);
            // Ne pas lever l'exception pour ne pas bloquer la transaction DB
        }
    }

    /**
     * Publie un événement d'annulation de réservation.
     * 
     * @param event L'événement BookingCancelledEvent à publier
     */
    public void publishBookingCancelled(BookingCancelledEvent event) {
        try {
            log.info("📤 Publishing event to topic: {} - BookingId: {}", 
                    KafkaTopicConfig.BOOKING_CANCELLED_TOPIC, event.getBookingId());

            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                    KafkaTopicConfig.BOOKING_CANCELLED_TOPIC,
                    event.getBookingId().toString(),
                    event
            );

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("✅ Event published successfully: {} - Offset: {}", 
                            KafkaTopicConfig.BOOKING_CANCELLED_TOPIC, 
                            result.getRecordMetadata().offset());
                } else {
                    log.error("❌ Failed to publish event to {}: {}", 
                            KafkaTopicConfig.BOOKING_CANCELLED_TOPIC, 
                            ex.getMessage(), ex);
                }
            });

        } catch (Exception e) {
            log.error("❌ Error publishing BookingCancelledEvent: {}", e.getMessage(), e);
            // Ne pas lever l'exception pour ne pas bloquer la transaction DB
        }
    }

    /**
     * Publie un événement de complétion de séjour.
     * 
     * @param event L'événement BookingCompletedEvent à publier
     */
    public void publishBookingCompleted(BookingCompletedEvent event) {
        try {
            log.info("📤 Publishing event to topic: {} - BookingId: {}", 
                    KafkaTopicConfig.BOOKING_COMPLETED_TOPIC, event.getBookingId());

            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                    KafkaTopicConfig.BOOKING_COMPLETED_TOPIC,
                    event.getBookingId().toString(),
                    event
            );

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("✅ Event published successfully: {} - Offset: {}", 
                            KafkaTopicConfig.BOOKING_COMPLETED_TOPIC, 
                            result.getRecordMetadata().offset());
                } else {
                    log.error("❌ Failed to publish event to {}: {}", 
                            KafkaTopicConfig.BOOKING_COMPLETED_TOPIC, 
                            ex.getMessage(), ex);
                }
            });

        } catch (Exception e) {
            log.error("❌ Error publishing BookingCompletedEvent: {}", e.getMessage(), e);
            // Ne pas lever l'exception pour ne pas bloquer la transaction DB
        }
    }
}
