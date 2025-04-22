package com.ezequiel.reiunio.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ezequiel.reiunio.entity.Usuario;
import com.ezequiel.reiunio.enums.Rol;
import com.ezequiel.reiunio.repository.UsuarioRepository;
import com.ezequiel.reiunio.service.UsuarioService;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public Usuario save(Usuario usuario) {
        // Cifrar la contraseña antes de guardar el usuario
        if (usuario.getId() == null || (usuario.getPassword() != null && !usuario.getPassword().startsWith("$2a$"))) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        usuarioRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> findByRol(Rol rol) {
        return StreamSupport
                .stream(usuarioRepository.findByRol(rol).spliterator(), false)
                .collect(Collectors.toList());
    }
}