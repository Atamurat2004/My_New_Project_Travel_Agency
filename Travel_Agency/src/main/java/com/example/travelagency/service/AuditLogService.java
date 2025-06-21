package com.example.travelagency.service;

import com.example.travelagency.model.AuditLog;
import com.example.travelagency.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public AuditLogService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    public void logEntity(String entityType, Long entityId, String action, Object entity) {
        try {
            String json = objectMapper.writeValueAsString(entity);
            AuditLog log = new AuditLog(null, entityType, entityId, action, json, LocalDateTime.now());
            auditLogRepository.save(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 