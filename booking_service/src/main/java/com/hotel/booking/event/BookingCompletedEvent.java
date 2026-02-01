package com.hotel.booking.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Événement publié quand un séjour est terminé
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCompletedEvent {
    
    private Long bookingId;
    private String confirmationNumber;
    private Long roomId;
    private Long hotelId;
    private String userId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfNights;
    private BigDecimal totalPrice;
    private LocalDateTime completedAt;
    private String eventType = "BOOKING_COMPLETED";
}
