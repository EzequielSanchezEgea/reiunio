package com.ezequiel.reiunio.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ezequiel.reiunio.entity.Juego;
import com.ezequiel.reiunio.entity.Prestamo;
import com.ezequiel.reiunio.entity.Usuario;
import com.ezequiel.reiunio.enums.EstadoPrestamo;
import com.ezequiel.reiunio.enums.TipoAccion;
import com.ezequiel.reiunio.service.HistorialCambiosService;
import com.ezequiel.reiunio.service.JuegoService;
import com.ezequiel.reiunio.service.PrestamoService;
import com.ezequiel.reiunio.service.UsuarioService;

@Controller
@RequestMapping("/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;
    private final JuegoService juegoService;
    private final UsuarioService usuarioService;
    private final HistorialCambiosService historialCambiosService;

    @Autowired
    public PrestamoController(PrestamoService prestamoService, JuegoService juegoService,
                             UsuarioService usuarioService, HistorialCambiosService historialCambiosService) {
        this.prestamoService = prestamoService;
        this.juegoService = juegoService;
        this.usuarioService = usuarioService;
        this.historialCambiosService = historialCambiosService;
    }

    // Listar todos los préstamos (solo admin y usuario con permisos extendidos)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USUARIO_EXTENDIDO')")
    public String listarPrestamos(Model model, @RequestParam(required = false) String estado) {
        List<Prestamo> prestamos;
        
        if (estado != null && !estado.isEmpty()) {
            try {
                EstadoPrestamo estadoEnum = EstadoPrestamo.valueOf(estado.toUpperCase());
                prestamos = prestamoService.findByEstado(estadoEnum);
                model.addAttribute("filtroEstado", estado);
            } catch (IllegalArgumentException e) {
                prestamos = prestamoService.findAll();
            }
        } else {
            prestamos = prestamoService.findAll();
        }
        
        model.addAttribute("prestamos", prestamos);
        model.addAttribute("estados", EstadoPrestamo.values());
        return "prestamos/lista";
    }

    // Ver detalle de un préstamo
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USUARIO_EXTENDIDO') or @securityUtils.esPrestamoDeUsuario(#id, principal.username)")
    public String verPrestamo(@PathVariable Long id, Model model) {
        Optional<Prestamo> prestamo = prestamoService.findById(id);
        
        if (prestamo.isPresent()) {
            model.addAttribute("prestamo", prestamo.get());
            return "prestamos/detalle";
        } else {
            return "redirect:/prestamos";
        }
    }

    // Formulario para crear un nuevo préstamo
    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USUARIO_EXTENDIDO')")
    public String nuevoPrestamo(Model model) {
        List<Usuario> usuarios = usuarioService.findAll();
        List<Juego> juegos = juegoService.findByDisponible(true);
        
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("juegos", juegos);
        return "prestamos/formulario";
    }

    // Procesar la creación de un préstamo
    @PostMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USUARIO_EXTENDIDO')")
    public String crearPrestamo(@RequestParam Long usuarioId,
                               @RequestParam Long juegoId,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDevolucionEstimada,
                               RedirectAttributes redirectAttributes,
                               Principal principal) {
        
        Optional<Usuario> usuario = usuarioService.findById(usuarioId);
        Optional<Juego> juego = juegoService.findById(juegoId);
        
        if (!usuario.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
            return "redirect:/prestamos/nuevo";
        }
        
        if (!juego.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Juego no encontrado");
            return "redirect:/prestamos/nuevo";
        }
        
        if (!juego.get().getDisponible()) {
            redirectAttributes.addFlashAttribute("error", "El juego no está disponible para préstamo");
            return "redirect:/prestamos/nuevo";
        }
        
        try {
            Prestamo prestamo = prestamoService.realizarPrestamo(usuario.get(), juego.get(), fechaDevolucionEstimada);
            
            // Registrar en el historial
            Optional<Usuario> adminUsuario = usuarioService.findByUsername(principal.getName());
            if (adminUsuario.isPresent()) {
                historialCambiosService.registrarCambio(
                    adminUsuario.get(),
                    TipoAccion.CREACION,
                    "Prestamo",
                    prestamo.getId(),
                    "Préstamo del juego " + juego.get().getNombre() + " al usuario " + usuario.get().getUsername()
                );
            }
            
            redirectAttributes.addFlashAttribute("mensaje", "Préstamo registrado correctamente");
            return "redirect:/prestamos";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/prestamos/nuevo";
        }
    }

    // Registrar devolución de un préstamo
    @PostMapping("/{id}/devolucion")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USUARIO_EXTENDIDO')")
    public String registrarDevolucion(@PathVariable Long id,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDevolucion,
                                    RedirectAttributes redirectAttributes,
                                    Principal principal) {
        
        if (fechaDevolucion == null) {
            fechaDevolucion = LocalDate.now();
        }
        
        try {
            Prestamo prestamo = prestamoService.registrarDevolucion(id, fechaDevolucion);
            
            // Registrar en el historial
            Optional<Usuario> adminUsuario = usuarioService.findByUsername(principal.getName());
            if (adminUsuario.isPresent()) {
                historialCambiosService.registrarCambio(
                    adminUsuario.get(),
                    TipoAccion.MODIFICACION,
                    "Prestamo",
                    prestamo.getId(),
                    "Devolución del juego " + prestamo.getJuego().getNombre() + " por el usuario " + prestamo.getUsuario().getUsername()
                );
            }
            
            redirectAttributes.addFlashAttribute("mensaje", "Devolución registrada correctamente");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/prestamos";
    }

    // Ver préstamos atrasados
    @GetMapping("/atrasados")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USUARIO_EXTENDIDO')")
    public String verPrestamosAtrasados(Model model) {
        List<Prestamo> prestamosAtrasados = prestamoService.findPrestamosAtrasados();
        model.addAttribute("prestamos", prestamosAtrasados);
        model.addAttribute("tituloLista", "Préstamos atrasados");
        return "prestamos/lista";
    }

    // Ver mis préstamos (para usuario básico)
    @GetMapping("/mis-prestamos")
    public String verMisPrestamos(Model model, Principal principal) {
        Optional<Usuario> usuario = usuarioService.findByUsername(principal.getName());
        
        if (usuario.isPresent()) {
            List<Prestamo> prestamos = prestamoService.findByUsuario(usuario.get());
            model.addAttribute("prestamos", prestamos);
            model.addAttribute("tituloLista", "Mis préstamos");
            return "prestamos/lista";
        } else {
            return "redirect:/";
        }
    }
}