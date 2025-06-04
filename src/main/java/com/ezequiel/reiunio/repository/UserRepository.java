package com.ezequiel.reiunio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.Role;

/**
 * Repository interface for managing {@link User} entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their username.
     *
     * @param username the username
     * @return an Optional containing the user, if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their email address.
     *
     * @param email the user's email
     * @return an Optional containing the user, if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists with the given username.
     *
     * @param username the username to check
     * @return true if a user exists with that username, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Checks if a user exists with the given email.
     *
     * @param email the email to check
     * @return true if a user exists with that email, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Finds all users with a specific role.
     *
     * @param role the role to search for
     * @return iterable of users with the specified role
     */
    Iterable<User> findByRole(Role role);

    /**
     * Searches for users where the username, first name, or last name contains the given input (case-insensitive).
     *
     * @param username part of the username to search for
     * @param firstName part of the first name to search for
     * @param lastName part of the last name to search for
     * @return list of matching users
     */
    List<User> findByUsernameContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
        String username, String firstName, String lastName);

    /**
     * Finds users by role with pagination support.
     *
     * @param role the role to filter users by
     * @param pageable pagination information
     * @return paginated list of users with the given role
     */
    Page<User> findByRole(Role role, Pageable pageable);

    /**
     * Searches users by username, first name, last name, or email with pagination support (case-insensitive).
     * CORREGIDO: Usa una sola query con OR conditions para búsqueda más eficiente.
     *
     * @param searchTerm the term to search in all fields
     * @param pageable pagination information
     * @return paginated list of matching users
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Busca usuarios por rol Y término de búsqueda combinados.
     * NUEVO: Permite filtrar por rol específico y buscar dentro de ese subconjunto.
     *
     * @param role the role to filter by
     * @param searchTerm the term to search in all fields
     * @param pageable pagination information
     * @return paginated list of matching users
     */
    @Query("SELECT u FROM User u WHERE " +
           "u.role = :role AND (" +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<User> searchUsersByRoleAndTerm(@Param("role") Role role, 
                                       @Param("searchTerm") String searchTerm, 
                                       Pageable pageable);

    /**
     * Método mantenido para compatibilidad con el controlador existente
     * @deprecated Use searchUsers(String, Pageable) instead
     */
    @Deprecated
    default Page<User> findByUsernameContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String username, String firstName, String lastName, String email, Pageable pageable) {
        // Usar el nuevo método optimizado
        return searchUsers(username, pageable);
    }

    /**
     * Counts how many users have the given role.
     *
     * @param role the role to count users for
     * @return number of users with that role
     */
    long countByRole(Role role);
}