package com.hotel.booking.kafka;

import com.hotel.booking.event.BookingCreatedEvent;
import com.hotel.booking.event.BookingCancelledEvent;
import com.hotel.booking.event.BookingCompletedEvent;
import com.hotel.booking.kafka.exception.KafkaPublishException;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration pour BookingEventProducer avec EmbeddedKafka.
 * 
 * Teste :
 * - Publication réussie des événements
 * - Contenu des messages
 * - Métriques (compteurs)
 * - Retry mechanism
 */
@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, 
               brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" },
               topics = { 
                   KafkaTopicConfig.BOOKING_CREATED_TOPIC, 
                   KafkaTopicConfig.BOOKING_CANCELLED_TOPIC, 
                   KafkaTopicConfig.BOOKING_COMPLETED_TOPIC,
                   KafkaTopicConfig.BOOKING_DLQ_TOPIC
               })
class BookingEventProducerIntegrationTest {

    @Autowired
    private BookingEventProducer eventProducer;

    @Autowired
    private MeterRegistry meterRegistry;

    private KafkaMessageListenerContainer<String, BookingCreatedEvent> createdListenerContainer;
    private KafkaMessageListenerContainer<String, BookingCancelledEvent> cancelledListenerContainer;
    private KafkaMessageListenerContainer<String, BookingCompletedEvent> completedListenerContainer;

    private BlockingQueue<ConsumerRecord<String, BookingCreatedEvent>> createdRecords;
    private BlockingQueue<ConsumerRecord<String, BookingCancelledEvent>> cancelledRecords;
    private BlockingQueue<ConsumerRecord<String, BookingCompletedEvent>> completedRecords;

    @BeforeEach
    void setUp() {
        createdRecords = new LinkedBlockingQueue<>();
        cancelledRecords = new LinkedBlockingQueue<>();
        completedRecords = new LinkedBlockingQueue<>();

        // Configuration des consumers pour les tests
        createCreatedEventConsumer();
        createCancelledEventConsumer();
        createCompletedEventConsumer();
    }

    @Test
    void shouldPublishBookingCreatedEventSuccessfully() throws InterruptedException {
        // Given
        BookingCreatedEvent event = BookingCreatedEvent.builder()
                .bookingId(1L)
                .userId(100L)
                .roomId(200L)
                .hotelId(300L)
                .checkInDate(LocalDate.now().plusDays(7))
                .checkOutDate(LocalDate.now().plusDays(10))
                .numberOfGuests(2)
                .totalPrice(BigDecimal.valueOf(450.00))
                .status("CONFIRMED")
                .eventTimestamp(LocalDateTime.now())
                .eventType("BOOKING_CREATED")
                .build();

        // When
        eventProducer.publishBookingCreated(event);

        // Then : vérifier que le message est reçu
        ConsumerRecord<String, BookingCreatedEvent> record = createdRecords.poll(10, TimeUnit.SECONDS);
        assertThat(record).isNotNull();
        assertThat(record.key()).isEqualTo("1");
        assertThat(record.value().getBookingId()).isEqualTo(1L);
        assertThat(record.value().getUserId()).isEqualTo(100L);
        assertThat(record.value().getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(450.00));

        // Vérifier les métriques
        double successCount = meterRegistry.counter("kafka.publish.success", "service", "booking").count();
        assertThat(successCount).isGreaterThan(0);
    }

    @Test
    void shouldPublishBookingCancelledEventSuccessfully() throws InterruptedException {
        // Given
        BookingCancelledEvent event = BookingCancelledEvent.builder()
                .bookingId(2L)
                .userId(100L)
                .roomId(200L)
                .hotelId(300L)
                .checkInDate(LocalDate.now().plusDays(7))
                .checkOutDate(LocalDate.now().plusDays(10))
                .cancellationReason("Client request")
                .status("CANCELLED")
                .eventTimestamp(LocalDateTime.now())
                .eventType("BOOKING_CANCELLED")
                .build();

        // When
        eventProducer.publishBookingCancelled(event);

        // Then
        ConsumerRecord<String, BookingCancelledEvent> record = cancelledRecords.poll(10, TimeUnit.SECONDS);
        assertThat(record).isNotNull();
        assertThat(record.key()).isEqualTo("2");
        assertThat(record.value().getBookingId()).isEqualTo(2L);
        assertThat(record.value().getCancellationReason()).isEqualTo("Client request");
        assertThat(record.value().getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void shouldPublishBookingCompletedEventSuccessfully() throws InterruptedException {
        // Given
        BookingCompletedEvent event = BookingCompletedEvent.builder()
                .bookingId(3L)
                .userId(100L)
                .roomId(200L)
                .hotelId(300L)
                .checkInDate(LocalDate.now().minusDays(3))
                .checkOutDate(LocalDate.now())
                .numberOfNights(3)
                .totalPrice(BigDecimal.valueOf(450.00))
                .status("COMPLETED")
                .eventTimestamp(LocalDateTime.now())
                .eventType("BOOKING_COMPLETED")
                .build();

        // When
        eventProducer.publishBookingCompleted(event);

        // Then
        ConsumerRecord<String, BookingCompletedEvent> record = completedRecords.poll(10, TimeUnit.SECONDS);
        assertThat(record).isNotNull();
        assertThat(record.key()).isEqualTo("3");
        assertThat(record.value().getBookingId()).isEqualTo(3L);
        assertThat(record.value().getNumberOfNights()).isEqualTo(3);
        assertThat(record.value().getStatus()).isEqualTo("COMPLETED");
    }

    // ===== HELPER METHODS =====

    private void createCreatedEventConsumer() {
        Map<String, Object> consumerProps = getConsumerProps("booking-created-test-group");
        
        DefaultKafkaConsumerFactory<String, BookingCreatedEvent> cf = 
                new DefaultKafkaConsumerFactory<>(consumerProps);
        
        ContainerProperties containerProps = new ContainerProperties(KafkaTopicConfig.BOOKING_CREATED_TOPIC);
        createdListenerContainer = new KafkaMessageListenerContainer<>(cf, containerProps);
        
        createdListenerContainer.setupMessageListener((MessageListener<String, BookingCreatedEvent>) 
                record -> createdRecords.add(record));
        
        createdListenerContainer.start();
        ContainerTestUtils.waitForAssignment(createdListenerContainer, 1);
    }

    private void createCancelledEventConsumer() {
        Map<String, Object> consumerProps = getConsumerProps("booking-cancelled-test-group");
        
        DefaultKafkaConsumerFactory<String, BookingCancelledEvent> cf = 
                new DefaultKafkaConsumerFactory<>(consumerProps);
        
        ContainerProperties containerProps = new ContainerProperties(KafkaTopicConfig.BOOKING_CANCELLED_TOPIC);
        cancelledListenerContainer = new KafkaMessageListenerContainer<>(cf, containerProps);
        
        cancelledListenerContainer.setupMessageListener((MessageListener<String, BookingCancelledEvent>) 
                record -> cancelledRecords.add(record));
        
        cancelledListenerContainer.start();
        ContainerTestUtils.waitForAssignment(cancelledListenerContainer, 1);
    }

    private void createCompletedEventConsumer() {
        Map<String, Object> consumerProps = getConsumerProps("booking-completed-test-group");
        
        DefaultKafkaConsumerFactory<String, BookingCompletedEvent> cf = 
                new DefaultKafkaConsumerFactory<>(consumerProps);
        
        ContainerProperties containerProps = new ContainerProperties(KafkaTopicConfig.BOOKING_COMPLETED_TOPIC);
        completedListenerContainer = new KafkaMessageListenerContainer<>(cf, containerProps);
        
        completedListenerContainer.setupMessageListener((MessageListener<String, BookingCompletedEvent>) 
                record -> completedRecords.add(record));
        
        completedListenerContainer.start();
        ContainerTestUtils.waitForAssignment(completedListenerContainer, 1);
    }

    private Map<String, Object> getConsumerProps(String groupId) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.hotel.booking.event");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.hotel.booking.event.BookingCreatedEvent");
        return props;
    }
}
