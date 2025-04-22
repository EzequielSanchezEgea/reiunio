package com.ezequiel.reiunio.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ezequiel.reiunio.entity.HistorialCambios;
import com.ezequiel.reiunio.entity.Usuario;
import com.ezequiel.reiunio.enums.TipoAccion;
import com.ezequiel.reiunio.repository.HistorialCambiosRepository;
import com.ezequiel.reiunio.service.HistorialCambiosService;

@Service
public class HistorialCambiosServiceImpl implements HistorialCambiosService {

    private final HistorialCambiosRepository historialCambiosRepository;
    
    @Autowired
    public HistorialCambiosServiceImpl(HistorialCambiosRepository historialCambiosRepository) {
        this.historialCambiosRepository = historialCambiosRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialCambios> findAll() {
        return historialCambiosRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<HistorialCambios> findById(Long id) {
        return historialCambiosRepository.findById(id);
    }

    @Override
    @Transactional
    public HistorialCambios save(HistorialCambios historialCambios) {
        return historialCambiosRepository.save(historialCambios);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialCambios> findByUsuario(Usuario usuario) {
        return historialCambiosRepository.findByUsuario(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialCambios> findByTipoAccion(TipoAccion tipoAccion) {
        return historialCambiosRepository.findByTipoAccion(tipoAccion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialCambios> findByEntidadAfectada(String entidadAfectada) {
        return historialCambiosRepository.findByEntidadAfectada(entidadAfectada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialCambios> findByEntidadAfectadaYId(String entidadAfectada, Long entidadId) {
        return historialCambiosRepository.findByEntidadAfectadaAndEntidadId(entidadAfectada, entidadId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialCambios> findEntreFechas(LocalDateTime inicio, LocalDateTime fin) {
        return historialCambiosRepository.findByFechaHoraCambioBetween(inicio, fin);
    }

    @Override
    @Transactional
    public void registrarCambio(Usuario usuario, TipoAccion tipoAccion, String entidadAfectada, 
                                Long entidadId, String descripcion) {
        HistorialCambios cambio = new HistorialCambios();
        cambio.setUsuario(usuario);
        cambio.setTipoAccion(tipoAccion);
        cambio.setEntidadAfectada(entidadAfectada);
        cambio.setEntidadId(entidadId);
        cambio.setDescripcion(descripcion);
        cambio.setFechaHoraCambio(LocalDateTime.now());
        
        historialCambiosRepository.save(cambio);
    }
}