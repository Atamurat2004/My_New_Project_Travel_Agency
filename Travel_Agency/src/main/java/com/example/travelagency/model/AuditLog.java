package com.example.travelagency.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String entityType;
    private Long entityId;
    private String action;
    @Lob
    private String jsonData;
    private LocalDateTime timestamp;

    public AuditLog() {}
    
    public AuditLog(Long id, String entityType, Long entityId, String action, String jsonData, LocalDateTime timestamp) {
        this.id = id;
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.jsonData = jsonData;
        this.timestamp = timestamp;
    }
    
    public static AuditLogBuilder builder() { return new AuditLogBuilder(); }
    
    public static class AuditLogBuilder {
        private Long id;
        private String entityType;
        private Long entityId;
        private String action;
        private String jsonData;
        private LocalDateTime timestamp;
        
        public AuditLogBuilder id(Long id) { this.id = id; return this; }
        public AuditLogBuilder entityType(String entityType) { this.entityType = entityType; return this; }
        public AuditLogBuilder entityId(Long entityId) { this.entityId = entityId; return this; }
        public AuditLogBuilder action(String action) { this.action = action; return this; }
        public AuditLogBuilder jsonData(String jsonData) { this.jsonData = jsonData; return this; }
        public AuditLogBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
        
        public AuditLog build() { 
            return new AuditLog(id, entityType, entityId, action, jsonData, timestamp); 
        }
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getJsonData() { return jsonData; }
    public void setJsonData(String jsonData) { this.jsonData = jsonData; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
} 