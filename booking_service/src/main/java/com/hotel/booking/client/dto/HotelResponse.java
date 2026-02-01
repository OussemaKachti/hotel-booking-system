package com.hotel.booking.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la réponse du Hotel Service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelResponse {
    private Long id;
    private String name;
    private String description;
    private String address;
    private String city;
    private String country;
    private Integer rating;
}
