package com.ezequiel.reiunio.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.ezequiel.reiunio.enums.Role;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Entity representing a user in the system.
 * Includes authentication details, profile info, and relationships to loans, sessions, and logs.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"password", "loans", "createdGameSessions", "gameSessionParticipations", "auditLogs"})
@EqualsAndHashCode(exclude = {"loans", "createdGameSessions", "gameSessionParticipations", "auditLogs"})
public class User {

    /** Unique identifier for the user. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Username for login. Must be unique. */
    @NotBlank
    @Size(min = 3, max = 50)
    @Column(unique = true)
    private String username;

    /** Password (optional on update). */
    private String password;

    /** Email address. Must be unique and valid. */
    @NotBlank
    @Email
    @Column(unique = true)
    private String email;

    /** First name of the user. */
    @NotBlank
    @Size(max = 100)
    private String firstName;

    /** Last name of the user. */
    @Size(max = 100)
    private String lastName;

    /** Registration date of the user. Defaults to current date. */
    @Column(name = "registration_date")
    @Builder.Default
    private LocalDate registrationDate = LocalDate.now();

    /** Role of the user (e.g., ADMIN, USER). */
    @Enumerated(EnumType.STRING)
    private Role role;

    /** Path to the user's profile photo, if any. */
    @Column(name = "profile_photo_path")
    private String profilePhotoPath;

    /** List of loans made by the user. */
    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<Loan> loans = new ArrayList<>();

    /** List of game sessions created by the user. */
    @OneToMany(mappedBy = "creator")
    @Builder.Default
    private List<GameSession> createdGameSessions = new ArrayList<>();

    /** List of participations in game sessions. */
    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<GameSessionPlayer> gameSessionParticipations = new ArrayList<>();

    /** List of audit logs associated with the user. */
    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<AuditLog> auditLogs = new ArrayList<>();

    /**
     * Returns the profile photo URL or a default placeholder if no custom photo is set.
     *
     * @return Profile photo URL or default image path.
     */
    public String getProfilePhotoUrl() {
        return (profilePhotoPath != null && !profilePhotoPath.isEmpty())
                ? profilePhotoPath
                : "/defaults/user-placeholder.jpg";
    }

    /**
     * Checks if the user has set a custom profile photo.
     *
     * @return true if a custom profile photo exists.
     */
    public boolean hasCustomProfilePhoto() {
        return profilePhotoPath != null && !profilePhotoPath.isEmpty();
    }

    /**
     * Generates initials from the user's name.
     *
     * @return Uppercase initials, useful for avatars.
     */
    public String getInitials() {
        StringBuilder initials = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) {
            initials.append(firstName.charAt(0));
        }
        if (lastName != null && !lastName.isEmpty()) {
            initials.append(lastName.charAt(0));
        }
        return initials.toString().toUpperCase();
    }

    /**
     * Concatenates first and last name.
     *
     * @return Full name of the user.
     */
    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) {
            fullName.append(firstName);
        }
        if (lastName != null && !lastName.isEmpty()) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(lastName);
        }
        return fullName.toString();
    }
}
