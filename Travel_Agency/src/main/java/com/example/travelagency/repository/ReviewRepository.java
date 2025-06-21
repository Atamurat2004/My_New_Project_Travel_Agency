package com.example.travelagency.repository;

import com.example.travelagency.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByTourId(Long tourId);
    List<Review> findByUserId(Long userId);
} 