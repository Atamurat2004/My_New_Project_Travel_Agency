package com.example.travelagency.repository;

import com.example.travelagency.model.Tour;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TourRepository extends JpaRepository<Tour, Long> {
} 