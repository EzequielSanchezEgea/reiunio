package com.ezequiel.reiunio.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ezequiel.reiunio.entity.HistorialCambios;
import com.ezequiel.reiunio.entity.Usuario;
import com.ezequiel.reiunio.enums.TipoAccion;

@Repository
public interface HistorialCambiosRepository extends JpaRepository<HistorialCambios, Long> {
    
    List<HistorialCambios> findByUsuario(Usuario usuario);
    
    List<HistorialCambios> findByTipoAccion(TipoAccion tipoAccion);
    
    List<HistorialCambios> findByEntidadAfectada(String entidadAfectada);
    
    List<HistorialCambios> findByEntidadAfectadaAndEntidadId(String entidadAfectada, Long entidadId);
    
    List<HistorialCambios> findByFechaHoraCambioBetween(LocalDateTime inicio, LocalDateTime fin);
}