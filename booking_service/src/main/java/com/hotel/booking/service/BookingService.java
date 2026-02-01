package com.hotel.booking.service;

import com.hotel.booking.dto.BookingRequest;
import com.hotel.booking.dto.BookingResponse;
import com.hotel.booking.dto.BookingUpdateRequest;
import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.BookingStatus;
import com.hotel.booking.exception.BookingNotFoundException;
import com.hotel.booking.exception.InvalidBookingException;
import com.hotel.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;

    /**
     * Créer une nouvelle réservation
     */
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        log.info("Creating new booking for user: {}", request.getUserId());

        // Validation des dates
        validateDates(request.getCheckInDate(), request.getCheckOutDate());

        // Créer l'entité Booking
        Booking booking = new Booking();
        booking.setConfirmationNumber(generateConfirmationNumber());
        booking.setRoomId(request.getRoomId());
        booking.setHotelId(request.getHotelId());
        booking.setUserId(request.getUserId());
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setNumberOfGuests(request.getNumberOfGuests());
        booking.setPricePerNight(request.getPricePerNight());
        booking.setSpecialRequests(request.getSpecialRequests());

        // Calculer le nombre de nuits et le prix total
        int numberOfNights = calculateNumberOfNights(request.getCheckInDate(), request.getCheckOutDate());
        booking.setNumberOfNights(numberOfNights);
        
        BigDecimal totalPrice = request.getPricePerNight().multiply(BigDecimal.valueOf(numberOfNights));
        booking.setTotalPrice(totalPrice);

        booking.setStatus(BookingStatus.CONFIRMED);

        // Sauvegarder
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created successfully with confirmation number: {}", savedBooking.getConfirmationNumber());

        return mapToResponse(savedBooking);
    }

    /**
     * Récupérer toutes les réservations
     */
    public List<BookingResponse> getAllBookings() {
        log.info("Fetching all bookings");
        return bookingRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer une réservation par ID
     */
    public BookingResponse getBookingById(Long id) {
        log.info("Fetching booking with ID: {}", id);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + id));
        return mapToResponse(booking);
    }

    /**
     * Récupérer une réservation par numéro de confirmation
     */
    public BookingResponse getBookingByConfirmationNumber(String confirmationNumber) {
        log.info("Fetching booking with confirmation number: {}", confirmationNumber);
        Booking booking = bookingRepository.findByConfirmationNumber(confirmationNumber)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with confirmation number: " + confirmationNumber));
        return mapToResponse(booking);
    }

    /**
     * Récupérer toutes les réservations d'un utilisateur
     */
    public List<BookingResponse> getBookingsByUserId(String userId) {
        log.info("Fetching bookings for user: {}", userId);
        return bookingRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les réservations par statut
     */
    public List<BookingResponse> getBookingsByStatus(BookingStatus status) {
        log.info("Fetching bookings with status: {}", status);
        return bookingRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les réservations d'un hôtel
     */
    public List<BookingResponse> getBookingsByHotelId(Long hotelId) {
        log.info("Fetching bookings for hotel: {}", hotelId);
        return bookingRepository.findByHotelId(hotelId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Mettre à jour une réservation
     */
    @Transactional
    public BookingResponse updateBooking(Long id, BookingUpdateRequest request) {
        log.info("Updating booking with ID: {}", id);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + id));

        // Vérifier que la réservation peut être modifiée
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new InvalidBookingException("Cannot update a " + booking.getStatus() + " booking");
        }

        // Mettre à jour les champs si fournis
        if (request.getCheckInDate() != null && request.getCheckOutDate() != null) {
            validateDates(request.getCheckInDate(), request.getCheckOutDate());
            booking.setCheckInDate(request.getCheckInDate());
            booking.setCheckOutDate(request.getCheckOutDate());

            // Recalculer le nombre de nuits et le prix total
            int numberOfNights = calculateNumberOfNights(request.getCheckInDate(), request.getCheckOutDate());
            booking.setNumberOfNights(numberOfNights);
            BigDecimal totalPrice = booking.getPricePerNight().multiply(BigDecimal.valueOf(numberOfNights));
            booking.setTotalPrice(totalPrice);
        }

        if (request.getNumberOfGuests() != null) {
            booking.setNumberOfGuests(request.getNumberOfGuests());
        }

        if (request.getSpecialRequests() != null) {
            booking.setSpecialRequests(request.getSpecialRequests());
        }

        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Booking updated successfully: {}", id);

        return mapToResponse(updatedBooking);
    }

    /**
     * Annuler une réservation
     */
    @Transactional
    public BookingResponse cancelBooking(Long id) {
        log.info("Cancelling booking with ID: {}", id);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + id));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidBookingException("Booking is already cancelled");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new InvalidBookingException("Cannot cancel a completed booking");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking cancelledBooking = bookingRepository.save(booking);
        log.info("Booking cancelled successfully: {}", id);

        return mapToResponse(cancelledBooking);
    }

    /**
     * Supprimer une réservation (soft delete via statut)
     */
    @Transactional
    public void deleteBooking(Long id) {
        log.info("Deleting booking with ID: {}", id);
        
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + id));

        // On annule plutôt que de supprimer physiquement
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        log.info("Booking deleted (cancelled) successfully: {}", id);
    }

    // ==================== Helper Methods ====================

    /**
     * Valider les dates de réservation
     */
    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        LocalDate today = LocalDate.now();

        if (checkIn.isBefore(today)) {
            throw new InvalidBookingException("Check-in date cannot be in the past");
        }

        if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
            throw new InvalidBookingException("Check-out date must be after check-in date");
        }

        long daysBetween = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (daysBetween > 30) {
            throw new InvalidBookingException("Booking cannot exceed 30 nights");
        }
    }

    /**
     * Calculer le nombre de nuits
     */
    private int calculateNumberOfNights(LocalDate checkIn, LocalDate checkOut) {
        return (int) ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    /**
     * Générer un numéro de confirmation unique
     */
    private String generateConfirmationNumber() {
        return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Mapper Booking vers BookingResponse
     */
    private BookingResponse mapToResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setConfirmationNumber(booking.getConfirmationNumber());
        response.setRoomId(booking.getRoomId());
        response.setHotelId(booking.getHotelId());
        response.setUserId(booking.getUserId());
        response.setCheckInDate(booking.getCheckInDate());
        response.setCheckOutDate(booking.getCheckOutDate());
        response.setNumberOfGuests(booking.getNumberOfGuests());
        response.setNumberOfNights(booking.getNumberOfNights());
        response.setPricePerNight(booking.getPricePerNight());
        response.setTotalPrice(booking.getTotalPrice());
        response.setStatus(booking.getStatus());
        response.setSpecialRequests(booking.getSpecialRequests());
        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());
        return response;
    }
}
