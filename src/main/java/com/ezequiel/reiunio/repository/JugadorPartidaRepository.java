package com.ezequiel.reiunio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ezequiel.reiunio.entity.JugadorPartida;
import com.ezequiel.reiunio.entity.JugadorPartidaId;
import com.ezequiel.reiunio.entity.Partida;
import com.ezequiel.reiunio.entity.Usuario;

@Repository
public interface JugadorPartidaRepository extends JpaRepository<JugadorPartida, JugadorPartidaId> {
    
    List<JugadorPartida> findByUsuario(Usuario usuario);
    
    List<JugadorPartida> findByPartida(Partida partida);
    
    List<JugadorPartida> findByPartidaAndConfirmado(Partida partida, Boolean confirmado);
    
    List<JugadorPartida> findByUsuarioAndConfirmado(Usuario usuario, Boolean confirmado);
    
    long countByPartidaAndConfirmado(Partida partida, Boolean confirmado);
}