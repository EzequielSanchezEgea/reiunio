package com.ezequiel.reiunio.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ezequiel.reiunio.entity.Juego;
import com.ezequiel.reiunio.enums.EstadoJuego;

public interface JuegoService {
    
    List<Juego> findAll();
    
    Optional<Juego> findById(Long id);
    
    Juego save(Juego juego);
    
    void deleteById(Long id);
    
    List<Juego> findByNombre(String nombre);
    
    List<Juego> findByCategoria(String categoria);
    
    List<Juego> findByDisponible(Boolean disponible);
    
    List<Juego> findByEstado(EstadoJuego estado);
    
    List<Juego> findByNumeroJugadores(int numJugadores);
    
    // Nuevos métodos
    long count();
    
    long countByDisponible(Boolean disponible);
    
    Map<EstadoJuego, Long> countByEstado();
    
    Map<String, Long> countByCategoria();
    
    List<Object[]> findMostBorrowed(int limit);
    
    List<Juego> findNeverBorrowed();
}