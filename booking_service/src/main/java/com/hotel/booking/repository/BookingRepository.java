package com.hotel.booking.repository;

import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Trouver une réservation par numéro de confirmation
    Optional<Booking> findByConfirmationNumber(String confirmationNumber);

    // Trouver toutes les réservations d'un utilisateur
    List<Booking> findByUserId(String userId);

    // Trouver toutes les réservations d'un hôtel
    List<Booking> findByHotelId(Long hotelId);

    // Trouver toutes les réservations d'une chambre
    List<Booking> findByRoomId(Long roomId);

    // Trouver les réservations par statut
    List<Booking> findByStatus(BookingStatus status);

    // Trouver les réservations d'un utilisateur avec un statut spécifique
    List<Booking> findByUserIdAndStatus(String userId, BookingStatus status);

    // Trouver les réservations d'une chambre entre deux dates (pour vérifier la disponibilité)
    List<Booking> findByRoomIdAndCheckInDateLessThanEqualAndCheckOutDateGreaterThanEqualAndStatusIn(
            Long roomId, LocalDate checkOut, LocalDate checkIn, List<BookingStatus> statuses);

    // Compter les réservations d'un utilisateur
    long countByUserId(String userId);

    // Vérifier si une réservation existe pour une chambre à des dates données
    boolean existsByRoomIdAndCheckInDateLessThanAndCheckOutDateGreaterThanAndStatusIn(
            Long roomId, LocalDate checkOut, LocalDate checkIn, List<BookingStatus> statuses);
}
