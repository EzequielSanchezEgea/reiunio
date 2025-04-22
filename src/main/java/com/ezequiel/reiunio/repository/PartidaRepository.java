package com.ezequiel.reiunio.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ezequiel.reiunio.entity.Juego;
import com.ezequiel.reiunio.entity.Partida;
import com.ezequiel.reiunio.entity.Usuario;
import com.ezequiel.reiunio.enums.EstadoPartida;

@Repository
public interface PartidaRepository extends JpaRepository<Partida, Long> {
    
    List<Partida> findByCreador(Usuario creador);
    
    List<Partida> findByJuego(Juego juego);
    
    List<Partida> findByEstado(EstadoPartida estado);
    
    List<Partida> findByFechaAfterAndEstado(LocalDate fecha, EstadoPartida estado);
    
    List<Partida> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin);
    
    @Query("SELECT p FROM Partida p JOIN p.jugadores j WHERE j.usuario.id = :usuarioId")
    List<Partida> findPartidasByJugador(@Param("usuarioId") Long usuarioId);
    
    @Query("SELECT p FROM Partida p WHERE p.fecha = CURRENT_DATE")
    List<Partida> findPartidasHoy();
    
    // Nuevo método para buscar partidas por juego, estado y fecha futura
    List<Partida> findByJuegoAndEstadoAndFechaGreaterThanEqual(Juego juego, EstadoPartida estado, LocalDate fecha);
}