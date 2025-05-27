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

@Controller
@RequestMapping("/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public String listAuditLogs(Model model,
                               @RequestParam(required = false) String actionType,
                               @RequestParam(required = false) String entity,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Listing audit logs - page: {}, size: {}, actionType: {}, entity: {}", 
                 page, size, actionType, entity);
        
        // Validate page size
        if (size < 1 || size > 100) {
            size = 20;
        }
        
        // Create pageable with sorting (most recent first)
        Pageable pageable = PageRequest.of(page, size, Sort.by("changeDateTime").descending());
        
        Page<AuditLog> auditLogsPage;
        
        try {
            if (actionType != null && !actionType.isEmpty()) {
                try {
                    ActionType type = ActionType.valueOf(actionType.toUpperCase());
                    auditLogsPage = auditLogService.findByActionType(type, pageable);
                    model.addAttribute("filterActionType", actionType);
                    log.debug("Filtered by action type: {}, found {} records", actionType, auditLogsPage.getTotalElements());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid action type: {}", actionType);
                    auditLogsPage = auditLogService.findAll(pageable);
                }
            } else if (entity != null && !entity.isEmpty()) {
                auditLogsPage = auditLogService.findByAffectedEntity(entity, pageable);
                model.addAttribute("filterEntity", entity);
                log.debug("Filtered by entity: {}, found {} records", entity, auditLogsPage.getTotalElements());
            } else if (startDate != null && endDate != null) {
                auditLogsPage = auditLogService.findBetweenDates(startDate, endDate, pageable);
                model.addAttribute("filterStartDate", startDate);
                model.addAttribute("filterEndDate", endDate);
                log.debug("Filtered by date range: {} to {}, found {} records", 
                         startDate, endDate, auditLogsPage.getTotalElements());
            } else {
                auditLogsPage = auditLogService.findAll(pageable);
                log.debug("No filters applied, found {} total records", auditLogsPage.getTotalElements());
            }
        } catch (Exception e) {
            log.error("Error retrieving audit logs", e);
            auditLogsPage = Page.empty(pageable);
        }
        
        // Add pagination data to model
        model.addAttribute("auditLogs", auditLogsPage.getContent());
        model.addAttribute("currentPage", auditLogsPage.getNumber());
        model.addAttribute("totalPages", auditLogsPage.getTotalPages());
        model.addAttribute("totalElements", auditLogsPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("hasContent", auditLogsPage.hasContent());
        model.addAttribute("isFirst", auditLogsPage.isFirst());
        model.addAttribute("isLast", auditLogsPage.isLast());
        
        // Add filter options
        model.addAttribute("actionTypes", ActionType.values());
        model.addAttribute("entities", List.of("User", "Game", "Loan", "GameSession"));
        
        log.debug("Returning page {} of {} (total {} records)", 
                 auditLogsPage.getNumber() + 1, auditLogsPage.getTotalPages(), auditLogsPage.getTotalElements());
        
        return "audit-logs/list";
    }
    
    @GetMapping("/entity")
    public String viewEntityAuditLogs(Model model,
                                     @RequestParam String entity,
                                     @RequestParam(required = false) Long entityId,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Viewing entity audit logs - entity: {}, entityId: {}, page: {}, size: {}", 
                 entity, entityId, page, size);
        
        // Validate page size
        if (size < 1 || size > 100) {
            size = 20;
        }
        
        // Create pageable with sorting (most recent first)
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
        
        // Add pagination data to model
        model.addAttribute("auditLogs", auditLogsPage.getContent());
        model.addAttribute("currentPage", auditLogsPage.getNumber());
        model.addAttribute("totalPages", auditLogsPage.getTotalPages());
        model.addAttribute("totalElements", auditLogsPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("hasContent", auditLogsPage.hasContent());
        model.addAttribute("isFirst", auditLogsPage.isFirst());
        model.addAttribute("isLast", auditLogsPage.isLast());
        
        // Add filter data
        model.addAttribute("filterEntity", entity);
        if (entityId != null) {
            model.addAttribute("filterEntityId", entityId);
        }
        model.addAttribute("subtitle", subtitle);
        
        // Add filter options
        model.addAttribute("actionTypes", ActionType.values());
        model.addAttribute("entities", List.of("User", "Game", "Loan", "GameSession"));
        
        return "audit-logs/list";
    }
}