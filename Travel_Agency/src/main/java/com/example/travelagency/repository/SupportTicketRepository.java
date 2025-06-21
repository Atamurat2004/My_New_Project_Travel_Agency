package com.example.travelagency.repository;

import com.example.travelagency.model.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    
    List<SupportTicket> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<SupportTicket> findByStatusOrderByCreatedAtDesc(String status);
    
    List<SupportTicket> findByTicketTypeOrderByCreatedAtDesc(String ticketType);
    
    List<SupportTicket> findByRefundRequestedTrueOrderByCreatedAtDesc();
    
    List<SupportTicket> findByRefundApprovedTrueOrderByCreatedAtDesc();
    
    @Query("SELECT st FROM SupportTicket st WHERE st.status = 'OPEN' OR st.status = 'IN_PROGRESS' ORDER BY st.createdAt DESC")
    List<SupportTicket> findActiveTickets();
    
    @Query("SELECT st FROM SupportTicket st WHERE st.user.id = :userId AND (st.status = 'OPEN' OR st.status = 'IN_PROGRESS') ORDER BY st.createdAt DESC")
    List<SupportTicket> findActiveTicketsByUser(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(st) FROM SupportTicket st WHERE st.status = 'OPEN'")
    Long countOpenTickets();
    
    @Query("SELECT COUNT(st) FROM SupportTicket st WHERE st.refundRequested = true AND st.refundApproved IS NULL")
    Long countPendingRefundRequests();

    List<SupportTicket> findAllByOrderByCreatedAtDesc();
} 