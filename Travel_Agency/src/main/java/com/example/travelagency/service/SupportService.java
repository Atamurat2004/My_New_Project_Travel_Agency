package com.example.travelagency.service;

import com.example.travelagency.model.*;
import com.example.travelagency.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SupportService {
    
    @Autowired
    private SupportTicketRepository supportTicketRepository;
    
    @Autowired
    private SupportMessageRepository supportMessageRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AuditLogService auditLogService;

    // Создание нового тикета поддержки
    @Transactional
    public SupportTicket createSupportTicket(Long userId, Long bookingId, String subject, 
                                           String description, String ticketType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        Booking booking = null;
        if (bookingId != null) {
            booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Бронирование не найдено"));
        }
        
        SupportTicket ticket = SupportTicket.builder()
                .user(user)
                .booking(booking)
                .subject(subject)
                .description(description)
                .status("OPEN")
                .ticketType(ticketType)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .refundRequested(false)
                .refundApproved(false)
                .build();
        
        SupportTicket savedTicket = supportTicketRepository.save(ticket);
        
        // Логируем создание тикета
        auditLogService.logEntity("SupportTicket", savedTicket.getId(), "CREATE", savedTicket);
        
        return savedTicket;
    }

    // Отправка сообщения в тикет
    @Transactional
    public SupportMessage sendMessage(Long ticketId, Long senderId, String message, String messageType) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Тикет не найден"));
        
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Отправитель не найден"));
        
        SupportMessage supportMessage = SupportMessage.builder()
                .supportTicket(ticket)
                .sender(sender)
                .message(message)
                .sentAt(LocalDateTime.now())
                .messageType(messageType)
                .isRead(false)
                .build();
        
        SupportMessage savedMessage = supportMessageRepository.save(supportMessage);
        
        // Обновляем время последнего обновления тикета
        ticket.setUpdatedAt(LocalDateTime.now());
        supportTicketRepository.save(ticket);
        
        // Логируем отправку сообщения
        auditLogService.logEntity("SupportMessage", savedMessage.getId(), "SEND_MESSAGE", savedMessage);
        
        return savedMessage;
    }

    // Получение всех сообщений тикета
    public List<SupportMessage> getTicketMessages(Long ticketId) {
        return supportMessageRepository.findBySupportTicketIdOrderBySentAtAsc(ticketId);
    }

    // Получение тикетов пользователя
    public List<SupportTicket> getUserTickets(Long userId) {
        return supportTicketRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // Получение тикета по ID
    public SupportTicket getTicketById(Long ticketId) {
        return supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Тикет не найден"));
    }

    // Получение активных тикетов для админа
    public List<SupportTicket> getActiveTickets() {
        return supportTicketRepository.findActiveTickets();
    }

    // Обновление статуса тикета
    @Transactional
    public SupportTicket updateTicketStatus(Long ticketId, String newStatus, String adminNotes) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Тикет не найден"));
        
        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(LocalDateTime.now());
        
        if (newStatus.equals("RESOLVED")) {
            ticket.setResolvedAt(LocalDateTime.now());
        }
        
        if (adminNotes != null) {
            ticket.setAdminNotes(adminNotes);
        }
        
        SupportTicket updatedTicket = supportTicketRepository.save(ticket);
        
        // Логируем изменение статуса
        auditLogService.logEntity("SupportTicket", ticketId, "UPDATE_STATUS", updatedTicket);
        
        return updatedTicket;
    }

    // Запрос на возврат средств
    @Transactional
    public SupportTicket requestRefund(Long ticketId, String reason) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Тикет не найден"));
        
        if (ticket.getBooking() == null) {
            throw new RuntimeException("Тикет не связан с бронированием");
        }
        
        ticket.setRefundRequested(true);
        ticket.setUpdatedAt(LocalDateTime.now());
        
        // Добавляем сообщение о запросе возврата
        SupportMessage refundMessage = SupportMessage.builder()
                .supportTicket(ticket)
                .sender(ticket.getUser())
                .message("Запрос на возврат средств. Причина: " + reason)
                .sentAt(LocalDateTime.now())
                .messageType("USER")
                .isRead(false)
                .build();
        
        supportMessageRepository.save(refundMessage);
        
        SupportTicket updatedTicket = supportTicketRepository.save(ticket);
        
        // Логируем запрос возврата
        auditLogService.logEntity("SupportTicket", ticketId, "REQUEST_REFUND", updatedTicket);
        
        return updatedTicket;
    }

    // Одобрение возврата средств
    @Transactional
    public SupportTicket approveRefund(Long ticketId, String adminNotes) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Тикет не найден"));
        
        if (!ticket.getRefundRequested()) {
            throw new RuntimeException("Возврат не был запрошен");
        }
        
        ticket.setRefundApproved(true);
        ticket.setRefundDate(LocalDateTime.now());
        ticket.setStatus("RESOLVED");
        ticket.setResolvedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        
        if (adminNotes != null) {
            ticket.setAdminNotes(adminNotes);
        }
        
        // Отменяем бронирование
        if (ticket.getBooking() != null) {
            Booking booking = ticket.getBooking();
            booking.setStatus("CANCELLED");
            booking.setCancelledDate(LocalDateTime.now());
            booking.setRefundDate(LocalDateTime.now());
            booking.setRefundAmount(booking.getTour().getPrice());
            bookingRepository.save(booking);
        }
        
        SupportTicket updatedTicket = supportTicketRepository.save(ticket);
        
        // Добавляем системное сообщение об одобрении возврата
        SupportMessage refundApprovedMessage = SupportMessage.builder()
                .supportTicket(ticket)
                .sender(null) // Системное сообщение
                .message("Возврат средств одобрен. Деньги будут возвращены в течение 3-5 рабочих дней.")
                .sentAt(LocalDateTime.now())
                .messageType("SYSTEM")
                .isRead(false)
                .build();
        
        supportMessageRepository.save(refundApprovedMessage);
        
        // Логируем одобрение возврата
        auditLogService.logEntity("SupportTicket", ticketId, "APPROVE_REFUND", updatedTicket);
        
        return updatedTicket;
    }

    // Отклонение возврата средств
    @Transactional
    public SupportTicket rejectRefund(Long ticketId, String reason) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Тикет не найден"));
        
        ticket.setRefundApproved(false);
        ticket.setUpdatedAt(LocalDateTime.now());
        
        // Добавляем сообщение об отклонении возврата
        SupportMessage refundRejectedMessage = SupportMessage.builder()
                .supportTicket(ticket)
                .sender(null) // Системное сообщение
                .message("Возврат средств отклонен. Причина: " + reason)
                .sentAt(LocalDateTime.now())
                .messageType("SYSTEM")
                .isRead(false)
                .build();
        
        supportMessageRepository.save(refundRejectedMessage);
        
        SupportTicket updatedTicket = supportTicketRepository.save(ticket);
        
        // Логируем отклонение возврата
        auditLogService.logEntity("SupportTicket", ticketId, "REJECT_REFUND", updatedTicket);
        
        return updatedTicket;
    }

    // Получение статистики для админа
    public SupportStatistics getSupportStatistics() {
        Long openTickets = supportTicketRepository.countOpenTickets();
        Long pendingRefunds = supportTicketRepository.countPendingRefundRequests();
        
        return new SupportStatistics(openTickets, pendingRefunds);
    }

    // Внутренний класс для статистики
    public static class SupportStatistics {
        private Long openTickets;
        private Long pendingRefunds;
        
        public SupportStatistics(Long openTickets, Long pendingRefunds) {
            this.openTickets = openTickets;
            this.pendingRefunds = pendingRefunds;
        }
        
        public Long getOpenTickets() { return openTickets; }
        public void setOpenTickets(Long openTickets) { this.openTickets = openTickets; }
        public Long getPendingRefunds() { return pendingRefunds; }
        public void setPendingRefunds(Long pendingRefunds) { this.pendingRefunds = pendingRefunds; }
    }
} 