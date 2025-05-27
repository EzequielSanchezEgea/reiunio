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

    // Field for profile photo
    @Column(name = "profile_photo_path")
    private String profilePhotoPath;

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

    /**
     * Returns the profile photo URL or a default placeholder if no photo is set
     */
    public String getProfilePhotoUrl() {
        if (profilePhotoPath != null && !profilePhotoPath.isEmpty()) {
            return profilePhotoPath;
        }
        
        // Return a default user placeholder image
        return "/defaults/user-placeholder.jpg";
    }

    /**
     * Returns true if user has a custom profile photo
     */
    public boolean hasCustomProfilePhoto() {
        return profilePhotoPath != null && !profilePhotoPath.isEmpty();
    }

    /**
     * Gets user initials for avatar generation
     */
    public String getInitials() {
        String initials = "";
        if (firstName != null && !firstName.isEmpty()) {
            initials += firstName.charAt(0);
        }
        if (lastName != null && !lastName.isEmpty()) {
            initials += lastName.charAt(0);
        }
        return initials.toUpperCase();
    }

    /**
     * Gets full name
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