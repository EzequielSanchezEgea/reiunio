package com.ezequiel.reiunio.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ezequiel.reiunio.entity.AuditLog;
import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.ActionType;

public interface AuditLogService {
    
    // Existing methods (non-paginated)
    List<AuditLog> findAll();
    
    Optional<AuditLog> findById(Long id);
    
    AuditLog save(AuditLog auditLog);
    
    List<AuditLog> findByUser(User user);
    
    List<AuditLog> findByActionType(ActionType actionType);
    
    List<AuditLog> findByAffectedEntity(String affectedEntity);
    
    List<AuditLog> findByAffectedEntityAndId(String affectedEntity, Long entityId);
    
    List<AuditLog> findBetweenDates(LocalDateTime start, LocalDateTime end);
    
    void logChange(User user, ActionType actionType, String affectedEntity, 
                   Long entityId, String description);
    
    // New paginated methods
    Page<AuditLog> findAll(Pageable pageable);
    
    Page<AuditLog> findByUser(User user, Pageable pageable);
    
    Page<AuditLog> findByActionType(ActionType actionType, Pageable pageable);
    
    Page<AuditLog> findByAffectedEntity(String affectedEntity, Pageable pageable);
    
    Page<AuditLog> findByAffectedEntityAndId(String affectedEntity, Long entityId, Pageable pageable);
    
    Page<AuditLog> findBetweenDates(LocalDateTime start, LocalDateTime end, Pageable pageable);
}