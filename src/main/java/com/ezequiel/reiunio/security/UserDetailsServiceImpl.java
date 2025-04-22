package com.ezequiel.reiunio.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ezequiel.reiunio.entity.Usuario;
import com.ezequiel.reiunio.repository.UsuarioRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Autowired
    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        
        if (!usuarioOpt.isPresent()) {
            throw new UsernameNotFoundException("Usuario no encontrado: " + username);
        }
        
        Usuario usuario = usuarioOpt.get();
        
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
        
        return new User(
            usuario.getUsername(),
            usuario.getPassword(),
            true,  // enabled
            true,  // accountNonExpired
            true,  // credentialsNonExpired
            true,  // accountNonLocked
            authorities
        );
    }
}