package com.hotel.booking.kafka.exception;

/**
 * Exception levée lors de l'échec de publication d'un événement Kafka.
 */
public class KafkaPublishException extends RuntimeException {

    private final String topic;
    private final Object event;

    public KafkaPublishException(String message, String topic, Object event) {
        super(message);
        this.topic = topic;
        this.event = event;
    }

    public KafkaPublishException(String message, String topic, Object event, Throwable cause) {
        super(message, cause);
        this.topic = topic;
        this.event = event;
    }

    public String getTopic() {
        return topic;
    }

    public Object getEvent() {
        return event;
    }
}
