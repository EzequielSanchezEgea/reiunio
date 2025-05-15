package com.ezequiel.reiunio.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.ezequiel.reiunio.entity.AuditLog;
import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.ActionType;

public interface AuditLogService {
    
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
}