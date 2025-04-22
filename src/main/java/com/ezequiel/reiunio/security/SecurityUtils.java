package com.ezequiel.reiunio.security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ezequiel.reiunio.entity.Partida;
import com.ezequiel.reiunio.entity.Prestamo;
import com.ezequiel.reiunio.service.PartidaService;
import com.ezequiel.reiunio.service.PrestamoService;
import com.ezequiel.reiunio.service.UsuarioService;

@Component
public class SecurityUtils {

    private final PartidaService partidaService;
    private final PrestamoService prestamoService;
    private final UsuarioService usuarioService;

    @Autowired
    public SecurityUtils(PartidaService partidaService, PrestamoService prestamoService, UsuarioService usuarioService) {
        this.partidaService = partidaService;
        this.prestamoService = prestamoService;
        this.usuarioService = usuarioService;
    }

    /**
     * Verifica si un usuario es el creador de una partida
     * @param partidaId ID de la partida
     * @param username Nombre de usuario
     * @return true si el usuario es el creador de la partida, false en caso contrario
     */
    public boolean esCreadorPartida(Long partidaId, String username) {
        Optional<Partida> partida = partidaService.findById(partidaId);
        return partida.isPresent() && partida.get().getCreador().getUsername().equals(username);
    }

    /**
     * Verifica si un préstamo pertenece a un usuario
     * @param prestamoId ID del préstamo
     * @param username Nombre de usuario
     * @return true si el préstamo pertenece al usuario, false en caso contrario
     */
    public boolean esPrestamoDeUsuario(Long prestamoId, String username) {
        Optional<Prestamo> prestamo = prestamoService.findById(prestamoId);
        return prestamo.isPresent() && prestamo.get().getUsuario().getUsername().equals(username);
    }
}