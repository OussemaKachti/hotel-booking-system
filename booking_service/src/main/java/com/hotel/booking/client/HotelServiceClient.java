package com.hotel.booking.client;

import com.hotel.booking.client.dto.HotelResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Client Feign pour communiquer avec Hotel Service
 */
@FeignClient(
        name = "hotel-service",
        url = "${services.hotel-service.url:http://localhost:8083}",
        fallback = HotelServiceClientFallback.class
)
public interface HotelServiceClient {

    /**
     * Récupérer un hôtel par ID
     */
    @GetMapping("/api/hotels/{id}")
    HotelResponse getHotelById(@PathVariable Long id);
}
