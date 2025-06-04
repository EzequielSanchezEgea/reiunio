package com.ezequiel.reiunio.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ezequiel.reiunio.entity.AuditLog;
import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.ActionType;

/**
 * Repository interface for accessing and managing {@link AuditLog} entities.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Retrieves all audit logs for a given user.
     *
     * @param user the user associated with the audit logs
     * @return a list of audit logs
     */
    List<AuditLog> findByUser(User user);

    /**
     * Retrieves all audit logs of a specific action type.
     *
     * @param actionType the action type to filter by
     * @return a list of audit logs
     */
    List<AuditLog> findByActionType(ActionType actionType);

    /**
     * Retrieves all audit logs for a specific affected entity.
     *
     * @param affectedEntity the name of the affected entity
     * @return a list of audit logs
     */
    List<AuditLog> findByAffectedEntity(String affectedEntity);

    /**
     * Retrieves audit logs by affected entity name and its ID.
     *
     * @param affectedEntity the name of the affected entity
     * @param entityId the ID of the affected entity
     * @return a list of audit logs
     */
    List<AuditLog> findByAffectedEntityAndEntityId(String affectedEntity, Long entityId);

    /**
     * Retrieves audit logs that occurred within a specific date and time range.
     *
     * @param start the start of the date-time range
     * @param end the end of the date-time range
     * @return a list of audit logs
     */
    List<AuditLog> findByChangeDateTimeBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Retrieves paginated audit logs for a given user.
     *
     * @param user the user associated with the audit logs
     * @param pageable the pagination information
     * @return a page of audit logs
     */
    Page<AuditLog> findByUser(User user, Pageable pageable);

    /**
     * Retrieves paginated audit logs of a specific action type.
     *
     * @param actionType the action type to filter by
     * @param pageable the pagination information
     * @return a page of audit logs
     */
    Page<AuditLog> findByActionType(ActionType actionType, Pageable pageable);

    /**
     * Retrieves paginated audit logs for a specific affected entity.
     *
     * @param affectedEntity the name of the affected entity
     * @param pageable the pagination information
     * @return a page of audit logs
     */
    Page<AuditLog> findByAffectedEntity(String affectedEntity, Pageable pageable);

    /**
     * Retrieves paginated audit logs by affected entity name and its ID.
     *
     * @param affectedEntity the name of the affected entity
     * @param entityId the ID of the affected entity
     * @param pageable the pagination information
     * @return a page of audit logs
     */
    Page<AuditLog> findByAffectedEntityAndEntityId(String affectedEntity, Long entityId, Pageable pageable);

    /**
     * Retrieves paginated audit logs that occurred within a specific date and time range.
     *
     * @param start the start of the date-time range
     * @param end the end of the date-time range
     * @param pageable the pagination information
     * @return a page of audit logs
     */
    Page<AuditLog> findByChangeDateTimeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    /**
     * Retrieves audit logs with combined filters - supports any combination of filters.
     * Uses simple AND conditions for date filtering to ensure proper range handling.
     *
     * @param actionType the action type to filter by (optional)
     * @param affectedEntity the affected entity name to filter by (optional)
     * @param startDate the start date and time to filter by (optional)
     * @param endDate the end date and time to filter by (optional)
     * @param pageable pagination information
     * @return a page of audit logs matching the combined filters
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:actionType IS NULL OR a.actionType = :actionType) AND " +
           "(:affectedEntity IS NULL OR :affectedEntity = '' OR a.affectedEntity = :affectedEntity) AND " +
           "(:startDate IS NULL OR a.changeDateTime >= :startDate) AND " +
           "(:endDate IS NULL OR a.changeDateTime <= :endDate)")
    Page<AuditLog> findWithCombinedFilters(@Param("actionType") ActionType actionType,
                                          @Param("affectedEntity") String affectedEntity,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          Pageable pageable);
}