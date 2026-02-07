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
 * Event publié lorsqu'un séjour est terminé (après le checkout).
 * Cet événement est consommé par :
 * - Room Service : pour remettre la chambre disponible
 * - Review Service : pour permettre à l'utilisateur de laisser un avis
 * - Analytics Service : pour les statistiques de séjour
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCompletedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID de la réservation complétée
     */
    private Long bookingId;

    /**
     * ID de l'utilisateur
     */
    private Long userId;

    /**
     * ID de la chambre à libérer
     */
    private Long roomId;

    /**
     * ID de l'hôtel
     */
    private Long hotelId;

    /**
     * Date d'arrivée effective
     */
    private LocalDate checkInDate;

    /**
     * Date de départ effective
     */
    private LocalDate checkOutDate;

    /**
     * Prix total payé
     */
    private BigDecimal totalPrice;

    /**
     * Nombre de nuits du séjour
     */
    private Integer numberOfNights;

    /**
     * Statut (devrait être COMPLETED)
     */
    private String status;

    /**
     * Timestamp de complétion
     */
    private LocalDateTime eventTimestamp;

    /**
     * Type d'événement
     */
    private String eventType = "BOOKING_COMPLETED";
}
