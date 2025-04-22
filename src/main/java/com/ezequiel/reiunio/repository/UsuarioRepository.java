package com.ezequiel.reiunio.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ezequiel.reiunio.entity.Usuario;
import com.ezequiel.reiunio.enums.Rol;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByUsername(String username);
    
    Optional<Usuario> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    Iterable<Usuario> findByRol(Rol rol);
}