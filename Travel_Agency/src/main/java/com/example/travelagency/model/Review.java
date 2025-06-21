package com.example.travelagency.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference("user-review")
    private User user;
    @ManyToOne
    @JoinColumn(name = "tour_id")
    @JsonBackReference("tour-review")
    private Tour tour;
    private int rating;
    private String comment;

    public Review() {}
    
    public Review(Long id, User user, Tour tour, int rating, String comment) {
        this.id = id;
        this.user = user;
        this.tour = tour;
        this.rating = rating;
        this.comment = comment;
    }
    
    public static ReviewBuilder builder() { return new ReviewBuilder(); }
    
    public static class ReviewBuilder {
        private Long id;
        private User user;
        private Tour tour;
        private int rating;
        private String comment;
        
        public ReviewBuilder id(Long id) { this.id = id; return this; }
        public ReviewBuilder user(User user) { this.user = user; return this; }
        public ReviewBuilder tour(Tour tour) { this.tour = tour; return this; }
        public ReviewBuilder rating(int rating) { this.rating = rating; return this; }
        public ReviewBuilder comment(String comment) { this.comment = comment; return this; }
        
        public Review build() { 
            return new Review(id, user, tour, rating, comment); 
        }
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Tour getTour() { return tour; }
    public void setTour(Tour tour) { this.tour = tour; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
} 