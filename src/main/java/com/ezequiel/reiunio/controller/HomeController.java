package com.ezequiel.reiunio.controller;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ezequiel.reiunio.entity.Juego;
import com.ezequiel.reiunio.entity.Partida;
import com.ezequiel.reiunio.entity.Usuario;
import com.ezequiel.reiunio.enums.EstadoPartida;
import com.ezequiel.reiunio.service.JuegoService;
import com.ezequiel.reiunio.service.PartidaService;
import com.ezequiel.reiunio.service.PrestamoService;
import com.ezequiel.reiunio.service.UsuarioService;

@Controller
public class HomeController {

    private final UsuarioService usuarioService;
    private final JuegoService juegoService;
    private final PartidaService partidaService;
    private final PrestamoService prestamoService;

    @Autowired
    public HomeController(UsuarioService usuarioService, JuegoService juegoService,
                         PartidaService partidaService, PrestamoService prestamoService) {
        this.usuarioService = usuarioService;
        this.juegoService = juegoService;
        this.partidaService = partidaService;
        this.prestamoService = prestamoService;
    }

    @GetMapping("/")
    public String home(Model model, Principal principal) {
        // Partidas para hoy
        List<Partida> partidasHoy = partidaService.findPartidasHoy();
        model.addAttribute("partidasHoy", partidasHoy);
        
        // Próximas partidas (programadas)
        List<Partida> proximasPartidas = partidaService.findByEstado(EstadoPartida.PROGRAMADA);
        model.addAttribute("proximasPartidas", proximasPartidas);
        
        // Juegos disponibles
        List<Juego> juegosDisponibles = juegoService.findByDisponible(true);
        model.addAttribute("juegosDisponibles", juegosDisponibles);
        
        // Si el usuario está autenticado, mostrar información personalizada
        if (principal != null) {
            Optional<Usuario> usuario = usuarioService.findByUsername(principal.getName());
            if (usuario.isPresent()) {
                model.addAttribute("usuario", usuario.get());
                
                // Partidas donde está inscrito el usuario
                List<Partida> misPartidas = partidaService.findPartidasByJugador(usuario.get().getId());
                model.addAttribute("misPartidas", misPartidas);
            }
        }
        
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/acceso-denegado")
    public String accesoDenegado() {
        return "error/acceso-denegado";
    }
}