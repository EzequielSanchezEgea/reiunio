package com.ezequiel.reiunio.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.ezequiel.reiunio.entity.Juego;
import com.ezequiel.reiunio.entity.Partida;
import com.ezequiel.reiunio.entity.Usuario;
import com.ezequiel.reiunio.enums.EstadoPartida;

public interface PartidaService {
    
    List<Partida> findAll();
    
    Optional<Partida> findById(Long id);
    
    Partida save(Partida partida);
    
    void deleteById(Long id);
    
    List<Partida> findByCreador(Usuario creador);
    
    List<Partida> findByJuego(Juego juego);
    
    List<Partida> findByEstado(EstadoPartida estado);
    
    List<Partida> findProximasPartidas();
    
    List<Partida> findPartidasEntreFechas(LocalDate fechaInicio, LocalDate fechaFin);
    
    List<Partida> findPartidasByJugador(Long usuarioId);
    
    List<Partida> findPartidasHoy();
    
    // Nuevo método para buscar partidas por juego, estado y fecha futura
    List<Partida> findByJuegoAndEstadoAndFechaFutura(Juego juego, EstadoPartida estado, LocalDate fecha);
    
    boolean agregarJugadorAPartida(Long partidaId, Long usuarioId);
    
    boolean eliminarJugadorDePartida(Long partidaId, Long usuarioId);
    
    boolean confirmarJugadorEnPartida(Long partidaId, Long usuarioId);
}