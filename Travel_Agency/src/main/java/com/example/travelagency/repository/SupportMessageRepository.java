package com.example.travelagency.repository;

import com.example.travelagency.model.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {
    
    List<SupportMessage> findBySupportTicketIdOrderBySentAtAsc(Long supportTicketId);
    
    List<SupportMessage> findBySupportTicketIdAndIsReadFalseOrderBySentAtAsc(Long supportTicketId);
    
    @Query("SELECT sm FROM SupportMessage sm WHERE sm.supportTicket.id = :ticketId AND sm.sender.id != :userId ORDER BY sm.sentAt DESC")
    List<SupportMessage> findUnreadMessagesForUser(@Param("ticketId") Long ticketId, @Param("userId") Long userId);
    
    @Query("SELECT COUNT(sm) FROM SupportMessage sm WHERE sm.supportTicket.id = :ticketId AND sm.isRead = false AND sm.sender.id != :userId")
    Long countUnreadMessagesForUser(@Param("ticketId") Long ticketId, @Param("userId") Long userId);

    List<SupportMessage> findBySenderId(Long senderId);
} 