package com.example.travelagency.controller;

import com.example.travelagency.model.Review;
import com.example.travelagency.model.Tour;
import com.example.travelagency.model.User;
import com.example.travelagency.repository.ReviewRepository;
import com.example.travelagency.repository.TourRepository;
import com.example.travelagency.repository.UserRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final TourRepository tourRepository;

    @Autowired
    public ReviewController(ReviewRepository reviewRepository, UserRepository userRepository, TourRepository tourRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.tourRepository = tourRepository;
    }

    @PostMapping
    public ResponseEntity<Review> addReview(@RequestBody AddReviewRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Tour tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new RuntimeException("Тур не найден"));

        // Проверка, оставлял ли пользователь уже отзыв на этот тур
        boolean alreadyReviewed = reviewRepository.findByUserId(user.getId())
            .stream().anyMatch(review -> review.getTour().getId().equals(request.getTourId()));
        if(alreadyReviewed) {
            return ResponseEntity.badRequest().body(null); // Или бросить исключение с сообщением
        }
        
        Review review = Review.builder()
                .user(user)
                .tour(tour)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
        return ResponseEntity.ok(reviewRepository.save(review));
    }

    @GetMapping("/tour/{tourId}")
    public List<Review> getReviewsByTour(@PathVariable Long tourId) {
        return reviewRepository.findByTourId(tourId);
    }

    @GetMapping("/my")
    public List<ReviewDTO> getMyReviews() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return reviewRepository.findByUserId(user.getId())
                .stream().map(ReviewDTO::new).collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewDTO> updateReview(@PathVariable Long id, @RequestBody UpdateReviewRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Отзыв не найден"));

        if (!review.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).body(null);
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        Review updatedReview = reviewRepository.save(review);

        return ResponseEntity.ok(new ReviewDTO(updatedReview));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Отзыв не найден"));

        if (!review.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }

        reviewRepository.delete(review);
        return ResponseEntity.ok().build();
    }

    // DTO для запроса на добавление отзыва
    public static class AddReviewRequest {
        private Long tourId;
        private int rating;
        private String comment;
        public Long getTourId() { return tourId; }
        public int getRating() { return rating; }
        public String getComment() { return comment; }
    }
    
    // DTO для запроса на обновление отзыва
    public static class UpdateReviewRequest {
        private int rating;
        private String comment;
        public int getRating() { return rating; }
        public String getComment() { return comment; }
    }

    // DTO для отображения отзыва
    public static class ReviewDTO {
        public Long id;
        public int rating;
        public String comment;
        public TourDTO tour;

        public ReviewDTO(Review review) {
            this.id = review.getId();
            this.rating = review.getRating();
            this.comment = review.getComment();
            this.tour = new TourDTO(review.getTour());
        }
    }

    public static class TourDTO {
        public String name;
        public String city;
        public String country;

        public TourDTO(Tour tour) {
            if (tour != null) {
                this.name = tour.getName();
                this.city = tour.getCity();
                this.country = tour.getCountry();
            }
        }
    }
} 