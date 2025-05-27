package com.ezequiel.reiunio.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ezequiel.reiunio.entity.AuditLog;
import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.ActionType;
import com.ezequiel.reiunio.repository.AuditLogRepository;
import com.ezequiel.reiunio.service.AuditLogService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    // Existing non-paginated methods
    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findAll() {
        log.debug("Finding all audit logs");
        return auditLogRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuditLog> findById(Long id) {
        log.debug("Finding audit log by id: {}", id);
        return auditLogRepository.findById(id);
    }

    @Override
    @Transactional
    public AuditLog save(AuditLog auditLog) {
        log.debug("Saving audit log: {}", auditLog.getDescription());
        return auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findByUser(User user) {
        log.debug("Finding audit logs by user: {}", user.getUsername());
        return auditLogRepository.findByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findByActionType(ActionType actionType) {
        log.debug("Finding audit logs by action type: {}", actionType);
        return auditLogRepository.findByActionType(actionType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findByAffectedEntity(String affectedEntity) {
        log.debug("Finding audit logs by affected entity: {}", affectedEntity);
        return auditLogRepository.findByAffectedEntity(affectedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findByAffectedEntityAndId(String affectedEntity, Long entityId) {
        log.debug("Finding audit logs by affected entity: {} and id: {}", affectedEntity, entityId);
        return auditLogRepository.findByAffectedEntityAndEntityId(affectedEntity, entityId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findBetweenDates(LocalDateTime start, LocalDateTime end) {
        log.debug("Finding audit logs between dates: {} and {}", start, end);
        return auditLogRepository.findByChangeDateTimeBetween(start, end);
    }

    @Override
    @Transactional
    public void logChange(User user, ActionType actionType, String affectedEntity, 
                         Long entityId, String description) {
        log.debug("Logging change: {} - {} - {} - {}", user.getUsername(), actionType, affectedEntity, description);
        
        AuditLog auditLog = AuditLog.builder()
                .user(user)
                .actionType(actionType)
                .affectedEntity(affectedEntity)
                .entityId(entityId)
                .description(description)
                .build();
        
        auditLogRepository.save(auditLog);
    }

    // New paginated methods
    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> findAll(Pageable pageable) {
        log.debug("Finding all audit logs with pagination: page {}, size {}", 
                 pageable.getPageNumber(), pageable.getPageSize());
        return auditLogRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> findByUser(User user, Pageable pageable) {
        log.debug("Finding audit logs by user: {} with pagination: page {}, size {}", 
                 user.getUsername(), pageable.getPageNumber(), pageable.getPageSize());
        return auditLogRepository.findByUser(user, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> findByActionType(ActionType actionType, Pageable pageable) {
        log.debug("Finding audit logs by action type: {} with pagination: page {}, size {}", 
                 actionType, pageable.getPageNumber(), pageable.getPageSize());
        return auditLogRepository.findByActionType(actionType, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> findByAffectedEntity(String affectedEntity, Pageable pageable) {
        log.debug("Finding audit logs by affected entity: {} with pagination: page {}, size {}", 
                 affectedEntity, pageable.getPageNumber(), pageable.getPageSize());
        return auditLogRepository.findByAffectedEntity(affectedEntity, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> findByAffectedEntityAndId(String affectedEntity, Long entityId, Pageable pageable) {
        log.debug("Finding audit logs by affected entity: {} and id: {} with pagination: page {}, size {}", 
                 affectedEntity, entityId, pageable.getPageNumber(), pageable.getPageSize());
        return auditLogRepository.findByAffectedEntityAndEntityId(affectedEntity, entityId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> findBetweenDates(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        log.debug("Finding audit logs between dates: {} and {} with pagination: page {}, size {}", 
                 start, end, pageable.getPageNumber(), pageable.getPageSize());
        return auditLogRepository.findByChangeDateTimeBetween(start, end, pageable);
    }
}