package com.example.chambre;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "chambres")
public class Chambre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String roomNumber;

    @Column(nullable = false)
    private Long hotelId; // Reference to Hotel Service

    @Column(nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    private ChambreStatus status;

    @Column(nullable = false)
    private BigDecimal pricePerNight;

    public Chambre() {}

    public Chambre(String roomNumber, Long hotelId, int capacity,
                   ChambreStatus status, BigDecimal pricePerNight) {
        this.roomNumber = roomNumber;
        this.hotelId = hotelId;
        this.capacity = capacity;
        this.status = status;
        this.pricePerNight = pricePerNight;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setStatus(ChambreStatus status) {
        this.status = status;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public Long getId() {
        return id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public Long getHotelId() {
        return hotelId;
    }

    public int getCapacity() {
        return capacity;
    }

    public ChambreStatus getStatus() {
        return status;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }
}
