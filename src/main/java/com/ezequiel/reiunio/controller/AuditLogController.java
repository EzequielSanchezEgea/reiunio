package com.ezequiel.reiunio.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ezequiel.reiunio.entity.AuditLog;
import com.ezequiel.reiunio.enums.ActionType;
import com.ezequiel.reiunio.service.AuditLogService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller responsible for handling requests related to audit log entries.
 * Access is restricted to users with the ADMIN role.
 */
@Controller
@RequestMapping("/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * Displays a paginated list of audit logs with optional filters: action type, affected entity,
     * and date range. Now supports combining multiple filters.
     *
     * @param model      the UI model to populate
     * @param actionType optional filter by action type (CREATE, UPDATE, DELETE, etc.)
     * @param entity     optional filter by affected entity name
     * @param startDate  optional filter start datetime
     * @param endDate    optional filter end datetime
     * @param page       the page number to retrieve (default is 0)
     * @param size       the page size (default is 20; max is 100)
     * @return the name of the Thymeleaf view to render
     */
    @GetMapping
    public String listAuditLogs(Model model,
                                @RequestParam(required = false) String actionType,
                                @RequestParam(required = false) String entity,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "20") int size) {

        log.debug("Listing audit logs - page: {}, size: {}, actionType: {}, entity: {}, startDate: {}, endDate: {}", 
                 page, size, actionType, entity, startDate, endDate);

        if (size < 1 || size > 100) {
            size = 20;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("changeDateTime").descending());
        Page<AuditLog> auditLogsPage;

        try {
            // Parse action type if provided
            ActionType actionTypeEnum = null;
            if (actionType != null && !actionType.isEmpty()) {
                try {
                    actionTypeEnum = ActionType.valueOf(actionType.toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid action type: {}", actionType);
                    actionTypeEnum = null;
                }
            }

            // Normalize entity (convert empty string to null)
            String normalizedEntity = (entity != null && entity.trim().isEmpty()) ? null : entity;

            // Use the new combined filter method
            auditLogsPage = auditLogService.findWithCombinedFilters(
                actionTypeEnum, normalizedEntity, startDate, endDate, pageable);

            // Add filter attributes to model for form persistence
            if (actionTypeEnum != null) {
                model.addAttribute("filterActionType", actionType);
                log.debug("Applied action type filter: {}", actionType);
            }
            if (normalizedEntity != null) {
                model.addAttribute("filterEntity", normalizedEntity);
                log.debug("Applied entity filter: {}", normalizedEntity);
            }
            if (startDate != null) {
                model.addAttribute("filterStartDate", startDate);
                log.debug("Applied start date filter: {}", startDate);
            }
            if (endDate != null) {
                model.addAttribute("filterEndDate", endDate);
                log.debug("Applied end date filter: {}", endDate);
            }

            log.debug("Found {} records with combined filters", auditLogsPage.getTotalElements());

        } catch (Exception e) {
            log.error("Error retrieving audit logs", e);
            auditLogsPage = Page.empty(pageable);
        }

        model.addAttribute("auditLogs", auditLogsPage.getContent());
        model.addAttribute("currentPage", auditLogsPage.getNumber());
        model.addAttribute("totalPages", auditLogsPage.getTotalPages());
        model.addAttribute("totalElements", auditLogsPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("hasContent", auditLogsPage.hasContent());
        model.addAttribute("isFirst", auditLogsPage.isFirst());
        model.addAttribute("isLast", auditLogsPage.isLast());
        model.addAttribute("actionTypes", ActionType.values());
        model.addAttribute("entities", List.of("User", "Game", "Loan", "GameSession"));

        log.debug("Returning page {} of {} (total {} records)",
                auditLogsPage.getNumber() + 1, auditLogsPage.getTotalPages(), auditLogsPage.getTotalElements());

        return "audit-logs/list";
    }
    /**
     * Displays a paginated list of audit logs for a specific entity, with optional filtering by entity ID.
     *
     * @param model    the UI model to populate
     * @param entity   the name of the affected entity
     * @param entityId optional ID of the specific entity instance
     * @param page     the page number to retrieve (default is 0)
     * @param size     the page size (default is 20; max is 100)
     * @return the name of the Thymeleaf view to render
     */
    @GetMapping("/entity")
    public String viewEntityAuditLogs(Model model,
                                      @RequestParam String entity,
                                      @RequestParam(required = false) Long entityId,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size) {

        log.debug("Viewing entity audit logs - entity: {}, entityId: {}, page: {}, size: {}", entity, entityId, page, size);

        if (size < 1 || size > 100) {
            size = 20;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("changeDateTime").descending());
        Page<AuditLog> auditLogsPage;
        String subtitle;

        try {
            if (entityId != null) {
                auditLogsPage = auditLogService.findByAffectedEntityAndId(entity, entityId, pageable);
                subtitle = "Audit logs for " + entity + " #" + entityId;
                log.debug("Found {} records for entity {} with ID {}", auditLogsPage.getTotalElements(), entity, entityId);
            } else {
                auditLogsPage = auditLogService.findByAffectedEntity(entity, pageable);
                subtitle = "Audit logs for " + entity;
                log.debug("Found {} records for entity {}", auditLogsPage.getTotalElements(), entity);
            }
        } catch (Exception e) {
            log.error("Error retrieving entity audit logs for entity: {}, entityId: {}", entity, entityId, e);
            auditLogsPage = Page.empty(pageable);
            subtitle = "Error loading audit logs";
        }

        model.addAttribute("auditLogs", auditLogsPage.getContent());
        model.addAttribute("currentPage", auditLogsPage.getNumber());
        model.addAttribute("totalPages", auditLogsPage.getTotalPages());
        model.addAttribute("totalElements", auditLogsPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("hasContent", auditLogsPage.hasContent());
        model.addAttribute("isFirst", auditLogsPage.isFirst());
        model.addAttribute("isLast", auditLogsPage.isLast());
        model.addAttribute("filterEntity", entity);

        if (entityId != null) {
            model.addAttribute("filterEntityId", entityId);
        }

        model.addAttribute("subtitle", subtitle);
        model.addAttribute("actionTypes", ActionType.values());
        model.addAttribute("entities", List.of("User", "Game", "Loan", "GameSession"));

        return "audit-logs/list";
    }
}