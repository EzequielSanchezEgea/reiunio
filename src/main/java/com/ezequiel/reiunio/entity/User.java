package com.ezequiel.reiunio.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.ezequiel.reiunio.enums.Role;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"password", "loans", "createdGameSessions", "gameSessionParticipations", "auditLogs"})
@EqualsAndHashCode(exclude = {"loans", "createdGameSessions", "gameSessionParticipations", "auditLogs"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(unique = true)
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    @Email
    @Column(unique = true)
    private String email;

    @NotBlank
    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Column(name = "registration_date")
    @Builder.Default
    private LocalDate registrationDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<Loan> loans = new ArrayList<>();

    @OneToMany(mappedBy = "creator")
    @Builder.Default
    private List<GameSession> createdGameSessions = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<GameSessionPlayer> gameSessionParticipations = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<AuditLog> auditLogs = new ArrayList<>();
}