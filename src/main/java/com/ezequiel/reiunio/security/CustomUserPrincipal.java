package com.ezequiel.reiunio.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.ezequiel.reiunio.entity.User;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Custom implementation of UserDetails for Spring Security, 
 * wrapping application-specific User entity.
 */
@Data
@AllArgsConstructor
public class CustomUserPrincipal implements UserDetails {

    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private boolean enabled;
    private boolean accountNonExpired;
    private boolean credentialsNonExpired;
    private boolean accountNonLocked;
    private Collection<? extends GrantedAuthority> authorities;
    private User user;

    /**
     * Returns the authorities granted to the user.
     * 
     * @return collection of granted authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Returns the user's password.
     * 
     * @return password
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the username used to authenticate the user.
     * 
     * @return username
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Indicates whether the user's account has expired.
     * 
     * @return true if account is non-expired, false otherwise
     */
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    /**
     * Indicates whether the user is locked or unlocked.
     * 
     * @return true if account is non-locked, false otherwise
     */
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    /**
     * Indicates whether the user's credentials (password) have expired.
     * 
     * @return true if credentials are non-expired, false otherwise
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    /**
     * Indicates whether the user is enabled or disabled.
     * 
     * @return true if enabled, false otherwise
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns the associated User entity.
     * 
     * @return the User object
     */
    public User getUser() {
        return user;
    }

    /**
     * Returns the profile photo URL of the user.
     * 
     * @return profile photo URL or default if user is null
     */
    public String getProfilePhotoUrl() {
        return user != null ? user.getProfilePhotoUrl() : "/defaults/user-placeholder.jpg";
    }

    /**
     * Returns the full name of the user.
     * 
     * @return full name or username if user is null
     */
    public String getFullName() {
        return user != null ? user.getFullName() : username;
    }

    /**
     * Returns the initials of the user.
     * 
     * @return initials or first letter of username if user is null
     */
    public String getInitials() {
        return user != null ? user.getInitials() : username.substring(0, 1).toUpperCase();
    }
}
