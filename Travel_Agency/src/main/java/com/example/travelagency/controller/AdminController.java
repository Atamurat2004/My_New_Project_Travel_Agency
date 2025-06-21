package com.example.travelagency.controller;

import com.example.travelagency.model.Tour;
import com.example.travelagency.model.User;
import com.example.travelagency.model.Booking;
import com.example.travelagency.model.SupportTicket;
import com.example.travelagency.repository.TourRepository;
import com.example.travelagency.repository.UserRepository;
import com.example.travelagency.repository.BookingRepository;
import com.example.travelagency.repository.SupportTicketRepository;
import com.example.travelagency.service.SupportService;
import com.example.travelagency.model.SupportMessage;
import com.example.travelagency.repository.ReviewRepository;
import com.example.travelagency.repository.SupportMessageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.stream.Collectors;
import com.example.travelagency.service.AuditLogService;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final AuditLogService auditLogService;
    private final SupportTicketRepository supportTicketRepository;
    private final SupportService supportService;
    private final ReviewRepository reviewRepository;
    private final SupportMessageRepository supportMessageRepository;

    @Autowired
    public AdminController(TourRepository tourRepository, UserRepository userRepository, BookingRepository bookingRepository, AuditLogService auditLogService, SupportTicketRepository supportTicketRepository, SupportService supportService, ReviewRepository reviewRepository, SupportMessageRepository supportMessageRepository) {
        this.tourRepository = tourRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.auditLogService = auditLogService;
        this.supportTicketRepository = supportTicketRepository;
        this.supportService = supportService;
        this.reviewRepository = reviewRepository;
        this.supportMessageRepository = supportMessageRepository;
    }

    @PostMapping("/tours")
    public Map<String, String> createTour(@RequestBody Tour tour) {
        logger.info("Creating new tour: {}", tour.getName());
        Tour savedTour = tourRepository.save(tour);
        auditLogService.logEntity("TOUR", savedTour.getId(), "CREATE", savedTour);
        logger.info("Tour created with ID: {}", savedTour.getId());
        return Map.of("message", "Tour created successfully", "id", savedTour.getId().toString());
    }

    @PostMapping("/tours/upload")
    public Map<String, String> createTourWithImages(
            @RequestPart("tour") Tour tour,
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            HttpServletRequest request) {
        logger.info("UPLOAD: tour={}, images={}, authHeader={}", tour, images != null ? images.length : 0, request.getHeader("Authorization"));
        try {
            List<String> imagePaths = new ArrayList<>();
            if (images != null) {
                Path uploadDir = Paths.get("../Frontend/public/assets/images");
                if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);
                for (MultipartFile file : images) {
                    if (!file.isEmpty()) {
                        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                        Path filePath = uploadDir.resolve(filename);
                        Files.copy(file.getInputStream(), filePath);
                        imagePaths.add("/assets/images/" + filename);
                    }
                }
            }
            tour.setImages(imagePaths);
            Tour savedTour = tourRepository.save(tour);
            auditLogService.logEntity("TOUR", savedTour.getId(), "CREATE", savedTour);
            logger.info("Tour created with ID: {} (with images)", savedTour.getId());
            return Map.of("message", "Tour with images created successfully", "id", savedTour.getId().toString());
        } catch (Exception e) {
            logger.error("Error creating tour with images", e);
            throw new RuntimeException("Ошибка при создании тура с изображениями: " + e.getMessage());
        }
    }

    @PutMapping(value = "/tours/{id}", consumes = {"multipart/form-data"})
    public Map<String, String> updateTourWithImages(
            @PathVariable Long id,
            @RequestPart("tour") Tour tour,
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            @RequestPart(value = "existingImages", required = false) String existingImagesJson
    ) {
        try {
            if (!tourRepository.existsById(id)) {
                throw new RuntimeException("Tour not found");
            }
            Tour existing = tourRepository.findById(id).orElseThrow();
            // Обновляем основные поля
            existing.setName(tour.getName());
            existing.setDescription(tour.getDescription());
            existing.setCountry(tour.getCountry());
            existing.setCity(tour.getCity());
            existing.setStartDate(tour.getStartDate());
            existing.setEndDate(tour.getEndDate());
            existing.setPrice(tour.getPrice());
            existing.setCategory(tour.getCategory());
            // Работа с изображениями
            ObjectMapper mapper = new ObjectMapper();
            List<String> imagesToKeep = existing.getImages() != null ? new ArrayList<>(existing.getImages()) : new ArrayList<>();
            if (existingImagesJson != null) {
                imagesToKeep = mapper.readValue(existingImagesJson, new TypeReference<List<String>>(){});
            }
            // (опционально) удалить физически файлы, которые были удалены из imagesToKeep
            List<String> oldImages = existing.getImages() != null ? existing.getImages() : new ArrayList<>();
            for (String oldImg : oldImages) {
                if (!imagesToKeep.contains(oldImg)) {
                    try {
                        Path p = Paths.get("../Frontend/public" + oldImg);
                        Files.deleteIfExists(p);
                    } catch (Exception ignore) {}
                }
            }
            // Добавляем новые изображения
            if (images != null) {
                Path uploadDir = Paths.get("../Frontend/public/assets/images");
                if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);
                for (MultipartFile file : images) {
                    if (!file.isEmpty()) {
                        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                        Path filePath = uploadDir.resolve(filename);
                        Files.copy(file.getInputStream(), filePath);
                        imagesToKeep.add("/assets/images/" + filename);
                    }
                }
            }
            existing.setImages(imagesToKeep);
            Tour updated = tourRepository.save(existing);
            auditLogService.logEntity("TOUR", updated.getId(), "UPDATE", updated);
            return Map.of("message", "Tour updated successfully");
        } catch (Exception e) {
            logger.error("Error updating tour with images", e);
            throw new RuntimeException("Ошибка при обновлении тура: " + e.getMessage());
        }
    }

    @DeleteMapping("/tours/{id}")
    public Map<String, String> deleteTour(@PathVariable Long id) {
        logger.info("Deleting tour with ID: {}", id);
        if (!tourRepository.existsById(id)) {
            throw new RuntimeException("Tour not found");
        }
        tourRepository.deleteById(id);
        return Map.of("message", "Tour deleted successfully");
    }

    @GetMapping("/bookings")
    public List<BookingAdminDTO> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(BookingAdminDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsers() {
        logger.info("Getting all users");
        return userRepository.findAll().stream()
                .map(user -> {
                    Map<String, Object> userMap = Map.of(
                        "id", user.getId(),
                        "name", user.getName(),
                        "email", user.getEmail(),
                        "role", user.getRole()
                    );
                    return userMap;
                })
                .collect(Collectors.toList());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        logger.info("Deleting user with ID: {}", id);

        if (!bookingRepository.findByUserId(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Нельзя удалить пользователя, у которого есть бронирования."));
        }
        if (!reviewRepository.findByUserId(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Нельзя удалить пользователя, который оставлял отзывы."));
        }
        if (!supportTicketRepository.findByUserIdOrderByCreatedAtDesc(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Нельзя удалить пользователя, у которого есть обращения в поддержку."));
        }

        if (!userRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        }

        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    @DeleteMapping("/users/{id}/force")
    @Transactional
    public ResponseEntity<Map<String, String>> forceDeleteUser(@PathVariable Long id) {
        logger.warn("FORCE DELETING user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Отвязываем бронирования
        bookingRepository.findByUserId(id).forEach(booking -> {
            booking.setUser(null);
            bookingRepository.save(booking);
        });

        // Отвязываем отзывы
        reviewRepository.findByUserId(id).forEach(review -> {
            review.setUser(null);
            reviewRepository.save(review);
        });

        // Отвязываем тикеты поддержки
        supportTicketRepository.findByUserIdOrderByCreatedAtDesc(id).forEach(ticket -> {
            ticket.setUser(null);
            supportTicketRepository.save(ticket);
        });
        
        // Отвязываем сообщения поддержки
        supportMessageRepository.findBySenderId(id).forEach(message -> {
            message.setSender(null);
            supportMessageRepository.save(message);
        });

        userRepository.delete(user);

        return ResponseEntity.ok(Map.of("message", "Пользователь был принудительно удален."));
    }

    @PutMapping("/users/{id}/role")
    public Map<String, String> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> request) {
        logger.info("Updating role for user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(request.get("role"));
        userRepository.save(user);
        return Map.of("message", "User role updated successfully");
    }

    @PostMapping("/test")
    public String test() {
        logger.info("TEST endpoint called");
        return "OK";
    }

    @PutMapping("/bookings/{id}/status")
    public Map<String, String> updateBookingStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new RuntimeException("Booking not found"));
        String status = request.get("status");
        if (status == null || status.isEmpty()) throw new RuntimeException("Status is required");

        if ("CONFIRMED".equalsIgnoreCase(status)) {
            booking.setConfirmedDate(LocalDateTime.now());
        } else if ("COMPLETED".equalsIgnoreCase(status)) {
            booking.setCompletedDate(LocalDateTime.now());
        }
        
        booking.setStatus(status);
        bookingRepository.save(booking);
        return Map.of("message", "Booking status updated successfully");
    }

    @PutMapping("/bookings/{id}/cancel")
    public Map<String, String> adminCancelBooking(@PathVariable Long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new RuntimeException("Booking not found"));
        if (booking.getStatus() != null && booking.getStatus().equalsIgnoreCase("CANCELLED")) {
            throw new RuntimeException("Booking already cancelled");
        }

        if ("PAID".equalsIgnoreCase(booking.getStatus())) {
            booking.setRefundDate(LocalDateTime.now());
            booking.setRefundAmount(booking.getTour().getPrice());
        }

        booking.setStatus("CANCELLED");
        booking.setCancelledDate(LocalDateTime.now());
        bookingRepository.save(booking);
        return Map.of("message", "Booking cancelled by admin");
    }

    @GetMapping("/support-tickets")
    public List<SupportTicketDTO> getAllSupportTickets() {
        return supportTicketRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(SupportTicketDTO::new)
                .collect(Collectors.toList());
    }

    @PutMapping("/support-tickets/{id}/status")
    public SupportTicketDTO updateSupportTicketStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String status = request.get("status");
        SupportTicket updatedTicket = supportService.updateTicketStatus(id, status, "Статус обновлен администратором.");
        return new SupportTicketDTO(updatedTicket);
    }

    @PostMapping("/support-tickets/{id}/message")
    public SupportMessage sendAdminReply(@PathVariable Long id, @RequestBody Map<String, String> request, Authentication authentication) {
        String message = request.get("message");
        String adminEmail = authentication.getName();
        User adminUser = userRepository.findByEmail(adminEmail).orElseThrow(() -> new RuntimeException("Admin user not found"));
        return supportService.sendMessage(id, adminUser.getId(), message, "ADMIN");
    }

    // DTOs for Admin Panel
    public static class UserAdminDTO {
        public Long id;
        public String name;
        public String email;
        public UserAdminDTO(User user) {
            if (user != null) {
                this.id = user.getId();
                this.name = user.getName();
                this.email = user.getEmail();
            } else {
                this.id = null;
                this.name = "Неизвестный пользователь";
                this.email = "";
            }
        }
    }

    public static class TourAdminDTO {
        public Long id;
        public String name;
        public String startDate;
        public String endDate;

        public TourAdminDTO(Tour tour) {
            if (tour != null) {
                this.id = tour.getId();
                this.name = tour.getName();
                this.startDate = tour.getStartDate() != null ? tour.getStartDate().toString() : null;
                this.endDate = tour.getEndDate() != null ? tour.getEndDate().toString() : null;
            }
        }
    }

    public static class BookingAdminDTO {
        public Long id;
        public String status;
        public UserAdminDTO user;
        public TourAdminDTO tour;

        public BookingAdminDTO(Booking booking) {
            this.id = booking.getId();
            this.status = booking.getStatus();
            this.user = new UserAdminDTO(booking.getUser());
            this.tour = new TourAdminDTO(booking.getTour());
        }
    }

    public static class SupportTicketDTO {
        public Long id;
        public String subject;
        public String description;
        public String status;
        public String ticketType;
        public LocalDateTime createdAt;
        public UserAdminDTO user;
        public List<SupportMessageDTO> messages;


        public SupportTicketDTO(SupportTicket ticket) {
            this.id = ticket.getId();
            this.subject = ticket.getSubject();
            this.description = ticket.getDescription();
            this.status = ticket.getStatus();
            this.ticketType = ticket.getTicketType();
            this.createdAt = ticket.getCreatedAt();
            this.user = new UserAdminDTO(ticket.getUser());
            if (ticket.getMessages() != null) {
                this.messages = ticket.getMessages().stream()
                        .map(SupportMessageDTO::new)
                        .collect(Collectors.toList());
            }
        }
    }

    public static class SupportMessageDTO {
        public Long id;
        public String message;
        public LocalDateTime sentAt;
        public String messageType;
        public UserAdminDTO sender;

        public SupportMessageDTO(SupportMessage message) {
            this.id = message.getId();
            this.message = message.getMessage();
            this.sentAt = message.getSentAt();
            this.messageType = message.getMessageType();
            this.sender = new UserAdminDTO(message.getSender());
        }
    }
} 