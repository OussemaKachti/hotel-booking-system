package com.hotel.booking.kafka.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Modèle représentant un message dans la Dead Letter Queue.
 * Contient l'événement original + métadonnées d'erreur.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DlqMessage implements Serializable {
    
    /**
     * Topic original où le message devait être publié
     */
    private String originalTopic;
    
    /**
     * Clé du message original
     */
    private String messageKey;
    
    /**
     * Payload du message original (sérialisé en JSON)
     */
    private Object originalPayload;
    
    /**
     * Message d'erreur
     */
    private String errorMessage;
    
    /**
     * Stack trace de l'erreur
     */
    private String errorStackTrace;
    
    /**
     * Nombre de tentatives effectuées
     */
    private int attemptCount;
    
    /**
     * Timestamp de la première tentative
     */
    private LocalDateTime firstAttemptTime;
    
    /**
     * Timestamp de la dernière tentative
     */
    private LocalDateTime lastAttemptTime;
    
    /**
     * Timestamp d'arrivée dans la DLQ
     */
    private LocalDateTime dlqTimestamp;
}
