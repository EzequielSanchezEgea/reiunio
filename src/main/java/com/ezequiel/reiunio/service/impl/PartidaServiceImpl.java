package com.ezequiel.reiunio.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ezequiel.reiunio.entity.Juego;
import com.ezequiel.reiunio.entity.JugadorPartida;
import com.ezequiel.reiunio.entity.JugadorPartidaId;
import com.ezequiel.reiunio.entity.Partida;
import com.ezequiel.reiunio.entity.Usuario;
import com.ezequiel.reiunio.enums.EstadoPartida;
import com.ezequiel.reiunio.repository.JugadorPartidaRepository;
import com.ezequiel.reiunio.repository.PartidaRepository;
import com.ezequiel.reiunio.repository.UsuarioRepository;
import com.ezequiel.reiunio.service.PartidaService;

@Service
public class PartidaServiceImpl implements PartidaService {

    private final PartidaRepository partidaRepository;
    private final UsuarioRepository usuarioRepository;
    private final JugadorPartidaRepository jugadorPartidaRepository;
    
    @Autowired
    public PartidaServiceImpl(PartidaRepository partidaRepository, 
                             UsuarioRepository usuarioRepository,
                             JugadorPartidaRepository jugadorPartidaRepository) {
        this.partidaRepository = partidaRepository;
        this.usuarioRepository = usuarioRepository;
        this.jugadorPartidaRepository = jugadorPartidaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Partida> findAll() {
        return partidaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Partida> findById(Long id) {
        return partidaRepository.findById(id);
    }

    @Override
    @Transactional
    public Partida save(Partida partida) {
        // Si es una partida nueva, asegurarse de que su estado sea PROGRAMADA
        if (partida.getId() == null) {
            partida.setEstado(EstadoPartida.PROGRAMADA);
        }
        return partidaRepository.save(partida);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        partidaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Partida> findByCreador(Usuario creador) {
        return partidaRepository.findByCreador(creador);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Partida> findByJuego(Juego juego) {
        return partidaRepository.findByJuego(juego);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Partida> findByEstado(EstadoPartida estado) {
        return partidaRepository.findByEstado(estado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Partida> findProximasPartidas() {
        return partidaRepository.findByFechaAfterAndEstado(LocalDate.now(), EstadoPartida.PROGRAMADA);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Partida> findPartidasEntreFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        return partidaRepository.findByFechaBetween(fechaInicio, fechaFin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Partida> findPartidasByJugador(Long usuarioId) {
        return partidaRepository.findPartidasByJugador(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Partida> findPartidasHoy() {
        return partidaRepository.findPartidasHoy();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Partida> findByJuegoAndEstadoAndFechaFutura(Juego juego, EstadoPartida estado, LocalDate fecha) {
        // Utilizando el método del repositorio que coincide con la funcionalidad requerida
        return partidaRepository.findByJuegoAndEstadoAndFechaGreaterThanEqual(juego, estado, fecha);
    }

    @Override
    @Transactional
    public boolean agregarJugadorAPartida(Long partidaId, Long usuarioId) {
        Optional<Partida> optPartida = partidaRepository.findById(partidaId);
        Optional<Usuario> optUsuario = usuarioRepository.findById(usuarioId);
        
        if (optPartida.isPresent() && optUsuario.isPresent()) {
            Partida partida = optPartida.get();
            Usuario usuario = optUsuario.get();
            
            // Verificar si el jugador ya está en la partida
            boolean yaRegistrado = jugadorPartidaRepository.existsById(new JugadorPartidaId(partidaId, usuarioId));
            if (yaRegistrado) {
                return false;
            }
            
            // Verificar si hay espacio disponible
            long jugadoresConfirmados = jugadorPartidaRepository.countByPartidaAndConfirmado(partida, true);
            if (jugadoresConfirmados >= partida.getMaxJugadores()) {
                return false;
            }
            
            // Agregar el jugador
            JugadorPartida jugadorPartida = new JugadorPartida();
            jugadorPartida.setPartida(partida);
            jugadorPartida.setUsuario(usuario);
            jugadorPartida.setFechaUnion(LocalDate.now());
            jugadorPartida.setConfirmado(false);
            
            jugadorPartidaRepository.save(jugadorPartida);
            return true;
        }
        
        return false;
    }

    @Override
    @Transactional
    public boolean eliminarJugadorDePartida(Long partidaId, Long usuarioId) {
        Optional<JugadorPartida> optJugadorPartida = jugadorPartidaRepository.findById(
                new JugadorPartidaId(partidaId, usuarioId));
        
        if (optJugadorPartida.isPresent()) {
            jugadorPartidaRepository.delete(optJugadorPartida.get());
            return true;
        }
        
        return false;
    }

    @Override
    @Transactional
    public boolean confirmarJugadorEnPartida(Long partidaId, Long usuarioId) {
        Optional<JugadorPartida> optJugadorPartida = jugadorPartidaRepository.findById(
                new JugadorPartidaId(partidaId, usuarioId));
        
        if (optJugadorPartida.isPresent()) {
            JugadorPartida jugadorPartida = optJugadorPartida.get();
            
            // Verificar si hay espacio disponible para confirmación
            Partida partida = jugadorPartida.getPartida();
            long jugadoresConfirmados = jugadorPartidaRepository.countByPartidaAndConfirmado(partida, true);
            
            if (jugadoresConfirmados >= partida.getMaxJugadores()) {
                return false;
            }
            
            jugadorPartida.setConfirmado(true);
            jugadorPartidaRepository.save(jugadorPartida);
            return true;
        }
        
        return false;
    }
}