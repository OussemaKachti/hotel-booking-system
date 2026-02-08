package com.hotel.booking.kafka;

import com.hotel.booking.event.BookingCancelledEvent;
import com.hotel.booking.event.BookingCompletedEvent;
import com.hotel.booking.event.BookingCreatedEvent;
import com.hotel.booking.kafka.exception.KafkaPublishException;
import com.hotel.booking.kafka.model.DlqMessage;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Producer Kafka professionnel pour le Booking Service.
 * 
 * Fonctionnalités :
 * - Retry automatique avec backoff exponentiel
 * - Dead Letter Queue (DLQ) pour messages en échec
 * - Métriques et monitoring avec Micrometer
 * - Timeout configuré
 * - Logging détaillé
 * 
 * @author Booking Team
 */
@Service
@Slf4j
public class BookingEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final MeterRegistry meterRegistry;
    
    // Compteurs de métriques
    private final Counter successCounter;
    private final Counter failureCounter;
    private final Counter dlqCounter;
    private final Timer publishTimer;

    public BookingEventProducer(KafkaTemplate<String, Object> kafkaTemplate, 
                                MeterRegistry meterRegistry) {
        this.kafkaTemplate = kafkaTemplate;
        this.meterRegistry = meterRegistry;
        
        // Initialisation des métriques
        this.successCounter = Counter.builder("kafka.publish.success")
                .description("Nombre de messages publiés avec succès")
                .tag("service", "booking")
                .register(meterRegistry);
                
        this.failureCounter = Counter.builder("kafka.publish.failure")
                .description("Nombre d'échecs de publication")
                .tag("service", "booking")
                .register(meterRegistry);
                
        this.dlqCounter = Counter.builder("kafka.publish.dlq")
                .description("Nombre de messages envoyés vers la DLQ")
                .tag("service", "booking")
                .register(meterRegistry);
                
        this.publishTimer = Timer.builder("kafka.publish.duration")
                .description("Durée de publication des messages")
                .tag("service", "booking")
                .register(meterRegistry);
    }

    /**
     * Publie un événement de création de réservation avec retry automatique.
     * 
     * @param event L'événement BookingCreatedEvent à publier
     * @throws KafkaPublishException Si tous les retries échouent
     */
    @Retryable(
        retryFor = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public void publishBookingCreated(BookingCreatedEvent event) {
        publishEvent(
                KafkaTopicConfig.BOOKING_CREATED_TOPIC,
                event.getBookingId().toString(),
                event,
                "BookingCreated"
        );
    }

    /**
     * Méthode de récupération appelée après épuisement des retries pour BookingCreated.
     * Envoie le message vers la DLQ.
     */
    @Recover
    public void recoverBookingCreated(Exception e, BookingCreatedEvent event) {
        log.error("🔴 All retries exhausted for BookingCreatedEvent. Sending to DLQ. BookingId: {}", 
                event.getBookingId(), e);
        sendToDlq(KafkaTopicConfig.BOOKING_CREATED_TOPIC, event.getBookingId().toString(), event, e, 3);
    }

    /**
     * Publie un événement d'annulation de réservation avec retry automatique.
     * 
     * @param event L'événement BookingCancelledEvent à publier
     * @throws KafkaPublishException Si tous les retries échouent
     */
    @Retryable(
        retryFor = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public void publishBookingCancelled(BookingCancelledEvent event) {
        publishEvent(
                KafkaTopicConfig.BOOKING_CANCELLED_TOPIC,
                event.getBookingId().toString(),
                event,
                "BookingCancelled"
        );
    }

    /**
     * Méthode de récupération appelée après épuisement des retries pour BookingCancelled.
     */
    @Recover
    public void recoverBookingCancelled(Exception e, BookingCancelledEvent event) {
        log.error("🔴 All retries exhausted for BookingCancelledEvent. Sending to DLQ. BookingId: {}", 
                event.getBookingId(), e);
        sendToDlq(KafkaTopicConfig.BOOKING_CANCELLED_TOPIC, event.getBookingId().toString(), event, e, 3);
    }

    /**
     * Publie un événement de complétion de réservation avec retry automatique.
     * 
     * @param event L'événement BookingCompletedEvent à publier
     * @throws KafkaPublishException Si tous les retries échouent
     */
    @Retryable(
        retryFor = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public void publishBookingCompleted(BookingCompletedEvent event) {
        publishEvent(
                KafkaTopicConfig.BOOKING_COMPLETED_TOPIC,
                event.getBookingId().toString(),
                event,
                "BookingCompleted"
        );
    }

    /**
     * Méthode de récupération appelée après épuisement des retries pour BookingCompleted.
     */
    @Recover
    public void recoverBookingCompleted(Exception e, BookingCompletedEvent event) {
        log.error("🔴 All retries exhausted for BookingCompletedEvent. Sending to DLQ. BookingId: {}", 
                event.getBookingId(), e);
        sendToDlq(KafkaTopicConfig.BOOKING_COMPLETED_TOPIC, event.getBookingId().toString(), event, e, 3);
    }

    /**
     * Méthode centrale de publication avec métriques et timeout.
     * 
     * @param topic Le topic Kafka
     * @param key La clé du message
     * @param event L'événement à publier
     * @param eventType Le type d'événement (pour logging)
     * @throws KafkaPublishException En cas d'échec
     */
    private void publishEvent(String topic, String key, Object event, String eventType) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            log.info("📤 Publishing {} event to topic: {} - Key: {}", eventType, topic, key);

            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);

            // Attendre la réponse avec timeout (30 secondes)
            SendResult<String, Object> result = future.get();
            
            // Succès : incrémenter compteur et enregistrer durée
            successCounter.increment();
            sample.stop(publishTimer);
            
            log.info("✅ {} event published successfully to {} - Offset: {}, Partition: {}", 
                    eventType,
                    topic, 
                    result.getRecordMetadata().offset(),
                    result.getRecordMetadata().partition());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            failureCounter.increment();
            log.error("❌ Thread interrupted while publishing {} event to {}", eventType, topic, e);
            throw new KafkaPublishException(
                    "Thread interrupted during publish", topic, event, e);
                    
        } catch (ExecutionException e) {
            failureCounter.increment();
            log.error("❌ Failed to publish {} event to {}: {}", eventType, topic, e.getMessage(), e);
            throw new KafkaPublishException(
                    "Failed to publish event after retries", topic, event, e.getCause());
        }
    }

    /**
     * Envoie un message vers la Dead Letter Queue après épuisement des retries.
     * 
     * @param originalTopic Le topic original
     * @param key La clé du message
     * @param event L'événement original
     * @param error L'exception qui a causé l'échec
     * @param attemptCount Le nombre de tentatives effectuées
     */
    private void sendToDlq(String originalTopic, String key, Object event, Exception error, int attemptCount) {
        try {
            DlqMessage dlqMessage = DlqMessage.builder()
                    .originalTopic(originalTopic)
                    .messageKey(key)
                    .originalPayload(event)
                    .errorMessage(error.getMessage())
                    .errorStackTrace(getStackTrace(error))
                    .attemptCount(attemptCount)
                    .firstAttemptTime(LocalDateTime.now().minusSeconds(attemptCount * 2L)) // Estimation
                    .lastAttemptTime(LocalDateTime.now())
                    .dlqTimestamp(LocalDateTime.now())
                    .build();

            kafkaTemplate.send(KafkaTopicConfig.BOOKING_DLQ_TOPIC, key, dlqMessage);
            dlqCounter.increment();
            
            log.warn("⚠️ Message sent to DLQ: {} - Key: {} - Attempts: {}", 
                    originalTopic, key, attemptCount);

        } catch (Exception dlqError) {
            log.error("🔥 CRITICAL: Failed to send message to DLQ! Original topic: {} - Key: {}", 
                    originalTopic, key, dlqError);
            // En production, cela devrait déclencher une alerte
        }
    }

    /**
     * Extrait la stack trace d'une exception en String.
     */
    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
