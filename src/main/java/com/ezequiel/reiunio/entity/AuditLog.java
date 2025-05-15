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

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "change_date_time")
    @Builder.Default
    private LocalDateTime changeDateTime = LocalDateTime.now();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type")
    private ActionType actionType;

    @NotBlank
    @Column(name = "affected_entity")
    private String affectedEntity;

    @Column(name = "entity_id")
    private Long entityId;

    @NotBlank
    @Column(length = 500)
    private String description;
}