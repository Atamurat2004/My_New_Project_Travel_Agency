package com.example.travelagency.controller;

import com.example.travelagency.model.Tour;
import com.example.travelagency.repository.TourRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api/tours")
public class TourController {
    private final TourRepository tourRepository;

    @Autowired
    public TourController(TourRepository tourRepository) {
        this.tourRepository = tourRepository;
    }

    @GetMapping
    public List<Tour> getAllTours() {
        return tourRepository.findAll();
    }

    @GetMapping("/{id}")
    public Tour getTourById(@PathVariable Long id) {
        return tourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tour not found"));
    }
} 