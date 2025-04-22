package com.ezequiel.reiunio.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ezequiel.reiunio.entity.Juego;
import com.ezequiel.reiunio.entity.Partida;
import com.ezequiel.reiunio.entity.Usuario;
import com.ezequiel.reiunio.enums.EstadoPartida;
import com.ezequiel.reiunio.enums.TipoAccion;
import com.ezequiel.reiunio.service.HistorialCambiosService;
import com.ezequiel.reiunio.service.JuegoService;
import com.ezequiel.reiunio.service.PartidaService;
import com.ezequiel.reiunio.service.UsuarioService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/partidas")
public class PartidaController {

    private final PartidaService partidaService;
    private final JuegoService juegoService;
    private final UsuarioService usuarioService;
    private final HistorialCambiosService historialCambiosService;

    @Autowired
    public PartidaController(PartidaService partidaService, JuegoService juegoService,
                            UsuarioService usuarioService, HistorialCambiosService historialCambiosService) {
        this.partidaService = partidaService;
        this.juegoService = juegoService;
        this.usuarioService = usuarioService;
        this.historialCambiosService = historialCambiosService;
    }

    // Listar todas las partidas
    @GetMapping
    public String listarPartidas(Model model, @RequestParam(required = false) String estado) {
        List<Partida> partidas;
        
        if (estado != null && !estado.isEmpty()) {
            try {
                EstadoPartida estadoEnum = EstadoPartida.valueOf(estado.toUpperCase());
                partidas = partidaService.findByEstado(estadoEnum);
                model.addAttribute("filtroEstado", estado);
            } catch (IllegalArgumentException e) {
                partidas = partidaService.findAll();
            }
        } else {
            partidas = partidaService.findAll();
        }
        
        model.addAttribute("partidas", partidas);
        model.addAttribute("estados", EstadoPartida.values());
        return "partidas/lista";
    }

    // Ver detalle de una partida
    @GetMapping("/{id}")
    public String verPartida(@PathVariable Long id, Model model, Principal principal) {
        Optional<Partida> partida = partidaService.findById(id);
        
        if (partida.isPresent()) {
            model.addAttribute("partida", partida.get());
            
            // Verificar si el usuario actual está inscrito en la partida
            Optional<Usuario> usuario = usuarioService.findByUsername(principal.getName());
            if (usuario.isPresent()) {
                boolean inscrito = partida.get().getJugadores().stream()
                        .anyMatch(jp -> jp.getUsuario().getId().equals(usuario.get().getId()));
                model.addAttribute("usuarioInscrito", inscrito);
            }
            
            return "partidas/detalle";
        } else {
            return "redirect:/partidas";
        }
    }

    // Formulario para crear una nueva partida
    @GetMapping("/nueva")
    public String nuevaPartida(Model model) {
        List<Juego> juegos = juegoService.findAll();
        
        model.addAttribute("partida", new Partida());
        model.addAttribute("juegos", juegos);
        return "partidas/formulario";
    }

    // Procesar la creación de una partida
    @PostMapping("/nueva")
    public String crearPartida(@Valid @ModelAttribute("partida") Partida partida,
                              BindingResult result,
                              @RequestParam Long juegoId,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaFin,
                              Model model,
                              RedirectAttributes redirectAttributes,
                              Principal principal) {
        
        if (result.hasErrors()) {
            model.addAttribute("juegos", juegoService.findAll());
            return "partidas/formulario";
        }
        
        Optional<Juego> juego = juegoService.findById(juegoId);
        Optional<Usuario> usuario = usuarioService.findByUsername(principal.getName());
        
        if (!juego.isPresent() || !usuario.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Juego o usuario no encontrado");
            return "redirect:/partidas/nueva";
        }
        
        partida.setJuego(juego.get());
        partida.setCreador(usuario.get());
        partida.setFecha(fecha);
        partida.setHoraInicio(horaInicio);
        partida.setHoraFin(horaFin);
        partida.setEstado(EstadoPartida.PROGRAMADA);
        
        partida = partidaService.save(partida);
        
        // Agregar al creador como primer jugador y confirmarlo
        partidaService.agregarJugadorAPartida(partida.getId(), usuario.get().getId());
        partidaService.confirmarJugadorEnPartida(partida.getId(), usuario.get().getId());
        
        // Registrar en el historial
        historialCambiosService.registrarCambio(
            usuario.get(),
            TipoAccion.CREACION,
            "Partida",
            partida.getId(),
            "Creación de partida: " + partida.getTitulo()
        );
        
        redirectAttributes.addFlashAttribute("mensaje", "Partida creada correctamente");
        return "redirect:/partidas";
    }
 // Formulario para editar una partida existente
    @GetMapping("/{id}/editar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USUARIO_EXTENDIDO') or @securityUtils.esCreadorPartida(#id, principal.username)")
    public String editarPartida(@PathVariable Long id, Model model) {
        Optional<Partida> partida = partidaService.findById(id);
        
        if (partida.isPresent()) {
            model.addAttribute("partida", partida.get());
            model.addAttribute("juegos", juegoService.findAll());
            model.addAttribute("edicion", true);
            return "partidas/formulario";
        } else {
            return "redirect:/partidas";
        }
    }

    // Procesar la actualización de una partida
    @PostMapping("/{id}/editar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USUARIO_EXTENDIDO') or @securityUtils.esCreadorPartida(#id, principal.username)")
    public String actualizarPartida(@PathVariable Long id,
                                  @Valid @ModelAttribute("partida") Partida partida,
                                  BindingResult result,
                                  @RequestParam Long juegoId,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaFin,
                                  Model model,
                                  RedirectAttributes redirectAttributes,
                                  Principal principal) {
        
        if (result.hasErrors()) {
            model.addAttribute("juegos", juegoService.findAll());
            model.addAttribute("edicion", true);
            return "partidas/formulario";
        }
        
        Optional<Partida> partidaExistente = partidaService.findById(id);
        Optional<Juego> juego = juegoService.findById(juegoId);
        Optional<Usuario> usuario = usuarioService.findByUsername(principal.getName());
        
        if (!partidaExistente.isPresent() || !juego.isPresent() || !usuario.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Partida, juego o usuario no encontrado");
            return "redirect:/partidas";
        }
        
        Partida partidaActual = partidaExistente.get();
        
        // Mantener el creador original
        partida.setId(id);
        partida.setCreador(partidaActual.getCreador());
        partida.setJuego(juego.get());
        partida.setFecha(fecha);
        partida.setHoraInicio(horaInicio);
        partida.setHoraFin(horaFin);
        
        // Mantener los jugadores originales
        partida.setJugadores(partidaActual.getJugadores());
        
        partida = partidaService.save(partida);
        
        // Registrar en el historial
        historialCambiosService.registrarCambio(
            usuario.get(),
            TipoAccion.MODIFICACION,
            "Partida",
            partida.getId(),
            "Modificación de partida: " + partida.getTitulo()
        );
        
        redirectAttributes.addFlashAttribute("mensaje", "Partida actualizada correctamente");
        return "redirect:/partidas";
    }

    // Cambiar el estado de una partida
    @PostMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USUARIO_EXTENDIDO') or @securityUtils.esCreadorPartida(#id, principal.username)")
    public String cambiarEstadoPartida(@PathVariable Long id,
                                     @RequestParam String estado,
                                     RedirectAttributes redirectAttributes,
                                     Principal principal) {
        
        Optional<Partida> partidaOpt = partidaService.findById(id);
        Optional<Usuario> usuarioOpt = usuarioService.findByUsername(principal.getName());
        
        if (partidaOpt.isPresent() && usuarioOpt.isPresent()) {
            Partida partida = partidaOpt.get();
            Usuario usuario = usuarioOpt.get();
            
            try {
                EstadoPartida nuevoEstado = EstadoPartida.valueOf(estado.toUpperCase());
                partida.setEstado(nuevoEstado);
                partidaService.save(partida);
                
                // Registrar en el historial
                historialCambiosService.registrarCambio(
                    usuario,
                    TipoAccion.MODIFICACION,
                    "Partida",
                    partida.getId(),
                    "Cambio de estado de partida a " + nuevoEstado + ": " + partida.getTitulo()
                );
                
                redirectAttributes.addFlashAttribute("mensaje", "Estado de la partida actualizado a " + nuevoEstado);
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("error", "Estado no válido");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Partida o usuario no encontrado");
        }
        
        return "redirect:/partidas";
    }

    // Eliminar una partida
    @PostMapping("/{id}/eliminar")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.esCreadorPartida(#id, principal.username)")
    public String eliminarPartida(@PathVariable Long id,
                                RedirectAttributes redirectAttributes,
                                Principal principal) {
        
        Optional<Partida> partidaOpt = partidaService.findById(id);
        Optional<Usuario> usuarioOpt = usuarioService.findByUsername(principal.getName());
        
        if (partidaOpt.isPresent() && usuarioOpt.isPresent()) {
            Partida partida = partidaOpt.get();
            Usuario usuario = usuarioOpt.get();
            String tituloPartida = partida.getTitulo();
            
            partidaService.deleteById(id);
            
            // Registrar en el historial
            historialCambiosService.registrarCambio(
                usuario,
                TipoAccion.ELIMINACION,
                "Partida",
                id,
                "Eliminación de partida: " + tituloPartida
            );
            
            redirectAttributes.addFlashAttribute("mensaje", "Partida eliminada correctamente");
        } else {
            redirectAttributes.addFlashAttribute("error", "Partida o usuario no encontrado");
        }
        
        return "redirect:/partidas";
    }

    // Unirse a una partida
    @PostMapping("/{id}/unirse")
    public String unirseAPartida(@PathVariable Long id,
                               RedirectAttributes redirectAttributes,
                               Principal principal) {
        
        Optional<Usuario> usuarioOpt = usuarioService.findByUsername(principal.getName());
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            
            boolean agregado = partidaService.agregarJugadorAPartida(id, usuario.getId());
            
            if (agregado) {
                redirectAttributes.addFlashAttribute("mensaje", "Te has unido a la partida correctamente");
                
                // Registrar en el historial
                Optional<Partida> partidaOpt = partidaService.findById(id);
                if (partidaOpt.isPresent()) {
                    historialCambiosService.registrarCambio(
                        usuario,
                        TipoAccion.MODIFICACION,
                        "Partida",
                        id,
                        "Unión a partida: " + partidaOpt.get().getTitulo()
                    );
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "No se pudo unir a la partida. La partida puede estar llena o ya estás inscrito.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
        }
        
        return "redirect:/partidas/" + id;
    }

    // Abandonar una partida
    @PostMapping("/{id}/abandonar")
    public String abandonarPartida(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes,
                                 Principal principal) {
        
        Optional<Usuario> usuarioOpt = usuarioService.findByUsername(principal.getName());
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            
            boolean eliminado = partidaService.eliminarJugadorDePartida(id, usuario.getId());
            
            if (eliminado) {
                redirectAttributes.addFlashAttribute("mensaje", "Has abandonado la partida correctamente");
                
                // Registrar en el historial
                Optional<Partida> partidaOpt = partidaService.findById(id);
                if (partidaOpt.isPresent()) {
                    historialCambiosService.registrarCambio(
                        usuario,
                        TipoAccion.MODIFICACION,
                        "Partida",
                        id,
                        "Abandono de partida: " + partidaOpt.get().getTitulo()
                    );
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "No se pudo abandonar la partida. Es posible que no estés inscrito.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
        }
        
        return "redirect:/partidas/" + id;
    }

    // Confirmar asistencia a una partida
    @PostMapping("/{id}/confirmar")
    public String confirmarAsistencia(@PathVariable Long id,
                                    RedirectAttributes redirectAttributes,
                                    Principal principal) {
        
        Optional<Usuario> usuarioOpt = usuarioService.findByUsername(principal.getName());
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            
            boolean confirmado = partidaService.confirmarJugadorEnPartida(id, usuario.getId());
            
            if (confirmado) {
                redirectAttributes.addFlashAttribute("mensaje", "Has confirmado tu asistencia correctamente");
                
                // Registrar en el historial
                Optional<Partida> partidaOpt = partidaService.findById(id);
                if (partidaOpt.isPresent()) {
                    historialCambiosService.registrarCambio(
                        usuario,
                        TipoAccion.MODIFICACION,
                        "Partida",
                        id,
                        "Confirmación de asistencia a partida: " + partidaOpt.get().getTitulo()
                    );
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "No se pudo confirmar la asistencia. La partida puede estar llena o no estás inscrito.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
        }
        
        return "redirect:/partidas/" + id;
    }

    // Ver partidas de hoy
    @GetMapping("/hoy")
    public String verPartidasHoy(Model model) {
        List<Partida> partidas = partidaService.findPartidasHoy();
        model.addAttribute("partidas", partidas);
        model.addAttribute("tituloLista", "Partidas de hoy");
        return "partidas/lista";
    }
    
    // Ver próximas partidas
    @GetMapping("/proximas")
    public String verProximasPartidas(Model model) {
        List<Partida> partidas = partidaService.findProximasPartidas();
        model.addAttribute("partidas", partidas);
        model.addAttribute("tituloLista", "Próximas partidas");
        return "partidas/lista";
    }
    
    // Ver mis partidas creadas
    @GetMapping("/mis-partidas")
    public String verMisPartidas(Model model, Principal principal) {
        Optional<Usuario> usuario = usuarioService.findByUsername(principal.getName());
        
        if (usuario.isPresent()) {
            List<Partida> partidas = partidaService.findByCreador(usuario.get());
            model.addAttribute("partidas", partidas);
            model.addAttribute("tituloLista", "Mis partidas creadas");
            return "partidas/lista";
        } else {
            return "redirect:/partidas";
        }
    }
    
    // Ver partidas en las que estoy inscrito
    @GetMapping("/mis-inscripciones")
    public String verMisInscripciones(Model model, Principal principal) {
        Optional<Usuario> usuario = usuarioService.findByUsername(principal.getName());
        
        if (usuario.isPresent()) {
            List<Partida> partidas = partidaService.findPartidasByJugador(usuario.get().getId());
            model.addAttribute("partidas", partidas);
            model.addAttribute("tituloLista", "Mis inscripciones a partidas");
            return "partidas/lista";
        } else {
            return "redirect:/partidas";
        }
    }
}
    
    