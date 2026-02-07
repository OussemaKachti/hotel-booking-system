package com.hotel.booking.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Event publié lorsqu'une réservation est annulée.
 * Cet événement est consommé par :
 * - Room Service : pour libérer la chambre
 * - User Service : pour mettre à jour l'historique
 * - Analytics Service : pour les statistiques d'annulation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCancelledEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID de la réservation annulée
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
     * Date d'arrivée prévue (pour libérer les bonnes dates)
     */
    private LocalDate checkInDate;

    /**
     * Date de départ prévue
     */
    private LocalDate checkOutDate;

    /**
     * Raison de l'annulation (optionnel)
     */
    private String cancellationReason;

    /**
     * Statut après annulation (devrait être CANCELLED)
     */
    private String status;

    /**
     * Timestamp de l'annulation
     */
    private LocalDateTime eventTimestamp;

    /**
     * Type d'événement
     */
    private String eventType = "BOOKING_CANCELLED";
}
