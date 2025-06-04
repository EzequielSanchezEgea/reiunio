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

/**
 * Implementation of the {@link UserService} interface that handles all user-related business logic.
 * Provides methods for user persistence, lookup, search, and pagination.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Retrieves all users from the repository.
     *
     * @return a list of all users
     */
    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        log.debug("Finding all users");
        return userRepository.findAll();
    }

    /**
     * Finds a user by their ID.
     *
     * @param id the user's ID
     * @return an optional containing the user if found, or empty otherwise
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        log.debug("Finding user by id: {}", id);
        return userRepository.findById(id);
    }

    /**
     * Finds a user by their username.
     *
     * @param username the user's username
     * @return an optional containing the user if found, or empty otherwise
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        return userRepository.findByUsername(username);
    }

    /**
     * Finds a user by their email address.
     *
     * @param email the user's email
     * @return an optional containing the user if found, or empty otherwise
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    /**
     * Saves a user to the repository. Encrypts the password if it's not already encoded.
     *
     * @param user the user entity to save
     * @return the saved user
     */
    @Override
    @Transactional
    public User save(User user) {
        log.debug("Saving user: {}", user.getUsername());

        if (user.getId() == null || (user.getPassword() != null && !user.getPassword().startsWith("$2a$"))) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id the ID of the user to delete
     */
    @Override
    @Transactional
    public void deleteById(Long id) {
        log.debug("Deleting user by id: {}", id);
        userRepository.deleteById(id);
    }

    /**
     * Checks whether a username already exists.
     *
     * @param username the username to check
     * @return true if the username exists, false otherwise
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        log.debug("Checking if username exists: {}", username);
        return userRepository.existsByUsername(username);
    }

    /**
     * Checks whether an email already exists.
     *
     * @param email the email to check
     * @return true if the email exists, false otherwise
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        log.debug("Checking if email exists: {}", email);
        return userRepository.existsByEmail(email);
    }

    /**
     * Finds users by role.
     *
     * @param role the role to filter users by
     * @return a list of users with the specified role
     */
    @Override
    @Transactional(readOnly = true)
    public List<User> findByRole(Role role) {
        log.debug("Finding users by role: {}", role);
        return StreamSupport
                .stream(userRepository.findByRole(role).spliterator(), false)
                .collect(Collectors.toList());
    }

    /**
     * Searches users based on a search string matching username, first name, last name, or email.
     *
     * @param search the search query
     * @return a list of matching users
     */
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

    /**
     * Retrieves a paginated list of all users.
     *
     * @param pageable pagination configuration
     * @return a page of users
     */
    @Override
    @Transactional(readOnly = true)
    public Page<User> findAllPaginated(Pageable pageable) {
        log.debug("Finding all users with pagination: page {}, size {}", 
                 pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findAll(pageable);
    }

    /**
     * Retrieves a paginated list of users with a specific role.
     *
     * @param role the role to filter by
     * @param pageable pagination configuration
     * @return a page of users matching the role
     */
    @Override
    @Transactional(readOnly = true)
    public Page<User> findByRolePaginated(Role role, Pageable pageable) {
        log.debug("Finding users by role {} with pagination: page {}, size {}", 
                 role, pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findByRole(role, pageable);
    }

    /**
     * Searches users with pagination support.
     *
     * @param search the search query
     * @param pageable pagination configuration
     * @return a page of matching users
     */
    @Override
    @Transactional(readOnly = true)
    public Page<User> searchUsersPaginated(String search, Pageable pageable) {
        log.debug("Searching users by '{}' with pagination: page {}, size {}", 
                 search, pageable.getPageNumber(), pageable.getPageSize());
        
        if (search == null || search.trim().isEmpty()) {
            return findAllPaginated(pageable);
        }
        
        return userRepository.searchUsers(search.trim(), pageable);
    }

    /**
     * Searches users by role and search term combination.
     *
     * @param role the role to filter by
     * @param searchTerm the term to search in all fields
     * @param pageable pagination information
     * @return a page of matching users
     */
    @Override
    @Transactional(readOnly = true)
    public Page<User> searchUsersByRoleAndTerm(Role role, String searchTerm, Pageable pageable) {
        log.debug("Searching users by role {} and term '{}' with pagination: page {}, size {}", 
                 role, searchTerm, pageable.getPageNumber(), pageable.getPageSize());
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findByRolePaginated(role, pageable);
        }
        
        return userRepository.searchUsersByRoleAndTerm(role, searchTerm.trim(), pageable);
    }

    /**
     * Counts the total number of users.
     *
     * @return the total number of users
     */
    @Override
    @Transactional(readOnly = true)
    public long count() {
        log.debug("Counting total users");
        return userRepository.count();
    }

    /**
     * Counts the number of users by a specific role.
     *
     * @param role the role to filter by
     * @return the number of users with the specified role
     */
    @Override
    @Transactional(readOnly = true)
    public long countByRole(Role role) {
        log.debug("Counting users by role: {}", role);
        return userRepository.countByRole(role);
    }
}