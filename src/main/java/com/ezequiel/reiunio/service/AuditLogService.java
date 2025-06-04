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

    List<AuditLog> findAll();
    
    Optional<AuditLog> findById(Long id);
    
    AuditLog save(AuditLog auditLog);
    
    List<AuditLog> findByUser(User user);
    
    List<AuditLog> findByActionType(ActionType actionType);
    
    List<AuditLog> findByAffectedEntity(String affectedEntity);
    
    List<AuditLog> findByAffectedEntityAndId(String affectedEntity, Long entityId);
    
    List<AuditLog> findBetweenDates(LocalDateTime start, LocalDateTime end);
    
    void logChange(User user, ActionType actionType, String affectedEntity, Long entityId, String description);
    
    // Paginated methods
    Page<AuditLog> findAll(Pageable pageable);
    
    Page<AuditLog> findByUser(User user, Pageable pageable);
    
    Page<AuditLog> findByActionType(ActionType actionType, Pageable pageable);
    
    Page<AuditLog> findByAffectedEntity(String affectedEntity, Pageable pageable);
    
    Page<AuditLog> findByAffectedEntityAndId(String affectedEntity, Long entityId, Pageable pageable);
    
    Page<AuditLog> findBetweenDates(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    /**
     * Finds audit logs with combined filters - supports any combination of filters.
     *
     * @param actionType the action type to filter by (optional)
     * @param affectedEntity the affected entity name to filter by (optional)
     * @param startDate the start date and time to filter by (optional)
     * @param endDate the end date and time to filter by (optional)
     * @param pageable pagination information
     * @return a page of audit logs matching the combined filters
     */
    Page<AuditLog> findWithCombinedFilters(ActionType actionType, String affectedEntity, 
                                          LocalDateTime startDate, LocalDateTime endDate, 
                                          Pageable pageable);
}