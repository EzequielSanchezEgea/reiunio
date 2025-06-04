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

    /**
     * Retrieves all audit logs.
     *
     * @return a list of all audit logs
     */
    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findAll() {
        log.debug("Finding all audit logs");
        return auditLogRepository.findAll();
    }

    /**
     * Finds an audit log by its ID.
     *
     * @param id the ID of the audit log
     * @return an optional containing the audit log if found
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<AuditLog> findById(Long id) {
        log.debug("Finding audit log by id: {}", id);
        return auditLogRepository.findById(id);
    }

    /**
     * Saves a new audit log.
     *
     * @param auditLog the audit log to save
     * @return the saved audit log
     */
    @Override
    @Transactional
    public AuditLog save(AuditLog auditLog) {
        log.debug("Saving audit log: {}", auditLog.getDescription());
        return auditLogRepository.save(auditLog);
    }

    /**
     * Finds audit logs by user.
     *
     * @param user the user to filter logs by
     * @return a list of audit logs related to the user
     */
    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findByUser(User user) {
        log.debug("Finding audit logs by user: {}", user.getUsername());
        return auditLogRepository.findByUser(user);
    }

    /**
     * Finds audit logs by action type.
     *
     * @param actionType the action type to filter logs by
     * @return a list of audit logs matching the action type
     */
    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findByActionType(ActionType actionType) {
        log.debug("Finding audit logs by action type: {}", actionType);
        return auditLogRepository.findByActionType(actionType);
    }

    /**
     * Finds audit logs by affected entity name.
     *
     * @param affectedEntity the name of the affected entity
     * @return a list of audit logs related to the entity
     */
    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findByAffectedEntity(String affectedEntity) {
        log.debug("Finding audit logs by affected entity: {}", affectedEntity);
        return auditLogRepository.findByAffectedEntity(affectedEntity);
    }

    /**
     * Finds audit logs by affected entity name and entity ID.
     *
     * @param affectedEntity the name of the affected entity
     * @param entityId       the ID of the affected entity
     * @return a list of matching audit logs
     */
    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findByAffectedEntityAndId(String affectedEntity, Long entityId) {
        log.debug("Finding audit logs by affected entity: {} and id: {}", affectedEntity, entityId);
        return auditLogRepository.findByAffectedEntityAndEntityId(affectedEntity, entityId);
    }

    /**
     * Finds audit logs between two dates.
     *
     * @param start the start date and time
     * @param end   the end date and time
     * @return a list of audit logs within the date range
     */
    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findBetweenDates(LocalDateTime start, LocalDateTime end) {
        log.debug("Finding audit logs between dates: {} and {}", start, end);
        return auditLogRepository.findByChangeDateTimeBetween(start, end);
    }

    /**
     * Logs a new change in the system.
     *
     * @param user           the user who performed the action
     * @param actionType     the type of action performed
     * @param affectedEntity the affected entity
     * @param entityId       the ID of the affected entity
     * @param description    the description of the change
     */
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

    /**
     * Retrieves all audit logs with pagination.
     *
     * @param pageable pagination information
     * @return a page of audit logs
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> findAll(Pageable pageable) {
        log.debug("Finding all audit logs with pagination: page {}, size {}", 
                 pageable.getPageNumber(), pageable.getPageSize());
        return auditLogRepository.findAll(pageable);
    }

    /**
     * Finds paginated audit logs by user.
     *
     * @param user     the user to filter logs by
     * @param pageable pagination information
     * @return a page of audit logs
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> findByUser(User user, Pageable pageable) {
        log.debug("Finding audit logs by user: {} with pagination: page {}, size {}", 
                 user.getUsername(), pageable.getPageNumber(), pageable.getPageSize());
        return auditLogRepository.findByUser(user, pageable);
    }

    /**
     * Finds paginated audit logs by action type.
     *
     * @param actionType the action type to filter logs by
     * @param pageable   pagination information
     * @return a page of audit logs
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> findByActionType(ActionType actionType, Pageable pageable) {
        log.debug("Finding audit logs by action type: {} with pagination: page {}, size {}", 
                 actionType, pageable.getPageNumber(), pageable.getPageSize());
        return auditLogRepository.findByActionType(actionType, pageable);
    }

    /**
     * Finds paginated audit logs by affected entity name.
     *
     * @param affectedEntity the name of the affected entity
     * @param pageable       pagination information
     * @return a page of audit logs
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> findByAffectedEntity(String affectedEntity, Pageable pageable) {
        log.debug("Finding audit logs by affected entity: {} with pagination: page {}, size {}", 
                 affectedEntity, pageable.getPageNumber(), pageable.getPageSize());
        return auditLogRepository.findByAffectedEntity(affectedEntity, pageable);
    }

    /**
     * Finds paginated audit logs by affected entity and entity ID.
     *
     * @param affectedEntity the name of the affected entity
     * @param entityId       the ID of the affected entity
     * @param pageable       pagination information
     * @return a page of audit logs
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> findByAffectedEntityAndId(String affectedEntity, Long entityId, Pageable pageable) {
        log.debug("Finding audit logs by affected entity: {} and id: {} with pagination: page {}, size {}", 
                 affectedEntity, entityId, pageable.getPageNumber(), pageable.getPageSize());
        return auditLogRepository.findByAffectedEntityAndEntityId(affectedEntity, entityId, pageable);
    }

    /**
     * Finds paginated audit logs within a date range.
     *
     * @param start    the start date and time
     * @param end      the end date and time
     * @param pageable pagination information
     * @return a page of audit logs
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> findBetweenDates(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        log.debug("Finding audit logs between dates: {} and {} with pagination: page {}, size {}", 
                 start, end, pageable.getPageNumber(), pageable.getPageSize());
        return auditLogRepository.findByChangeDateTimeBetween(start, end, pageable);
    }

    /**
     * Finds audit logs with combined filters - supports any combination of filters.
     * All parameters are optional and will be ignored if null or empty.
     *
     * @param actionType the action type to filter by (optional)
     * @param affectedEntity the affected entity name to filter by (optional)
     * @param startDate the start date and time to filter by (optional)
     * @param endDate the end date and time to filter by (optional)
     * @param pageable pagination information
     * @return a page of audit logs matching the combined filters
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> findWithCombinedFilters(ActionType actionType, String affectedEntity, 
                                                  LocalDateTime startDate, LocalDateTime endDate, 
                                                  Pageable pageable) {
        log.debug("Finding audit logs with combined filters - actionType: {}, entity: {}, startDate: {}, endDate: {}, page: {}, size: {}", 
                 actionType, affectedEntity, startDate, endDate, pageable.getPageNumber(), pageable.getPageSize());

        // Normalize empty string to null for proper query handling
        String normalizedEntity = (affectedEntity != null && affectedEntity.trim().isEmpty()) ? null : affectedEntity;
        
        return auditLogRepository.findWithCombinedFilters(actionType, normalizedEntity, startDate, endDate, pageable);
    }
}