package com.example.travelagency.controller;

import com.example.travelagency.model.Booking;
import com.example.travelagency.repository.BookingRepository;
import com.example.travelagency.repository.TourRepository;
import com.example.travelagency.repository.UserRepository;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TourRepository tourRepository;

    @Autowired
    public BookingController(BookingRepository bookingRepository, UserRepository userRepository, TourRepository tourRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.tourRepository = tourRepository;
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestParam Long tourId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            if (email == null || email.isEmpty() || email.equals("anonymousUser")) {
                return ResponseEntity.status(401).body("Пользователь не авторизован");
            }
            var userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Пользователь не найден");
            }
            if (tourId == null) {
                return ResponseEntity.badRequest().body("tourId обязателен");
            }
            var tourOpt = tourRepository.findById(tourId);
            if (tourOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Тур не найден");
            }
            var user = userOpt.get();
            var tour = tourOpt.get();
            // Проверка: нельзя бронировать прошедший тур
            if (tour.getEndDate() != null && tour.getEndDate().isBefore(java.time.LocalDate.now())) {
                return ResponseEntity.badRequest().body("Нельзя бронировать прошедший тур");
            }
            // Проверка: нельзя бронировать один и тот же тур дважды (если статус не CANCELLED)
            var existing = bookingRepository.findByUserId(user.getId()).stream()
                .filter(b -> b.getTour().getId().equals(tourId) && (b.getStatus() == null || !b.getStatus().equalsIgnoreCase("CANCELLED")))
                .findFirst();
            if (existing.isPresent()) {
                return ResponseEntity.badRequest().body("Вы уже бронировали этот тур");
            }
            Booking booking = Booking.builder()
                    .user(user)
                    .tour(tour)
                    .bookingDate(LocalDateTime.now())
                    .status("NEW")
                    .build();
            Booking saved = bookingRepository.save(booking);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Ошибка при бронировании: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public List<Booking> getBookingsByUser(@PathVariable Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    @GetMapping("/my")
    public List<Booking> getMyBookings() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Пользователь не найден");
        }
        var user = userOpt.get();
        return bookingRepository.findByUserId(user.getId());
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        var bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Бронирование не найдено");
        }
        var booking = bookingOpt.get();
        // Только владелец может отменить
        if (!booking.getUser().getEmail().equals(email)) {
            return ResponseEntity.status(403).body("Нет доступа к отмене этого бронирования");
        }
        if (booking.getStatus() != null && booking.getStatus().equalsIgnoreCase("CANCELLED")) {
            return ResponseEntity.badRequest().body("Бронирование уже отменено");
        }
        booking.setStatus("CANCELLED");
        booking.setCancelledDate(LocalDateTime.now());
        bookingRepository.save(booking);
        return ResponseEntity.ok("Бронирование отменено");
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<?> payBooking(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        var bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Бронирование не найдено");
        }
        var booking = bookingOpt.get();
        // Только владелец может оплатить
        if (!booking.getUser().getEmail().equals(email)) {
            return ResponseEntity.status(403).body("Нет доступа к оплате этого бронирования");
        }
        // Проверяем статус
        String currentStatus = booking.getStatus();
        if (currentStatus == null || !currentStatus.equalsIgnoreCase("NEW") && !currentStatus.equalsIgnoreCase("CONFIRMED")) {
             return ResponseEntity.badRequest().body("Оплатить можно только новое или подтвержденное бронирование.");
        }
        booking.setStatus("PAID");
        booking.setPaymentDate(LocalDateTime.now());
        bookingRepository.save(booking);
        return ResponseEntity.ok("Бронирование оплачено");
    }

    // DTO для фронта
    public static class BookingDTO {
        public Long id;
        public String status;
        public String bookingDate;
        public String paymentDate;
        public String refundDate;
        public java.math.BigDecimal refundAmount;
        public TourDTO tour;
        public BookingDTO(Booking b) {
            this.id = b.getId();
            this.status = b.getStatus();
            this.bookingDate = b.getBookingDate() != null ? b.getBookingDate().toString() : null;
            this.paymentDate = b.getPaymentDate() != null ? b.getPaymentDate().toString() : null;
            this.refundDate = b.getRefundDate() != null ? b.getRefundDate().toString() : null;
            this.refundAmount = b.getRefundAmount();
            this.tour = b.getTour() != null ? new TourDTO(b.getTour()) : null;
        }
    }
    public static class TourDTO {
        public Long id;
        public String name;
        public String city;
        public String country;
        public String startDate;
        public String endDate;
        public java.math.BigDecimal price;
        public TourDTO(com.example.travelagency.model.Tour t) {
            this.id = t.getId();
            this.name = t.getName();
            this.city = t.getCity();
            this.country = t.getCountry();
            this.startDate = t.getStartDate() != null ? t.getStartDate().toString() : null;
            this.endDate = t.getEndDate() != null ? t.getEndDate().toString() : null;
            this.price = t.getPrice();
        }
    }

    @GetMapping("/my-dto")
    public List<BookingDTO> getMyBookingsDto() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Пользователь не найден");
        }
        var user = userOpt.get();
        return bookingRepository.findByUserId(user.getId())
                .stream().map(BookingDTO::new).toList();
    }
} 