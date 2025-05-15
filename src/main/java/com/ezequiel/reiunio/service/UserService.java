package com.ezequiel.reiunio.service;

import java.util.List;
import java.util.Optional;

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
}