package com.hotel.booking.client;

import com.hotel.booking.client.dto.RoomResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Fallback pour RoomServiceClient
 * Retourne des données mockées si le service n'est pas disponible
 */
@Component
@Slf4j
public class RoomServiceClientFallback implements RoomServiceClient {

    @Override
    public RoomResponse getRoomById(Long id) {
        log.warn("Room Service is unavailable, returning mock data for room ID: {}", id);
        
        // Retourner des données mockées
        RoomResponse mockRoom = new RoomResponse();
        mockRoom.setId(id);
        mockRoom.setRoomNumber("MOCK-" + id);
        mockRoom.setRoomType("STANDARD");
        mockRoom.setHotelId(1L);
        mockRoom.setPricePerNight(BigDecimal.valueOf(100.00));
        mockRoom.setCapacity(2);
        mockRoom.setAvailable(true);
        
        return mockRoom;
    }

    @Override
    public Boolean checkAvailability(Long id, String checkIn, String checkOut) {
        log.warn("Room Service is unavailable, assuming room {} is available", id);
        return true; // Par défaut, on considère la chambre comme disponible
    }
}
