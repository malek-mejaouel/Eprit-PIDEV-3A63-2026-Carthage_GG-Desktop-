package com.carthagegg.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Reservation {
    public enum Status { WAITING, CONFIRMED, CANCELLED }
    
    private int id;
    private String name;
    private BigDecimal price;
    private LocalDateTime reservationDate;
    private int eventId;
    private Status status;

    public Reservation() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public LocalDateTime getReservationDate() { return reservationDate; }
    public void setReservationDate(LocalDateTime reservationDate) { this.reservationDate = reservationDate; }
    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
