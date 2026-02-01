package com.hotel.booking.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO pour la réponse du Room Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {
    private Long id;
    private String roomNumber;
    private String roomType;
    private Long hotelId;
    private BigDecimal pricePerNight;
    private Integer capacity;
    private Boolean available;
}
