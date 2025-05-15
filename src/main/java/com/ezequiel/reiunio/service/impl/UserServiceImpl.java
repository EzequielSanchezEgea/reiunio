package com.ezequiel.reiunio.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
}