package com.hotel.booking.client;

import com.hotel.booking.client.dto.RoomResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Client Feign pour communiquer avec Room Service
 * fallback = Classe qui sera appelée si Room Service est indisponible
 */
@FeignClient(
        name = "room-service",
        url = "${services.room-service.url:http://localhost:8082}",
        fallback = RoomServiceClientFallback.class
)
public interface RoomServiceClient {

    /**
     * Récupérer une chambre par ID
     */
    @GetMapping("/api/rooms/{id}")
    RoomResponse getRoomById(@PathVariable Long id);

    /**
     * Vérifier la disponibilité d'une chambre
     */
    @GetMapping("/api/rooms/{id}/availability")
    Boolean checkAvailability(
            @PathVariable Long id,
            @RequestParam String checkIn,
            @RequestParam String checkOut
    );
}
