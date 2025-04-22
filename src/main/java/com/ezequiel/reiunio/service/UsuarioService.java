package com.ezequiel.reiunio.service;

import java.util.List;
import java.util.Optional;

import com.ezequiel.reiunio.entity.Usuario;
import com.ezequiel.reiunio.enums.Rol;

public interface UsuarioService {
    
    List<Usuario> findAll();
    
    Optional<Usuario> findById(Long id);
    
    Optional<Usuario> findByUsername(String username);
    
    Optional<Usuario> findByEmail(String email);
    
    Usuario save(Usuario usuario);
    
    void deleteById(Long id);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<Usuario> findByRol(Rol rol);
}