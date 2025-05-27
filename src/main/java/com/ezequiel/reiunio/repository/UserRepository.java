package com.ezequiel.reiunio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.Role;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    Iterable<User> findByRole(Role role);
    
    List<User> findByUsernameContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String username, String firstName, String lastName);
    
    // NEW: Pagination methods
    
    /**
     * Find users by role with pagination
     */
    Page<User> findByRole(Role role, Pageable pageable);
    
    /**
     * Search users with pagination
     */
    Page<User> findByUsernameContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String username, String firstName, String lastName, String email, Pageable pageable);
    
    /**
     * Count users by role
     */
    long countByRole(Role role);
}