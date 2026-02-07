package com.hotel.booking.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Event publié lorsqu'une nouvelle réservation est créée.
 * Cet événement est consommé par :
 * - Room Service : pour marquer la chambre comme réservée
 * - User Service : pour mettre à jour l'historique utilisateur
 * - Analytics Service : pour enregistrer les statistiques
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreatedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID de la réservation créée
     */
    private Long bookingId;

    /**
     * ID de l'utilisateur ayant effectué la réservation
     */
    private Long userId;

    /**
     * ID de la chambre réservée
     */
    private Long roomId;

    /**
     * ID de l'hôtel
     */
    private Long hotelId;

    /**
     * Date d'arrivée
     */
    private LocalDate checkInDate;

    /**
     * Date de départ
     */
    private LocalDate checkOutDate;

    /**
     * Nombre de personnes
     */
    private Integer numberOfGuests;

    /**
     * Prix total de la réservation
     */
    private BigDecimal totalPrice;

    /**
     * Statut de la réservation (devrait être CONFIRMED)
     */
    private String status;

    /**
     * Timestamp de création de l'événement
     */
    private LocalDateTime eventTimestamp;

    /**
     * Type d'événement
     */
    private String eventType = "BOOKING_CREATED";
}
