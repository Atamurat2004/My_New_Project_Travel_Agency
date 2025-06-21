package com.example.travelagency.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "support_tickets")
public class SupportTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;
    
    private String subject;
    private String description;
    private String status; // OPEN, IN_PROGRESS, RESOLVED, CANCELLED
    private String ticketType; // GENERAL, TOUR_CANCELLATION, PAYMENT_ISSUE, COMPLAINT
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    
    @OneToMany(mappedBy = "supportTicket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<SupportMessage> messages;
    
    private String adminNotes;
    private Boolean refundRequested;
    private Boolean refundApproved;
    private LocalDateTime refundDate;

    public SupportTicket() {}

    public SupportTicket(Long id, User user, Booking booking, String subject, String description, 
                        String status, String ticketType, LocalDateTime createdAt, LocalDateTime updatedAt, 
                        LocalDateTime resolvedAt, List<SupportMessage> messages, String adminNotes, 
                        Boolean refundRequested, Boolean refundApproved, LocalDateTime refundDate) {
        this.id = id;
        this.user = user;
        this.booking = booking;
        this.subject = subject;
        this.description = description;
        this.status = status;
        this.ticketType = ticketType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.resolvedAt = resolvedAt;
        this.messages = messages;
        this.adminNotes = adminNotes;
        this.refundRequested = refundRequested;
        this.refundApproved = refundApproved;
        this.refundDate = refundDate;
    }

    public static SupportTicketBuilder builder() {
        return new SupportTicketBuilder();
    }

    public static class SupportTicketBuilder {
        private Long id;
        private User user;
        private Booking booking;
        private String subject;
        private String description;
        private String status;
        private String ticketType;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime resolvedAt;
        private List<SupportMessage> messages;
        private String adminNotes;
        private Boolean refundRequested;
        private Boolean refundApproved;
        private LocalDateTime refundDate;

        public SupportTicketBuilder id(Long id) { this.id = id; return this; }
        public SupportTicketBuilder user(User user) { this.user = user; return this; }
        public SupportTicketBuilder booking(Booking booking) { this.booking = booking; return this; }
        public SupportTicketBuilder subject(String subject) { this.subject = subject; return this; }
        public SupportTicketBuilder description(String description) { this.description = description; return this; }
        public SupportTicketBuilder status(String status) { this.status = status; return this; }
        public SupportTicketBuilder ticketType(String ticketType) { this.ticketType = ticketType; return this; }
        public SupportTicketBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public SupportTicketBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public SupportTicketBuilder resolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; return this; }
        public SupportTicketBuilder messages(List<SupportMessage> messages) { this.messages = messages; return this; }
        public SupportTicketBuilder adminNotes(String adminNotes) { this.adminNotes = adminNotes; return this; }
        public SupportTicketBuilder refundRequested(Boolean refundRequested) { this.refundRequested = refundRequested; return this; }
        public SupportTicketBuilder refundApproved(Boolean refundApproved) { this.refundApproved = refundApproved; return this; }
        public SupportTicketBuilder refundDate(LocalDateTime refundDate) { this.refundDate = refundDate; return this; }

        public SupportTicket build() {
            return new SupportTicket(id, user, booking, subject, description, status, ticketType, 
                                   createdAt, updatedAt, resolvedAt, messages, adminNotes, 
                                   refundRequested, refundApproved, refundDate);
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getTicketType() { return ticketType; }
    public void setTicketType(String ticketType) { this.ticketType = ticketType; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    
    public List<SupportMessage> getMessages() { return messages; }
    public void setMessages(List<SupportMessage> messages) { this.messages = messages; }
    
    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
    
    public Boolean getRefundRequested() { return refundRequested; }
    public void setRefundRequested(Boolean refundRequested) { this.refundRequested = refundRequested; }
    
    public Boolean getRefundApproved() { return refundApproved; }
    public void setRefundApproved(Boolean refundApproved) { this.refundApproved = refundApproved; }
    
    public LocalDateTime getRefundDate() { return refundDate; }
    public void setRefundDate(LocalDateTime refundDate) { this.refundDate = refundDate; }
    
    // Поле для совместимости с фронтендом
    public LocalDateTime getDate() { return createdAt; }
    public void setDate(LocalDateTime date) { this.createdAt = date; }
} 