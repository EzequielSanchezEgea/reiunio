package com.ezequiel.reiunio.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.Role;
import com.ezequiel.reiunio.repository.UserRepository;
import com.ezequiel.reiunio.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        log.debug("Finding all users");
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        log.debug("Finding user by id: {}", id);
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public User save(User user) {
        log.debug("Saving user: {}", user.getUsername());
        
        // Encrypt password before saving the user
        if (user.getId() == null || (user.getPassword() != null && !user.getPassword().startsWith("$2a$"))) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.debug("Deleting user by id: {}", id);
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        log.debug("Checking if username exists: {}", username);
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        log.debug("Checking if email exists: {}", email);
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findByRole(Role role) {
        log.debug("Finding users by role: {}", role);
        return StreamSupport
                .stream(userRepository.findByRole(role).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> searchUsers(String search) {
        log.debug("Searching users by: {}", search);
        return userRepository.findAll().stream()
                .filter(user -> 
                    user.getUsername().toLowerCase().contains(search.toLowerCase()) ||
                    user.getFirstName().toLowerCase().contains(search.toLowerCase()) ||
                    user.getLastName().toLowerCase().contains(search.toLowerCase()) ||
                    user.getEmail().toLowerCase().contains(search.toLowerCase())
                )
                .collect(Collectors.toList());
    }

    // NEW: Pagination implementations

    @Override
    @Transactional(readOnly = true)
    public Page<User> findAllPaginated(Pageable pageable) {
        log.debug("Finding all users with pagination: page {}, size {}", 
                 pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> findByRolePaginated(Role role, Pageable pageable) {
        log.debug("Finding users by role {} with pagination: page {}, size {}", 
                 role, pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findByRole(role, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> searchUsersPaginated(String search, Pageable pageable) {
        log.debug("Searching users by '{}' with pagination: page {}, size {}", 
                 search, pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findByUsernameContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                search, search, search, search, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        log.debug("Counting total users");
        return userRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByRole(Role role) {
        log.debug("Counting users by role: {}", role);
        return userRepository.countByRole(role);
    }
}