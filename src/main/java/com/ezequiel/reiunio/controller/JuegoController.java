package com.ezequiel.reiunio.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.ezequiel.reiunio.entity.Prestamo;
import com.ezequiel.reiunio.entity.Usuario;
import com.ezequiel.reiunio.enums.EstadoJuego;
import com.ezequiel.reiunio.enums.EstadoPartida;
import com.ezequiel.reiunio.enums.TipoAccion;
import com.ezequiel.reiunio.service.HistorialCambiosService;
import com.ezequiel.reiunio.service.JuegoService;
import com.ezequiel.reiunio.service.PartidaService;
import com.ezequiel.reiunio.service.PrestamoService;
import com.ezequiel.reiunio.service.UsuarioService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/juegos")
public class JuegoController {

    private final JuegoService juegoService;
    private final UsuarioService usuarioService;
    private final HistorialCambiosService historialCambiosService;
    private final PartidaService partidaService;
    private final PrestamoService prestamoService;

    @Autowired
    public JuegoController(JuegoService juegoService, UsuarioService usuarioService, 
                          HistorialCambiosService historialCambiosService,
                          PartidaService partidaService, PrestamoService prestamoService) {
        this.juegoService = juegoService;
        this.usuarioService = usuarioService;
        this.historialCambiosService = historialCambiosService;
        this.partidaService = partidaService;
        this.prestamoService = prestamoService;
    }

    // Listar todos los juegos
    @GetMapping
    public String listarJuegos(Model model, 
                              @RequestParam(required = false) String nombre,
                              @RequestParam(required = false) String categoria,
                              @RequestParam(required = false) Boolean disponible) {
        List<Juego> juegos;
        
        if (nombre != null && !nombre.isEmpty()) {
            juegos = juegoService.findByNombre(nombre);
            model.addAttribute("filtroNombre", nombre);
        } else if (categoria != null && !categoria.isEmpty()) {
            juegos = juegoService.findByCategoria(categoria);
            model.addAttribute("filtroCategoria", categoria);
        } else if (disponible != null) {
            juegos = juegoService.findByDisponible(disponible);
            model.addAttribute("filtroDisponible", disponible);
        } else {
            juegos = juegoService.findAll();
        }
        
        model.addAttribute("juegos", juegos);
        
        // Agregar estadísticas
        model.addAttribute("totalJuegos", juegoService.count());
        model.addAttribute("juegosDisponibles", juegoService.countByDisponible(true));
        model.addAttribute("juegosNodisponibles", juegoService.countByDisponible(false));
        
        return "juegos/lista";
    }

    // Ver detalles de un juego
    @GetMapping("/{id}")
    public String verJuego(@PathVariable Long id, Model model, Principal principal) {
        Optional<Juego> juegoOpt = juegoService.findById(id);
        
        if (juegoOpt.isPresent()) {
            Juego juego = juegoOpt.get();
            model.addAttribute("juego", juego);
            
            // Cargar las partidas programadas futuras para este juego
            List<Partida> proximasPartidas = partidaService.findByJuegoAndEstadoAndFechaFutura(
                juego, EstadoPartida.PROGRAMADA, LocalDate.now());
            model.addAttribute("proximasPartidas", proximasPartidas);
            
            // Si el usuario está autenticado, verificar si tiene préstamos activos de este juego
            if (principal != null) {
                Optional<Usuario> usuarioOpt = usuarioService.findByUsername(principal.getName());
                if (usuarioOpt.isPresent()) {
                    Usuario usuario = usuarioOpt.get();
                    List<Prestamo> prestamosActivos = prestamoService.findActivosByUsuarioAndJuego(usuario, juego);
                    model.addAttribute("tienePrestamo", !prestamosActivos.isEmpty());
                    model.addAttribute("prestamosUsuario", prestamosActivos);
                }
            }
            
            return "juegos/detalle";
        } else {
            return "redirect:/juegos";
        }
    }

    // Formulario para crear un nuevo juego (solo admin)
    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuevoJuegoForm(Model model) {
        model.addAttribute("juego", new Juego());
        model.addAttribute("estados", EstadoJuego.values());
        model.addAttribute("edicion", false);
        return "juegos/formulario";
    }

    // Procesar la creación de un nuevo juego
    @PostMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String crearJuego(@Valid @ModelAttribute("juego") Juego juego, BindingResult result, 
                           Model model, RedirectAttributes redirectAttributes, Principal principal) {
        if (result.hasErrors()) {
            model.addAttribute("estados", EstadoJuego.values());
            model.addAttribute("edicion", false);
            return "juegos/formulario";
        }
        
        // Validaciones adicionales
        if (juego.getMaxJugadores() < juego.getMinJugadores()) {
            result.rejectValue("maxJugadores", "error.juego", "El número máximo de jugadores debe ser mayor o igual al mínimo");
            model.addAttribute("estados", EstadoJuego.values());
            model.addAttribute("edicion", false);
            return "juegos/formulario";
        }
        
        // Establecer valores por defecto
        juego.setFechaAdquisicion(LocalDate.now());
        juego.setDisponible(true);
        
        // Guardar el juego
        juego = juegoService.save(juego);
        
        // Registrar en el historial
        Optional<Usuario> adminUsuario = usuarioService.findByUsername(principal.getName());
        if (adminUsuario.isPresent()) {
            historialCambiosService.registrarCambio(
                adminUsuario.get(),
                TipoAccion.CREACION,
                "Juego",
                juego.getId(),
                "Creación de juego: " + juego.getNombre()
            );
        }
        
        redirectAttributes.addFlashAttribute("mensaje", "¡Juego creado correctamente!");
        return "redirect:/juegos";
    }

    // Formulario para editar un juego existente
    @GetMapping("/{id}/editar")
    @PreAuthorize("hasRole('ADMIN')")
    public String editarJuegoForm(@PathVariable Long id, Model model) {
        Optional<Juego> juego = juegoService.findById(id);
        
        if (juego.isPresent()) {
            model.addAttribute("juego", juego.get());
            model.addAttribute("estados", EstadoJuego.values());
            model.addAttribute("edicion", true);
            return "juegos/formulario";
        } else {
            return "redirect:/juegos";
        }
    }

    // Procesar la actualización de un juego
    @PostMapping("/{id}/editar")
    @PreAuthorize("hasRole('ADMIN')")
    public String actualizarJuego(@PathVariable Long id, @Valid @ModelAttribute("juego") Juego juego,
                                BindingResult result, Model model, RedirectAttributes redirectAttributes,
                                Principal principal) {
        if (result.hasErrors()) {
            model.addAttribute("estados", EstadoJuego.values());
            model.addAttribute("edicion", true);
            return "juegos/formulario";
        }
        
        // Validaciones adicionales
        if (juego.getMaxJugadores() < juego.getMinJugadores()) {
            result.rejectValue("maxJugadores", "error.juego", "El número máximo de jugadores debe ser mayor o igual al mínimo");
            model.addAttribute("estados", EstadoJuego.values());
            model.addAttribute("edicion", true);
            return "juegos/formulario";
        }
        
        Optional<Juego> juegoExistente = juegoService.findById(id);
        
        if (juegoExistente.isPresent()) {
            Juego juegoActual = juegoExistente.get();
            
            // Mantener fecha de adquisición original
            juego.setFechaAdquisicion(juegoActual.getFechaAdquisicion());
            
            // Guardar los cambios
            juego = juegoService.save(juego);
            
            // Registrar en el historial
            Optional<Usuario> adminUsuario = usuarioService.findByUsername(principal.getName());
            if (adminUsuario.isPresent()) {
                historialCambiosService.registrarCambio(
                    adminUsuario.get(),
                    TipoAccion.MODIFICACION,
                    "Juego",
                    juego.getId(),
                    "Modificación de juego: " + juego.getNombre()
                );
            }
            
            redirectAttributes.addFlashAttribute("mensaje", "¡Juego actualizado correctamente!");
            return "redirect:/juegos/" + id;
        } else {
            return "redirect:/juegos";
        }
    }

    // Eliminar un juego
    @PostMapping("/{id}/eliminar")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminarJuego(@PathVariable Long id, RedirectAttributes redirectAttributes, Principal principal) {
        Optional<Juego> juego = juegoService.findById(id);
        
        if (juego.isPresent()) {
            // Verificar si el juego tiene préstamos activos
            List<Prestamo> prestamosActivos = prestamoService.findByJuegoAndActivos(juego.get());
            if (!prestamosActivos.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", 
                    "No se puede eliminar el juego porque tiene préstamos activos. " +
                    "Por favor, espere a que los préstamos sean devueltos.");
                return "redirect:/juegos/" + id;
            }
            
            String nombreEliminado = juego.get().getNombre();
            
            // Eliminar el juego
            juegoService.deleteById(id);
            
            // Registrar en el historial
            Optional<Usuario> adminUsuario = usuarioService.findByUsername(principal.getName());
            if (adminUsuario.isPresent()) {
                historialCambiosService.registrarCambio(
                    adminUsuario.get(),
                    TipoAccion.ELIMINACION,
                    "Juego",
                    id,
                    "Eliminación de juego: " + nombreEliminado
                );
            }
            
            redirectAttributes.addFlashAttribute("mensaje", "Juego eliminado correctamente");
        }
        
        return "redirect:/juegos";
    }

    // Filtrar juegos por criterios
    @GetMapping("/buscar")
    public String buscarJuegos(@RequestParam(required = false) String nombre,
                              @RequestParam(required = false) String categoria,
                              @RequestParam(required = false) Boolean disponible,
                              @RequestParam(required = false) Integer numJugadores,
                              Model model) {
        List<Juego> juegos;
        
        if (nombre != null && !nombre.isEmpty()) {
            juegos = juegoService.findByNombre(nombre);
            model.addAttribute("filtroNombre", nombre);
        } else if (categoria != null && !categoria.isEmpty()) {
            juegos = juegoService.findByCategoria(categoria);
            model.addAttribute("filtroCategoria", categoria);
        } else if (disponible != null) {
            juegos = juegoService.findByDisponible(disponible);
            model.addAttribute("filtroDisponible", disponible);
        } else if (numJugadores != null) {
            juegos = juegoService.findByNumeroJugadores(numJugadores);
            model.addAttribute("filtroNumJugadores", numJugadores);
        } else {
            juegos = juegoService.findAll();
        }
        
        model.addAttribute("juegos", juegos);
        return "juegos/lista";
    }

    // Cambiar la disponibilidad de un juego
    @PostMapping("/{id}/disponibilidad")
    @PreAuthorize("hasRole('ADMIN')")
    public String cambiarDisponibilidad(@PathVariable Long id, @RequestParam Boolean disponible,
                                      RedirectAttributes redirectAttributes, Principal principal) {
        Optional<Juego> juegoOpt = juegoService.findById(id);
        
        if (juegoOpt.isPresent()) {
            Juego juego = juegoOpt.get();
            
            // Verificar si el juego tiene préstamos activos antes de marcarlo como disponible
            if (disponible) {
                List<Prestamo> prestamosActivos = prestamoService.findByJuegoAndActivos(juego);
                if (!prestamosActivos.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", 
                        "No se puede marcar el juego como disponible porque tiene préstamos activos.");
                    return "redirect:/juegos/" + id;
                }
            }
            
            juego.setDisponible(disponible);
            juegoService.save(juego);
            
            // Registrar en el historial
            Optional<Usuario> adminUsuario = usuarioService.findByUsername(principal.getName());
            if (adminUsuario.isPresent()) {
                String accion = disponible ? "Marcado como disponible" : "Marcado como no disponible";
                historialCambiosService.registrarCambio(
                    adminUsuario.get(),
                    TipoAccion.MODIFICACION,
                    "Juego",
                    juego.getId(),
                    accion + ": " + juego.getNombre()
                );
            }
            
            String mensaje = disponible ? "Juego marcado como disponible" : "Juego marcado como no disponible";
            redirectAttributes.addFlashAttribute("mensaje", mensaje);
        }
        
        return "redirect:/juegos/" + id;
    }
    
    // Ver juegos por categoría
    @GetMapping("/categoria/{categoria}")
    public String verJuegosPorCategoria(@PathVariable String categoria, Model model) {
        List<Juego> juegos = juegoService.findByCategoria(categoria);
        model.addAttribute("juegos", juegos);
        model.addAttribute("filtroCategoria", categoria);
        model.addAttribute("tituloLista", "Juegos de categoría: " + categoria);
        return "juegos/lista";
    }
    
    // Ver juegos disponibles
    @GetMapping("/disponibles")
    public String verJuegosDisponibles(Model model) {
        List<Juego> juegos = juegoService.findByDisponible(true);
        model.addAttribute("juegos", juegos);
        model.addAttribute("filtroDisponible", true);
        model.addAttribute("tituloLista", "Juegos disponibles");
        return "juegos/lista";
    }
    
    // Ver juegos no disponibles
    @GetMapping("/no-disponibles")
    public String verJuegosNoDisponibles(Model model) {
        List<Juego> juegos = juegoService.findByDisponible(false);
        model.addAttribute("juegos", juegos);
        model.addAttribute("filtroDisponible", false);
        model.addAttribute("tituloLista", "Juegos no disponibles");
        return "juegos/lista";
    }
    
    // Ver estadísticas de juegos (para admin)
    @GetMapping("/estadisticas")
    @PreAuthorize("hasRole('ADMIN')")
    public String verEstadisticasJuegos(Model model) {
        model.addAttribute("totalJuegos", juegoService.count());
        model.addAttribute("juegosDisponibles", juegoService.countByDisponible(true));
        model.addAttribute("juegosNoDisponibles", juegoService.countByDisponible(false));
        model.addAttribute("juegosPorEstado", juegoService.countByEstado());
        model.addAttribute("juegosPorCategoria", juegoService.countByCategoria());
        model.addAttribute("juegosMasPrestados", juegoService.findMostBorrowed(10));
        model.addAttribute("juegosNuncaPrestados", juegoService.findNeverBorrowed());
        
        return "juegos/estadisticas";
    }
}