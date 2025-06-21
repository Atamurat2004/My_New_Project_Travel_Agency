package com.example.travelagency.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "tours")
public class Tour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String country;
    private String city;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal price;
    private String category;
    @OneToMany(mappedBy = "tour")
    @com.fasterxml.jackson.annotation.JsonManagedReference("tour-booking")
    private List<Booking> bookings;
    @OneToMany(mappedBy = "tour")
    @com.fasterxml.jackson.annotation.JsonManagedReference("tour-review")
    private List<Review> reviews;
    @ElementCollection
    @CollectionTable(name = "tour_images", joinColumns = @JoinColumn(name = "tour_id"))
    private List<String> images;

    public Tour() {}
    
    public Tour(Long id, String name, String description, String country, String city, LocalDate startDate, LocalDate endDate, BigDecimal price, String category, List<Booking> bookings, List<Review> reviews, List<String> images) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.country = country;
        this.city = city;
        this.startDate = startDate;
        this.endDate = endDate;
        this.price = price;
        this.category = category;
        this.bookings = bookings;
        this.reviews = reviews;
        this.images = images;
    }
    
    public static TourBuilder builder() { return new TourBuilder(); }
    
    public static class TourBuilder {
        private Long id;
        private String name;
        private String description;
        private String country;
        private String city;
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal price;
        private String category;
        private List<Booking> bookings;
        private List<Review> reviews;
        private List<String> images;
        
        public TourBuilder id(Long id) { this.id = id; return this; }
        public TourBuilder name(String name) { this.name = name; return this; }
        public TourBuilder description(String description) { this.description = description; return this; }
        public TourBuilder country(String country) { this.country = country; return this; }
        public TourBuilder city(String city) { this.city = city; return this; }
        public TourBuilder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public TourBuilder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
        public TourBuilder price(BigDecimal price) { this.price = price; return this; }
        public TourBuilder category(String category) { this.category = category; return this; }
        public TourBuilder bookings(List<Booking> bookings) { this.bookings = bookings; return this; }
        public TourBuilder reviews(List<Review> reviews) { this.reviews = reviews; return this; }
        public TourBuilder images(List<String> images) { this.images = images; return this; }
        
        public Tour build() { 
            return new Tour(id, name, description, country, city, startDate, endDate, price, category, bookings, reviews, images); 
        }
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public List<Booking> getBookings() { return bookings; }
    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }
    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
} 