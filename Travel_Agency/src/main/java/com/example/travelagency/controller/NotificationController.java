package com.example.travelagency.controller;

import com.example.travelagency.model.Booking;
import com.example.travelagency.model.User;
import com.example.travelagency.repository.BookingRepository;
import com.example.travelagency.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Autowired
    public NotificationController(BookingRepository bookingRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/my")
    public List<NotificationDTO> getMyNotifications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        List<Booking> bookings = bookingRepository.findByUserId(user.getId());
        List<NotificationDTO> notifications = new ArrayList<>();

        for (Booking booking : bookings) {
            if (booking.getTour() == null) continue;

            if (booking.getBookingDate() != null) {
                notifications.add(new NotificationDTO(
                        "✅ Бронирование создано",
                        "Вы успешно забронировали тур '" + booking.getTour().getName() + "'.",
                        booking.getBookingDate()
                ));
            }

            if (booking.getConfirmedDate() != null) {
                notifications.add(new NotificationDTO(
                        "👍 Бронь подтверждена",
                        "Ваша бронь на тур '" + booking.getTour().getName() + "' была подтверждена администратором.",
                        booking.getConfirmedDate()
                ));
            }

            if (booking.getPaymentDate() != null) {
                notifications.add(new NotificationDTO(
                        "💳 Тур оплачен",
                        "Вы успешно оплатили тур '" + booking.getTour().getName() + "'.",
                        booking.getPaymentDate()
                ));
            }

            if (booking.getCancelledDate() != null) {
                notifications.add(new NotificationDTO(
                        "❌ Бронь отменена",
                        "Ваше бронирование на тур '" + booking.getTour().getName() + "' было отменено.",
                        booking.getCancelledDate()
                ));
            }

            if (booking.getRefundDate() != null) {
                notifications.add(new NotificationDTO(
                        "💸 Оформлен возврат",
                        "По вашему бронированию тура '" + booking.getTour().getName() + "' оформлен возврат средств.",
                        booking.getRefundDate()
                ));
            }
            
            if (booking.getCompletedDate() != null) {
                notifications.add(new NotificationDTO(
                        "✈️ Тур завершен",
                        "Ваш тур '" + booking.getTour().getName() + "' завершен. Надеемся, вам понравилось! Оставьте, пожалуйста, отзыв.",
                        booking.getCompletedDate()
                ));
            }
        }

        return notifications.stream()
                .sorted(Comparator.comparing(NotificationDTO::getDate).reversed())
                .collect(Collectors.toList());
    }

    public static class NotificationDTO {
        private final String title;
        private final String message;
        private final LocalDateTime date;

        public NotificationDTO(String title, String message, LocalDateTime date) {
            this.title = title;
            this.message = message;
            this.date = date;
        }

        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public LocalDateTime getDate() { return date; }
    }
} 