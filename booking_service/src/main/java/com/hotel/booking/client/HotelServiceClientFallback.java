package com.hotel.booking.client;

import com.hotel.booking.client.dto.HotelResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback pour HotelServiceClient
 */
@Component
@Slf4j
public class HotelServiceClientFallback implements HotelServiceClient {

    @Override
    public HotelResponse getHotelById(Long id) {
        log.warn("Hotel Service is unavailable, returning mock data for hotel ID: {}", id);
        
        HotelResponse mockHotel = new HotelResponse();
        mockHotel.setId(id);
        mockHotel.setName("Mock Hotel " + id);
        mockHotel.setDescription("This is a mock hotel (service unavailable)");
        mockHotel.setAddress("123 Mock Street");
        mockHotel.setCity("Paris");
        mockHotel.setCountry("France");
        mockHotel.setRating(4);
        
        return mockHotel;
    }
}
