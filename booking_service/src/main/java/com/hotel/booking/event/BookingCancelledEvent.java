package com.hotel.booking.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Événement publié quand une réservation est annulée
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCancelledEvent {
    
    private Long bookingId;
    private String confirmationNumber;
    private Long roomId;
    private Long hotelId;
    private String userId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String cancellationReason;
    private LocalDateTime cancelledAt;
    private String eventType = "BOOKING_CANCELLED";
}
