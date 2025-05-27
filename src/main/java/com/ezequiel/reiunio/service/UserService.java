package com.ezequiel.reiunio.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.Role;

public interface UserService {
    
    List<User> findAll();
    
    Optional<User> findById(Long id);
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    User save(User user);
    
    void deleteById(Long id);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<User> findByRole(Role role);
    
    List<User> searchUsers(String search);
    
    // NEW: Pagination methods
    
    /**
     * Find all users with pagination
     */
    Page<User> findAllPaginated(Pageable pageable);
    
    /**
     * Find users by role with pagination
     */
    Page<User> findByRolePaginated(Role role, Pageable pageable);
    
    /**
     * Search users with pagination
     */
    Page<User> searchUsersPaginated(String search, Pageable pageable);
    
    /**
     * Count total users
     */
    long count();
    
    /**
     * Count users by role
     */
    long countByRole(Role role);
}