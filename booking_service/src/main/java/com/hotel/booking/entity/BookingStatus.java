package com.hotel.booking.entity;

public enum BookingStatus {
    PENDING,      // En attente de confirmation
    CONFIRMED,    // Confirmée
    CANCELLED,    // Annulée
    COMPLETED,    // Séjour terminé
    NO_SHOW       // Client non présenté
}
