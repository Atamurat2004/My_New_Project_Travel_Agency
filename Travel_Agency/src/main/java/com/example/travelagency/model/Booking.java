package com.example.travelagency.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    @JsonBackReference("user-booking")
    private User user;
    @ManyToOne
    @JoinColumn(name = "tour_id", nullable = false)
    @JsonBackReference("tour-booking")
    private Tour tour;
    private LocalDateTime bookingDate;
    private LocalDateTime confirmedDate;
    private LocalDateTime paymentDate;
    private LocalDateTime completedDate;
    private LocalDateTime cancelledDate;
    private LocalDateTime refundDate;
    private BigDecimal refundAmount;
    private String status; // NEW, CONFIRMED, PAID, COMPLETED, CANCELLED

    public Booking() {}
    public Booking(Long id, User user, Tour tour, LocalDateTime bookingDate, LocalDateTime confirmedDate,
                   LocalDateTime paymentDate, LocalDateTime completedDate, LocalDateTime cancelledDate,
                   LocalDateTime refundDate, BigDecimal refundAmount, String status) {
        this.id = id;
        this.user = user;
        this.tour = tour;
        this.bookingDate = bookingDate;
        this.confirmedDate = confirmedDate;
        this.paymentDate = paymentDate;
        this.completedDate = completedDate;
        this.cancelledDate = cancelledDate;
        this.refundDate = refundDate;
        this.refundAmount = refundAmount;
        this.status = status;
    }
    public static BookingBuilder builder() { return new BookingBuilder(); }
    public static class BookingBuilder {
        private Long id;
        private User user;
        private Tour tour;
        private LocalDateTime bookingDate;
        private LocalDateTime confirmedDate;
        private LocalDateTime paymentDate;
        private LocalDateTime completedDate;
        private LocalDateTime cancelledDate;
        private LocalDateTime refundDate;
        private BigDecimal refundAmount;
        private String status;
        public BookingBuilder id(Long id) { this.id = id; return this; }
        public BookingBuilder user(User user) { this.user = user; return this; }
        public BookingBuilder tour(Tour tour) { this.tour = tour; return this; }
        public BookingBuilder bookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; return this; }
        public BookingBuilder confirmedDate(LocalDateTime confirmedDate) { this.confirmedDate = confirmedDate; return this; }
        public BookingBuilder paymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; return this; }
        public BookingBuilder completedDate(LocalDateTime completedDate) { this.completedDate = completedDate; return this; }
        public BookingBuilder cancelledDate(LocalDateTime cancelledDate) { this.cancelledDate = cancelledDate; return this; }
        public BookingBuilder refundDate(LocalDateTime refundDate) { this.refundDate = refundDate; return this; }
        public BookingBuilder refundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; return this; }
        public BookingBuilder status(String status) { this.status = status; return this; }
        public Booking build() {
            return new Booking(id, user, tour, bookingDate, confirmedDate, paymentDate,
                             completedDate, cancelledDate, refundDate, refundAmount, status);
        }
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Tour getTour() { return tour; }
    public void setTour(Tour tour) { this.tour = tour; }
    public LocalDateTime getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }
    public LocalDateTime getConfirmedDate() { return confirmedDate; }
    public void setConfirmedDate(LocalDateTime confirmedDate) { this.confirmedDate = confirmedDate; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
    public LocalDateTime getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDateTime completedDate) { this.completedDate = completedDate; }
    public LocalDateTime getCancelledDate() { return cancelledDate; }
    public void setCancelledDate(LocalDateTime cancelledDate) { this.cancelledDate = cancelledDate; }
    public LocalDateTime getRefundDate() { return refundDate; }
    public void setRefundDate(LocalDateTime refundDate) { this.refundDate = refundDate; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
} 