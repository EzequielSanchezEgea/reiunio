package com.ezequiel.reiunio.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.ezequiel.reiunio.entity.HistorialCambios;
import com.ezequiel.reiunio.entity.Usuario;
import com.ezequiel.reiunio.enums.TipoAccion;

public interface HistorialCambiosService {
    
    List<HistorialCambios> findAll();
    
    Optional<HistorialCambios> findById(Long id);
    
    HistorialCambios save(HistorialCambios historialCambios);
    
    List<HistorialCambios> findByUsuario(Usuario usuario);
    
    List<HistorialCambios> findByTipoAccion(TipoAccion tipoAccion);
    
    List<HistorialCambios> findByEntidadAfectada(String entidadAfectada);
    
    List<HistorialCambios> findByEntidadAfectadaYId(String entidadAfectada, Long entidadId);
    
    List<HistorialCambios> findEntreFechas(LocalDateTime inicio, LocalDateTime fin);
    
    void registrarCambio(Usuario usuario, TipoAccion tipoAccion, String entidadAfectada, 
                        Long entidadId, String descripcion);
}