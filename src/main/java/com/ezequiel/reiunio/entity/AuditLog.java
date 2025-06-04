package com.ezequiel.reiunio.entity;

import java.time.LocalDateTime;

import com.ezequiel.reiunio.enums.ActionType;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Entity representing an audit log entry.
 * Used to track actions performed by users in the system.
 */
@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    /**
     * Unique identifier for the audit log entry.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who performed the action.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The date and time when the change occurred.
     * Defaults to the current time if not set.
     */
    @NotNull
    @Column(name = "change_date_time")
    @Builder.Default
    private LocalDateTime changeDateTime = LocalDateTime.now();

    /**
     * The type of action that was performed (e.g., CREATE, UPDATE, DELETE).
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type")
    private ActionType actionType;

    /**
     * The name of the entity that was affected by the action.
     */
    @NotBlank
    @Column(name = "affected_entity")
    private String affectedEntity;

    /**
     * The ID of the affected entity, if applicable.
     */
    @Column(name = "entity_id")
    private Long entityId;

    /**
     * A description of the action taken.
     */
    @NotBlank
    @Column(length = 500)
    private String description;
}