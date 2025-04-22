package com.ezequiel.reiunio.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ezequiel.reiunio.entity.Juego;
import com.ezequiel.reiunio.enums.EstadoJuego;
import com.ezequiel.reiunio.repository.JuegoRepository;
import com.ezequiel.reiunio.repository.PrestamoRepository;
import com.ezequiel.reiunio.service.JuegoService;

@Service
public class JuegoServiceImpl implements JuegoService {

    private final JuegoRepository juegoRepository;
    private final PrestamoRepository prestamoRepository;
    
    @Autowired
    public JuegoServiceImpl(JuegoRepository juegoRepository, PrestamoRepository prestamoRepository) {
        this.juegoRepository = juegoRepository;
        this.prestamoRepository = prestamoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Juego> findAll() {
        return juegoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Juego> findById(Long id) {
        return juegoRepository.findById(id);
    }

    @Override
    @Transactional
    public Juego save(Juego juego) {
        return juegoRepository.save(juego);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        juegoRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Juego> findByNombre(String nombre) {
        return juegoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Juego> findByCategoria(String categoria) {
        return juegoRepository.findByCategoriaContainingIgnoreCase(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Juego> findByDisponible(Boolean disponible) {
        return juegoRepository.findByDisponible(disponible);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Juego> findByEstado(EstadoJuego estado) {
        return juegoRepository.findByEstado(estado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Juego> findByNumeroJugadores(int numJugadores) {
        return juegoRepository.findByMinJugadoresLessThanEqualAndMaxJugadoresGreaterThanEqual(
                numJugadores, numJugadores);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long count() {
        return juegoRepository.count();
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countByDisponible(Boolean disponible) {
        return juegoRepository.countByDisponible(disponible);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<EstadoJuego, Long> countByEstado() {
        Map<EstadoJuego, Long> resultado = new HashMap<>();
        
        for (EstadoJuego estado : EstadoJuego.values()) {
            long count = juegoRepository.countByEstado(estado);
            resultado.put(estado, count);
        }
        
        return resultado;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> countByCategoria() {
        List<Juego> todosLosJuegos = juegoRepository.findAll();
        
        return todosLosJuegos.stream()
                .filter(juego -> juego.getCategoria() != null && !juego.getCategoria().isEmpty())
                .collect(Collectors.groupingBy(
                    Juego::getCategoria,
                    Collectors.counting()
                ));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> findMostBorrowed(int limit) {
        return juegoRepository.findMostBorrowedJuegos(limit);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Juego> findNeverBorrowed() {
        return juegoRepository.findJuegosNeverBorrowed();
    }
}