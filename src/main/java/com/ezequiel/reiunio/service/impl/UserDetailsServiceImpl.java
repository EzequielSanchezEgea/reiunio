package com.ezequiel.reiunio.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.repository.UserRepository;
import com.ezequiel.reiunio.security.CustomUserPrincipal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service implementation for loading user-specific data during authentication.
 * Integrates with Spring Security to provide custom user details.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user by username for authentication purposes.
     *
     * @param username the username identifying the user
     * @return a fully populated UserDetails object
     * @throws UsernameNotFoundException if the user could not be found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for username: {}", username);

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (!userOpt.isPresent()) {
            log.warn("User not found: {}", username);
            throw new UsernameNotFoundException("User not found: " + username);
        }

        User user = userOpt.get();
        log.debug("User found: {} with role: {}", user.getUsername(), user.getRole());

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        return new CustomUserPrincipal(
            user.getUsername(),
            user.getPassword(),
            true,
            true,
            true,
            true,
            authorities,
            user
        );
    }
}
