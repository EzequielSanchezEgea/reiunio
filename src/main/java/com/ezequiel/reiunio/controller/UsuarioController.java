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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ezequiel.reiunio.entity.HistorialCambios;
import com.ezequiel.reiunio.entity.Usuario;
import com.ezequiel.reiunio.enums.Rol;
import com.ezequiel.reiunio.enums.TipoAccion;
import com.ezequiel.reiunio.service.HistorialCambiosService;
import com.ezequiel.reiunio.service.UsuarioService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final HistorialCambiosService historialCambiosService;

    @Autowired
    public UsuarioController(UsuarioService usuarioService, HistorialCambiosService historialCambiosService) {
        this.usuarioService = usuarioService;
        this.historialCambiosService = historialCambiosService;
    }

    // Listar todos los usuarios (solo admin)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String listarUsuarios(Model model) {
        List<Usuario> usuarios = usuarioService.findAll();
        model.addAttribute("usuarios", usuarios);
        return "usuarios/lista";
    }

    // Ver detalles de un usuario
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.username == #username")
    public String verUsuario(@PathVariable Long id, Model model, Principal principal) {
        Optional<Usuario> usuario = usuarioService.findById(id);
        
        if (usuario.isPresent()) {
            model.addAttribute("usuario", usuario.get());
            return "usuarios/detalle";
        } else {
            return "redirect:/usuarios";
        }
    }

    // Formulario para crear un nuevo usuario (solo admin)
    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuevoUsuarioForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", Rol.values());
        return "usuarios/formulario";
    }

    // Procesar la creación de un nuevo usuario
    @PostMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String crearUsuario(@Valid @ModelAttribute("usuario") Usuario usuario, BindingResult result, 
                              Model model, RedirectAttributes redirectAttributes, Principal principal) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Rol.values());
            return "usuarios/formulario";
        }
        
        // Verificar si el username o email ya existen
        if (usuarioService.existsByUsername(usuario.getUsername())) {
            result.rejectValue("username", "error.usuario", "El nombre de usuario ya está en uso");
            model.addAttribute("roles", Rol.values());
            return "usuarios/formulario";
        }
        
        if (usuarioService.existsByEmail(usuario.getEmail())) {
            result.rejectValue("email", "error.usuario", "El email ya está registrado");
            model.addAttribute("roles", Rol.values());
            return "usuarios/formulario";
        }
        
        // Establecer la fecha de registro
        usuario.setFechaRegistro(LocalDate.now());
        
        // Guardar el usuario
        usuario = usuarioService.save(usuario);
        
        // Registrar en el historial
        Optional<Usuario> adminUsuario = usuarioService.findByUsername(principal.getName());
        if (adminUsuario.isPresent()) {
            historialCambiosService.registrarCambio(
                adminUsuario.get(),
                TipoAccion.CREACION,
                "Usuario",
                usuario.getId(),
                "Creación de usuario: " + usuario.getUsername()
            );
        }
        
        redirectAttributes.addFlashAttribute("mensaje", "Usuario creado correctamente");
        return "redirect:/usuarios";
    }

    // Formulario para editar un usuario existente
    @GetMapping("/{id}/editar")
    @PreAuthorize("hasRole('ADMIN')")
    public String editarUsuarioForm(@PathVariable Long id, Model model) {
        Optional<Usuario> usuario = usuarioService.findById(id);
        
        if (usuario.isPresent()) {
            model.addAttribute("usuario", usuario.get());
            model.addAttribute("roles", Rol.values());
            model.addAttribute("edicion", true);
            return "usuarios/formulario";
        } else {
            return "redirect:/usuarios";
        }
    }

    // Procesar la actualización de un usuario
    @PostMapping("/{id}/editar")
    @PreAuthorize("hasRole('ADMIN')")
    public String actualizarUsuario(@PathVariable Long id, @Valid @ModelAttribute("usuario") Usuario usuario,
                                  BindingResult result, Model model, RedirectAttributes redirectAttributes,
                                  Principal principal) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Rol.values());
            model.addAttribute("edicion", true);
            return "usuarios/formulario";
        }
        
        Optional<Usuario> usuarioExistente = usuarioService.findById(id);
        
        if (usuarioExistente.isPresent()) {
            Usuario usuarioActual = usuarioExistente.get();
            
            // Verificar si el username ha cambiado y ya existe
            if (!usuarioActual.getUsername().equals(usuario.getUsername()) && 
                usuarioService.existsByUsername(usuario.getUsername())) {
                result.rejectValue("username", "error.usuario", "El nombre de usuario ya está en uso");
                model.addAttribute("roles", Rol.values());
                model.addAttribute("edicion", true);
                return "usuarios/formulario";
            }
            
            // Verificar si el email ha cambiado y ya existe
            if (!usuarioActual.getEmail().equals(usuario.getEmail()) && 
                usuarioService.existsByEmail(usuario.getEmail())) {
                result.rejectValue("email", "error.usuario", "El email ya está registrado");
                model.addAttribute("roles", Rol.values());
                model.addAttribute("edicion", true);
                return "usuarios/formulario";
            }
            
            // Mantener fecha de registro original
            usuario.setFechaRegistro(usuarioActual.getFechaRegistro());
            
            // Si no se proporciona una nueva contraseña, mantener la existente
            if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
                usuario.setPassword(usuarioActual.getPassword());
            }
            
            // Guardar los cambios
            usuario = usuarioService.save(usuario);
            
            // Registrar en el historial
            Optional<Usuario> adminUsuario = usuarioService.findByUsername(principal.getName());
            if (adminUsuario.isPresent()) {
                historialCambiosService.registrarCambio(
                    adminUsuario.get(),
                    TipoAccion.MODIFICACION,
                    "Usuario",
                    usuario.getId(),
                    "Modificación de usuario: " + usuario.getUsername()
                );
            }
            
            redirectAttributes.addFlashAttribute("mensaje", "Usuario actualizado correctamente");
            return "redirect:/usuarios";
        } else {
            return "redirect:/usuarios";
        }
    }

    // Eliminar un usuario
    @PostMapping("/{id}/eliminar")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes, Principal principal) {
        Optional<Usuario> usuario = usuarioService.findById(id);
        
        if (usuario.isPresent()) {
            String usernameEliminado = usuario.get().getUsername();
            
            // Eliminar el usuario
            usuarioService.deleteById(id);
            
            // Registrar en el historial
            Optional<Usuario> adminUsuario = usuarioService.findByUsername(principal.getName());
            if (adminUsuario.isPresent()) {
                historialCambiosService.registrarCambio(
                    adminUsuario.get(),
                    TipoAccion.ELIMINACION,
                    "Usuario",
                    id,
                    "Eliminación de usuario: " + usernameEliminado
                );
            }
            
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado correctamente");
        }
        
        return "redirect:/usuarios";
    }

    // Formulario de registro público
    @GetMapping("/registro")
    public String registroForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "usuarios/registro";
    }

    // Procesar el registro público
    @PostMapping("/registro")
    public String procesarRegistro(@Valid @ModelAttribute("usuario") Usuario usuario, BindingResult result,
                                 Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "usuarios/registro";
        }
        
        // Verificar si el username o email ya existen
        if (usuarioService.existsByUsername(usuario.getUsername())) {
            result.rejectValue("username", "error.usuario", "El nombre de usuario ya está en uso");
            return "usuarios/registro";
        }
        
        if (usuarioService.existsByEmail(usuario.getEmail())) {
            result.rejectValue("email", "error.usuario", "El email ya está registrado");
            return "usuarios/registro";
        }
        
        // Establecer rol por defecto como usuario básico
        usuario.setRol(Rol.USUARIO_BASICO);
        
        // Establecer la fecha de registro
        usuario.setFechaRegistro(LocalDate.now());
        
        // Guardar el usuario
        usuarioService.save(usuario);
        
        redirectAttributes.addFlashAttribute("mensaje", "Registro completado correctamente. Ya puedes iniciar sesión.");
        return "redirect:/login";
    }

    // Perfil del usuario actual
    @GetMapping("/perfil")
    public String perfilUsuario(Model model, Principal principal) {
        Optional<Usuario> usuario = usuarioService.findByUsername(principal.getName());
        
        if (usuario.isPresent()) {
            model.addAttribute("usuario", usuario.get());
            return "usuarios/perfil";
        } else {
            return "redirect:/";
        }
    }

    // Formulario para editar el perfil del usuario actual
    @GetMapping("/perfil/editar")
    public String editarPerfilForm(Model model, Principal principal) {
        Optional<Usuario> usuario = usuarioService.findByUsername(principal.getName());
        
        if (usuario.isPresent()) {
            model.addAttribute("usuario", usuario.get());
            model.addAttribute("edicion", true);
            return "usuarios/perfil-editar";
        } else {
            return "redirect:/";
        }
    }

    // Procesar la actualización del perfil
    @PostMapping("/perfil/editar")
    public String actualizarPerfil(@Valid @ModelAttribute("usuario") Usuario usuario, BindingResult result,
                                 Model model, RedirectAttributes redirectAttributes, Principal principal) {
        if (result.hasErrors()) {
            model.addAttribute("edicion", true);
            return "usuarios/perfil-editar";
        }
        
        Optional<Usuario> usuarioExistente = usuarioService.findByUsername(principal.getName());
        
        if (usuarioExistente.isPresent()) {
            Usuario usuarioActual = usuarioExistente.get();
            
            // Verificar si el email ha cambiado y ya existe
            if (!usuarioActual.getEmail().equals(usuario.getEmail()) && 
                usuarioService.existsByEmail(usuario.getEmail())) {
                result.rejectValue("email", "error.usuario", "El email ya está registrado");
                model.addAttribute("edicion", true);
                return "usuarios/perfil-editar";
            }
            
            // Mantener valores que no deben cambiar
            usuario.setId(usuarioActual.getId());
            usuario.setUsername(usuarioActual.getUsername()); // No permitir cambio de username
            usuario.setRol(usuarioActual.getRol()); // No permitir cambio de rol
            usuario.setFechaRegistro(usuarioActual.getFechaRegistro());
            
            // Si no se proporciona una nueva contraseña, mantener la existente
            if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
                usuario.setPassword(usuarioActual.getPassword());
            }
            
            // Guardar los cambios
            usuarioService.save(usuario);
            
            // Registrar en el historial
            historialCambiosService.registrarCambio(
                usuarioActual,
                TipoAccion.MODIFICACION,
                "Usuario",
                usuario.getId(),
                "Actualización de perfil de usuario"
            );
            
            redirectAttributes.addFlashAttribute("mensaje", "Perfil actualizado correctamente");
            return "redirect:/usuarios/perfil";
        } else {
            return "redirect:/";
        }
    }
}