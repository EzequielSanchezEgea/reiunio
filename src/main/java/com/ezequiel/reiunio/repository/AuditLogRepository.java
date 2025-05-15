package com.ezequiel.reiunio.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ezequiel.reiunio.entity.AuditLog;
import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.ActionType;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByUser(User user);
    
    List<AuditLog> findByActionType(ActionType actionType);
    
    List<AuditLog> findByAffectedEntity(String affectedEntity);
    
    List<AuditLog> findByAffectedEntityAndEntityId(String affectedEntity, Long entityId);
    
    List<AuditLog> findByChangeDateTimeBetween(LocalDateTime start, LocalDateTime end);
}