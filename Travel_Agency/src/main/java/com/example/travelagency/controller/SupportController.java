package com.example.travelagency.controller;

import com.example.travelagency.model.SupportTicket;
import com.example.travelagency.model.SupportMessage;
import com.example.travelagency.model.User;
import com.example.travelagency.repository.UserRepository;
import com.example.travelagency.service.SupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/support")
@CrossOrigin(origins = "*")
public class SupportController {
    
    @Autowired
    private SupportService supportService;
    
    @Autowired
    private UserRepository userRepository;

    // Создание нового тикета поддержки
    @PostMapping("/tickets")
    public ResponseEntity<SupportTicket> createTicket(@RequestBody CreateTicketRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        SupportTicket ticket = supportService.createSupportTicket(
                user.getId(), 
                request.getBookingId(), 
                request.getSubject(), 
                request.getDescription(), 
                request.getTicketType()
        );
        
        return ResponseEntity.ok(ticket);
    }

    // Создание нового обращения (альтернативный endpoint для фронтенда)
    @PostMapping
    public ResponseEntity<SupportTicket> createSupportRequest(@RequestBody CreateSupportRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        SupportTicket ticket = supportService.createSupportTicket(
                user.getId(), 
                request.getBookingId(), // bookingId может быть null
                request.getSubject(), 
                request.getMessage(), 
                "general"
        );
        
        return ResponseEntity.ok(ticket);
    }

    // Получение тикетов пользователя
    @GetMapping("/tickets/my")
    public ResponseEntity<List<SupportTicket>> getMyTickets() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        List<SupportTicket> tickets = supportService.getUserTickets(user.getId());
        return ResponseEntity.ok(tickets);
    }

    // Получение обращений пользователя (альтернативный endpoint для фронтенда)
    @GetMapping("/my")
    public ResponseEntity<List<SupportTicketDTO>> getMySupportRequests() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        List<SupportTicket> tickets = supportService.getUserTickets(user.getId());
        List<SupportTicketDTO> dtos = tickets.stream().map(SupportTicketDTO::new).toList();
        return ResponseEntity.ok(dtos);
    }

    // Получение деталей тикета
    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<SupportTicket> getTicketDetails(@PathVariable Long ticketId) {
        SupportTicket ticket = supportService.getTicketById(ticketId);
        return ResponseEntity.ok(ticket);
    }

    // Получение деталей обращения (альтернативный endpoint для фронтенда)
    @GetMapping("/{id}")
    public ResponseEntity<SupportTicketDTO> getSupportRequestDetails(@PathVariable Long id) {
        SupportTicket ticket = supportService.getTicketById(id);
        return ResponseEntity.ok(new SupportTicketDTO(ticket));
    }

    // Получение сообщений тикета
    @GetMapping("/tickets/{ticketId}/messages")
    public ResponseEntity<List<SupportMessage>> getTicketMessages(@PathVariable Long ticketId) {
        List<SupportMessage> messages = supportService.getTicketMessages(ticketId);
        return ResponseEntity.ok(messages);
    }

    // Получение сообщений обращения (альтернативный endpoint для фронтенда)
    @GetMapping("/{id}/messages")
    public ResponseEntity<List<SupportMessageDTO>> getSupportRequestMessages(@PathVariable Long id) {
        List<SupportMessage> messages = supportService.getTicketMessages(id);
        List<SupportMessageDTO> dtos = messages.stream().map(SupportMessageDTO::new).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Отправка ответа к обращению (альтернативный endpoint для фронтенда)
    @PostMapping("/{id}/reply")
    public ResponseEntity<SupportMessage> sendSupportReply(@PathVariable Long id, 
                                                          @RequestBody SendReplyRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        SupportMessage message = supportService.sendMessage(
                id, 
                user.getId(), 
                request.getMessage(), 
                "USER"
        );
        
        return ResponseEntity.ok(message);
    }

    // Обновление статуса тикета (для админа)
    @PutMapping("/tickets/{ticketId}/status")
    public ResponseEntity<SupportTicket> updateTicketStatus(@PathVariable Long ticketId, 
                                                           @RequestBody UpdateStatusRequest request) {
        SupportTicket ticket = supportService.updateTicketStatus(
                ticketId, 
                request.getStatus(), 
                request.getAdminNotes()
        );
        
        return ResponseEntity.ok(ticket);
    }

    // Запрос на возврат средств
    @PostMapping("/tickets/{ticketId}/refund-request")
    public ResponseEntity<SupportTicket> requestRefund(@PathVariable Long ticketId, 
                                                      @RequestBody RefundRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        SupportTicket ticket = supportService.requestRefund(ticketId, request.getReason());
        return ResponseEntity.ok(ticket);
    }

    // Одобрение возврата средств (для админа)
    @PostMapping("/tickets/{ticketId}/refund-approve")
    public ResponseEntity<SupportTicket> approveRefund(@PathVariable Long ticketId, 
                                                      @RequestBody RefundApprovalRequest request) {
        SupportTicket ticket = supportService.approveRefund(ticketId, request.getAdminNotes());
        return ResponseEntity.ok(ticket);
    }

    // Отклонение возврата средств (для админа)
    @PostMapping("/tickets/{ticketId}/refund-reject")
    public ResponseEntity<SupportTicket> rejectRefund(@PathVariable Long ticketId, 
                                                     @RequestBody RefundRejectionRequest request) {
        SupportTicket ticket = supportService.rejectRefund(ticketId, request.getReason());
        return ResponseEntity.ok(ticket);
    }

    // Получение статистики поддержки (для админа)
    @GetMapping("/statistics")
    public ResponseEntity<SupportService.SupportStatistics> getStatistics() {
        // Проверка прав администратора выполняется в SecurityConfig
        SupportService.SupportStatistics statistics = supportService.getSupportStatistics();
        return ResponseEntity.ok(statistics);
    }

    // DTO классы для запросов
    public static class CreateTicketRequest {
        private Long bookingId;
        private String subject;
        private String description;
        private String ticketType;

        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getTicketType() { return ticketType; }
        public void setTicketType(String ticketType) { this.ticketType = ticketType; }
    }

    public static class CreateSupportRequest {
        private String subject;
        private String message;
        private Long bookingId;

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    }

    public static class SendMessageRequest {
        private String message;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class UpdateStatusRequest {
        private String status;
        private String adminNotes;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getAdminNotes() { return adminNotes; }
        public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
    }

    public static class RefundRequest {
        private String reason;

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class RefundApprovalRequest {
        private String adminNotes;

        public String getAdminNotes() { return adminNotes; }
        public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
    }

    public static class RefundRejectionRequest {
        private String reason;

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class SendReplyRequest {
        private String message;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    // DTO для сообщений (чтобы избежать циклов JSON)
    public static class SupportMessageDTO {
        public Long id;
        public String message;
        public LocalDateTime sentAt;
        public String messageType;
        public UserDTO sender;

        public SupportMessageDTO(SupportMessage message) {
            this.id = message.getId();
            this.message = message.getMessage();
            this.sentAt = message.getSentAt();
            this.messageType = message.getMessageType();
            
            if (message.getSender() != null) {
                this.sender = new UserDTO(message.getSender());
            } else {
                // Для системных сообщений, где отправитель null
                this.sender = new UserDTO(null);
                this.sender.name = "Система";
            }
        }
    }

    // DTO для передачи данных на фронтенд
    public static class SupportTicketDTO {
        public Long id;
        public String subject;
        public String description;
        public String status;
        public String ticketType;
        public LocalDateTime createdAt;
        public UserDTO user;
        public BookingController.BookingDTO booking;

        public SupportTicketDTO(SupportTicket ticket) {
            this.id = ticket.getId();
            this.subject = ticket.getSubject();
            this.description = ticket.getDescription();
            this.status = ticket.getStatus();
            this.ticketType = ticket.getTicketType();
            this.createdAt = ticket.getCreatedAt();
            if (ticket.getUser() != null) {
                this.user = new UserDTO(ticket.getUser());
            }
            if (ticket.getBooking() != null) {
                this.booking = new BookingController.BookingDTO(ticket.getBooking());
            }
        }

        public LocalDateTime getDate() {
            return createdAt;
        }
    }
    
    public static class UserDTO {
        public String name;
        public String email;
        
        public UserDTO(User user) {
            if (user != null) {
                this.name = user.getName();
                this.email = user.getEmail();
            }
        }
    }
} 