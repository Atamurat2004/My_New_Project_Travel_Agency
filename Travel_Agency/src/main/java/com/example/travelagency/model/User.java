package com.example.travelagency.model;

import jakarta.persistence.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(unique = true, nullable = false)
    private String email;
    private String password;
    private String role; // USER, ADMIN
    @OneToMany(mappedBy = "user")
    @JsonManagedReference("user-booking")
    private List<Booking> bookings;
    @OneToMany(mappedBy = "user")
    @JsonManagedReference("user-review")
    private List<Review> reviews;
    private String phone;
    private String birthDate;
    private String passport;
    private String notifications;

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-sent-messages")
    private List<SupportMessage> sentMessages;

    public User() {}
    
    public User(Long id, String name, String email, String password, String role, String phone, String birthDate, String passport, String notifications, List<Booking> bookings, List<Review> reviews, List<SupportMessage> sentMessages) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.phone = phone;
        this.birthDate = birthDate;
        this.passport = passport;
        this.notifications = notifications;
        this.bookings = bookings;
        this.reviews = reviews;
        this.sentMessages = sentMessages;
    }
    
    public static UserBuilder builder() { return new UserBuilder(); }
    
    public static class UserBuilder {
        private Long id;
        private String name;
        private String email;
        private String password;
        private String role;
        private List<Booking> bookings;
        private List<Review> reviews;
        private String phone;
        private String birthDate;
        private String passport;
        private String notifications;
        private List<SupportMessage> sentMessages;
        
        public UserBuilder id(Long id) { this.id = id; return this; }
        public UserBuilder name(String name) { this.name = name; return this; }
        public UserBuilder email(String email) { this.email = email; return this; }
        public UserBuilder password(String password) { this.password = password; return this; }
        public UserBuilder role(String role) { this.role = role; return this; }
        public UserBuilder bookings(List<Booking> bookings) { this.bookings = bookings; return this; }
        public UserBuilder reviews(List<Review> reviews) { this.reviews = reviews; return this; }
        public UserBuilder phone(String phone) { this.phone = phone; return this; }
        public UserBuilder birthDate(String birthDate) { this.birthDate = birthDate; return this; }
        public UserBuilder passport(String passport) { this.passport = passport; return this; }
        public UserBuilder notifications(String notifications) { this.notifications = notifications; return this; }
        public UserBuilder sentMessages(List<SupportMessage> sentMessages) { this.sentMessages = sentMessages; return this; }
        
        public User build() { 
            return new User(id, name, email, password, role, phone, birthDate, passport, notifications, bookings, reviews, sentMessages); 
        }
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public List<Booking> getBookings() { return bookings; }
    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }
    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    public String getPassport() { return passport; }
    public void setPassport(String passport) { this.passport = passport; }
    public String getNotifications() { return notifications; }
    public void setNotifications(String notifications) { this.notifications = notifications; }
    public List<SupportMessage> getSentMessages() { return sentMessages; }
    public void setSentMessages(List<SupportMessage> sentMessages) { this.sentMessages = sentMessages; }
} 