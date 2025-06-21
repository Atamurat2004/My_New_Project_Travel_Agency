package com.example.travelagency.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "support_messages")
public class SupportMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "support_ticket_id")
    @JsonBackReference
    private SupportTicket supportTicket;
    
    @ManyToOne
    @JoinColumn(name = "sender_id")
    @JsonBackReference("user-sent-messages")
    private User sender;
    
    private String message;
    private LocalDateTime sentAt;
    private String messageType; // USER, ADMIN, SYSTEM
    private Boolean isRead;

    public SupportMessage() {}

    public SupportMessage(Long id, SupportTicket supportTicket, User sender, String message, 
                         LocalDateTime sentAt, String messageType, Boolean isRead) {
        this.id = id;
        this.supportTicket = supportTicket;
        this.sender = sender;
        this.message = message;
        this.sentAt = sentAt;
        this.messageType = messageType;
        this.isRead = isRead;
    }

    public static SupportMessageBuilder builder() {
        return new SupportMessageBuilder();
    }

    public static class SupportMessageBuilder {
        private Long id;
        private SupportTicket supportTicket;
        private User sender;
        private String message;
        private LocalDateTime sentAt;
        private String messageType;
        private Boolean isRead;

        public SupportMessageBuilder id(Long id) { this.id = id; return this; }
        public SupportMessageBuilder supportTicket(SupportTicket supportTicket) { this.supportTicket = supportTicket; return this; }
        public SupportMessageBuilder sender(User sender) { this.sender = sender; return this; }
        public SupportMessageBuilder message(String message) { this.message = message; return this; }
        public SupportMessageBuilder sentAt(LocalDateTime sentAt) { this.sentAt = sentAt; return this; }
        public SupportMessageBuilder messageType(String messageType) { this.messageType = messageType; return this; }
        public SupportMessageBuilder isRead(Boolean isRead) { this.isRead = isRead; return this; }

        public SupportMessage build() {
            return new SupportMessage(id, supportTicket, sender, message, sentAt, messageType, isRead);
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public SupportTicket getSupportTicket() { return supportTicket; }
    public void setSupportTicket(SupportTicket supportTicket) { this.supportTicket = supportTicket; }
    
    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    
    // Поле для совместимости с фронтендом
    public LocalDateTime getDate() { return sentAt; }
    public void setDate(LocalDateTime date) { this.sentAt = date; }
    
    // Поле для фронтенда - определяет, является ли отправитель пользователем
    public Boolean getIsUser() { 
        return sender != null && "USER".equals(messageType); 
    }
    public void setIsUser(Boolean isUser) { 
        // Это поле только для чтения, сеттер не нужен
    }
} 