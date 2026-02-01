package com.hotel.booking.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Événement publié quand une réservation est créée
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCreatedEvent {
    
    private Long bookingId;
    private String confirmationNumber;
    private Long roomId;
    private Long hotelId;
    private String userId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfGuests;
    private Integer numberOfNights;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private String eventType = "BOOKING_CREATED";
}
