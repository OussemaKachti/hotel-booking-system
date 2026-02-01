package com.hotel.booking.controller;

import com.hotel.booking.dto.BookingRequest;
import com.hotel.booking.dto.BookingResponse;
import com.hotel.booking.dto.BookingUpdateRequest;
import com.hotel.booking.entity.BookingStatus;
import com.hotel.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    /**
     * Créer une nouvelle réservation
     * POST /api/bookings
     */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        log.info("POST /api/bookings - Creating booking for user: {}", request.getUserId());
        BookingResponse response = bookingService.createBooking(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Récupérer toutes les réservations
     * GET /api/bookings
     * GET /api/bookings?status=CONFIRMED
     */
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings(
            @RequestParam(required = false) BookingStatus status) {
        log.info("GET /api/bookings - Fetching bookings with status filter: {}", status);
        
        List<BookingResponse> bookings;
        if (status != null) {
            bookings = bookingService.getBookingsByStatus(status);
        } else {
            bookings = bookingService.getAllBookings();
        }
        
        return ResponseEntity.ok(bookings);
    }

    /**
     * Récupérer une réservation par ID
     * GET /api/bookings/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        log.info("GET /api/bookings/{} - Fetching booking by ID", id);
        BookingResponse response = bookingService.getBookingById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer une réservation par numéro de confirmation
     * GET /api/bookings/confirmation/{confirmationNumber}
     */
    @GetMapping("/confirmation/{confirmationNumber}")
    public ResponseEntity<BookingResponse> getBookingByConfirmationNumber(
            @PathVariable String confirmationNumber) {
        log.info("GET /api/bookings/confirmation/{} - Fetching booking by confirmation number", confirmationNumber);
        BookingResponse response = bookingService.getBookingByConfirmationNumber(confirmationNumber);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer toutes les réservations d'un utilisateur
     * GET /api/bookings/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByUserId(@PathVariable String userId) {
        log.info("GET /api/bookings/user/{} - Fetching bookings by user ID", userId);
        List<BookingResponse> bookings = bookingService.getBookingsByUserId(userId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Récupérer toutes les réservations d'un hôtel
     * GET /api/bookings/hotel/{hotelId}
     */
    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByHotelId(@PathVariable Long hotelId) {
        log.info("GET /api/bookings/hotel/{} - Fetching bookings by hotel ID", hotelId);
        List<BookingResponse> bookings = bookingService.getBookingsByHotelId(hotelId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Mettre à jour une réservation
     * PUT /api/bookings/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<BookingResponse> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody BookingUpdateRequest request) {
        log.info("PUT /api/bookings/{} - Updating booking", id);
        BookingResponse response = bookingService.updateBooking(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Annuler une réservation
     * PATCH /api/bookings/{id}/cancel
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long id) {
        log.info("PATCH /api/bookings/{}/cancel - Cancelling booking", id);
        BookingResponse response = bookingService.cancelBooking(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Supprimer une réservation
     * DELETE /api/bookings/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        log.info("DELETE /api/bookings/{} - Deleting booking", id);
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Health check
     * GET /api/bookings/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Booking Service is running!");
    }
}
