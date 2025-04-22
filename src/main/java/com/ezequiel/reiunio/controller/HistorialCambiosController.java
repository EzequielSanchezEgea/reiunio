package com.ezequiel.reiunio.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ezequiel.reiunio.entity.HistorialCambios;
import com.ezequiel.reiunio.enums.TipoAccion;
import com.ezequiel.reiunio.service.HistorialCambiosService;

@Controller
@RequestMapping("/historial")
@PreAuthorize("hasRole('ADMIN')")
public class HistorialCambiosController {

    private final HistorialCambiosService historialCambiosService;

    @Autowired
    public HistorialCambiosController(HistorialCambiosService historialCambiosService) {
        this.historialCambiosService = historialCambiosService;
    }

    // Listar todo el historial de cambios
    @GetMapping
    public String listarHistorial(Model model,
                                 @RequestParam(required = false) String tipoAccion,
                                 @RequestParam(required = false) String entidad,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        
        List<HistorialCambios> historial;
        
        if (tipoAccion != null && !tipoAccion.isEmpty()) {
            try {
                TipoAccion tipo = TipoAccion.valueOf(tipoAccion.toUpperCase());
                historial = historialCambiosService.findByTipoAccion(tipo);
                model.addAttribute("filtroTipoAccion", tipoAccion);
            } catch (IllegalArgumentException e) {
                historial = historialCambiosService.findAll();
            }
        } else if (entidad != null && !entidad.isEmpty()) {
            historial = historialCambiosService.findByEntidadAfectada(entidad);
            model.addAttribute("filtroEntidad", entidad);
        } else if (fechaInicio != null && fechaFin != null) {
            historial = historialCambiosService.findEntreFechas(fechaInicio, fechaFin);
            model.addAttribute("filtroFechaInicio", fechaInicio);
            model.addAttribute("filtroFechaFin", fechaFin);
        } else {
            historial = historialCambiosService.findAll();
        }
        
        model.addAttribute("historial", historial);
        model.addAttribute("tiposAccion", TipoAccion.values());
        
        // Lista de entidades afectadas para el filtro
        model.addAttribute("entidades", List.of("Usuario", "Juego", "Prestamo", "Partida"));
        
        return "historial/lista";
    }
    
    // Ver historial de una entidad específica
    @GetMapping("/entidad")
    public String verHistorialEntidad(Model model,
                                    @RequestParam String entidad,
                                    @RequestParam(required = false) Long entidadId) {
        
        List<HistorialCambios> historial;
        
        if (entidadId != null) {
            historial = historialCambiosService.findByEntidadAfectadaYId(entidad, entidadId);
            model.addAttribute("subtitulo", "Historial de " + entidad + " #" + entidadId);
        } else {
            historial = historialCambiosService.findByEntidadAfectada(entidad);
            model.addAttribute("subtitulo", "Historial de " + entidad);
        }
        
        model.addAttribute("historial", historial);
        model.addAttribute("tiposAccion", TipoAccion.values());
        
        return "historial/lista";
    }
}