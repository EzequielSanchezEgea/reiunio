package com.ezequiel.reiunio.controller;

import java.time.LocalDateTime;
import java.util.List;

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
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<AuditLog> auditLogs;
        
        if (actionType != null && !actionType.isEmpty()) {
            try {
                ActionType type = ActionType.valueOf(actionType.toUpperCase());
                auditLogs = auditLogService.findByActionType(type);
                model.addAttribute("filterActionType", actionType);
            } catch (IllegalArgumentException e) {
                auditLogs = auditLogService.findAll();
            }
        } else if (entity != null && !entity.isEmpty()) {
            auditLogs = auditLogService.findByAffectedEntity(entity);
            model.addAttribute("filterEntity", entity);
        } else if (startDate != null && endDate != null) {
            auditLogs = auditLogService.findBetweenDates(startDate, endDate);
            model.addAttribute("filterStartDate", startDate);
            model.addAttribute("filterEndDate", endDate);
        } else {
            auditLogs = auditLogService.findAll();
        }
        
        model.addAttribute("auditLogs", auditLogs);
        model.addAttribute("actionTypes", ActionType.values());
        
        // List of affected entities for filter
        model.addAttribute("entities", List.of("User", "Game", "Loan", "GameSession"));
        
        return "audit-logs/list";
    }
    
    @GetMapping("/entity")
    public String viewEntityAuditLogs(Model model,
                                     @RequestParam String entity,
                                     @RequestParam(required = false) Long entityId) {
        
        List<AuditLog> auditLogs;
        
        if (entityId != null) {
            auditLogs = auditLogService.findByAffectedEntityAndId(entity, entityId);
            model.addAttribute("subtitle", "Audit logs for " + entity + " #" + entityId);
        } else {
            auditLogs = auditLogService.findByAffectedEntity(entity);
            model.addAttribute("subtitle", "Audit logs for " + entity);
        }
        
        model.addAttribute("auditLogs", auditLogs);
        model.addAttribute("actionTypes", ActionType.values());
        
        return "audit-logs/list";
    }
}