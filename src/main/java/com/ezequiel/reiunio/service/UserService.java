package com.ezequiel.reiunio.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.Role;

/**
 * Service interface for managing user operations.
 */
public interface UserService {

    /**
     * Retrieves all users from the repository.
     *
     * @return a list of all users
     */
    List<User> findAll();

    /**
     * Finds a user by their ID.
     *
     * @param id the user's ID
     * @return an optional containing the user if found, or empty otherwise
     */
    Optional<User> findById(Long id);

    /**
     * Finds a user by their username.
     *
     * @param username the user's username
     * @return an optional containing the user if found, or empty otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their email address.
     *
     * @param email the user's email
     * @return an optional containing the user if found, or empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Saves a user to the repository.
     *
     * @param user the user entity to save
     * @return the saved user
     */
    User save(User user);

    /**
     * Deletes a user by their ID.
     *
     * @param id the ID of the user to delete
     */
    void deleteById(Long id);

    /**
     * Checks whether a username already exists.
     *
     * @param username the username to check
     * @return true if the username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Checks whether an email already exists.
     *
     * @param email the email to check
     * @return true if the email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Finds users by role.
     *
     * @param role the role to filter users by
     * @return a list of users with the specified role
     */
    List<User> findByRole(Role role);

    /**
     * Searches users based on a search string.
     *
     * @param search the search query
     * @return a list of matching users
     */
    List<User> searchUsers(String search);

    /**
     * Retrieves a paginated list of all users.
     *
     * @param pageable pagination configuration
     * @return a page of users
     */
    Page<User> findAllPaginated(Pageable pageable);

    /**
     * Retrieves a paginated list of users with a specific role.
     *
     * @param role the role to filter by
     * @param pageable pagination configuration
     * @return a page of users matching the role
     */
    Page<User> findByRolePaginated(Role role, Pageable pageable);

    /**
     * Searches users with pagination support.
     *
     * @param search the search query
     * @param pageable pagination configuration
     * @return a page of matching users
     */
    Page<User> searchUsersPaginated(String search, Pageable pageable);

    /**
     * Searches users by role and search term combination.
     *
     * @param role the role to filter by
     * @param searchTerm the term to search in all fields
     * @param pageable pagination information
     * @return a page of matching users
     */
    Page<User> searchUsersByRoleAndTerm(Role role, String searchTerm, Pageable pageable);

    /**
     * Returns the total number of users.
     *
     * @return the total user count
     */
    long count();

    /**
     * Returns the number of users by a specific role.
     *
     * @param role the role to filter by
     * @return the number of users with the specified role
     */
    long countByRole(Role role);
}