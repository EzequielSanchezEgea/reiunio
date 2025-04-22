package com.ezequiel.reiunio.service.impl;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ezequiel.reiunio.entity.Juego;
import com.ezequiel.reiunio.entity.Prestamo;
import com.ezequiel.reiunio.entity.Usuario;
import com.ezequiel.reiunio.enums.EstadoPrestamo;
import com.ezequiel.reiunio.repository.JuegoRepository;
import com.ezequiel.reiunio.repository.PrestamoRepository;
import com.ezequiel.reiunio.service.PrestamoService;

@Service
public class PrestamoServiceImpl implements PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final JuegoRepository juegoRepository;
    
    @Autowired
    public PrestamoServiceImpl(PrestamoRepository prestamoRepository, JuegoRepository juegoRepository) {
        this.prestamoRepository = prestamoRepository;
        this.juegoRepository = juegoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prestamo> findAll() {
        return prestamoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Prestamo> findById(Long id) {
        return prestamoRepository.findById(id);
    }

    @Override
    @Transactional
    public Prestamo save(Prestamo prestamo) {
        return prestamoRepository.save(prestamo);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        prestamoRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prestamo> findByUsuario(Usuario usuario) {
        return prestamoRepository.findByUsuario(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prestamo> findByJuego(Juego juego) {
        return prestamoRepository.findByJuego(juego);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prestamo> findByEstado(EstadoPrestamo estado) {
        return prestamoRepository.findByEstado(estado);
    }

    @Override
    @Transactional
    public Prestamo realizarPrestamo(Usuario usuario, Juego juego, LocalDate fechaDevolucionEstimada) {
        // Verificar que el juego esté disponible
        if (!juego.getDisponible()) {
            throw new IllegalStateException("El juego no está disponible para préstamo");
        }
        
        // Verificar que el usuario no tenga préstamos atrasados
        if (tienePrestamosAtrasados(usuario)) {
            throw new IllegalStateException("El usuario tiene préstamos atrasados. No puede realizar nuevos préstamos hasta regularizar su situación.");
        }
        
        // Crear nuevo préstamo
        Prestamo prestamo = new Prestamo();
        prestamo.setUsuario(usuario);
        prestamo.setJuego(juego);
        prestamo.setFechaPrestamo(LocalDate.now());
        prestamo.setFechaDevolucionEstimada(fechaDevolucionEstimada);
        prestamo.setEstado(EstadoPrestamo.ACTIVO);
        
        // Marcar el juego como no disponible
        juego.setDisponible(false);
        juegoRepository.save(juego);
        
        return prestamoRepository.save(prestamo);
    }

    @Override
    @Transactional
    public Prestamo registrarDevolucion(Long prestamoId, LocalDate fechaDevolucion) {
        Optional<Prestamo> optPrestamo = prestamoRepository.findById(prestamoId);
        if (optPrestamo.isPresent()) {
            Prestamo prestamo = optPrestamo.get();
            
            // Verificar que el préstamo esté activo
            if (prestamo.getEstado() != EstadoPrestamo.ACTIVO) {
                throw new IllegalStateException("El préstamo no está activo, no se puede registrar su devolución");
            }
            
            // Registrar la devolución
            prestamo.setFechaDevolucionReal(fechaDevolucion);
            
            // Determinar si hay retraso
            if (fechaDevolucion.isAfter(prestamo.getFechaDevolucionEstimada())) {
                prestamo.setEstado(EstadoPrestamo.RETRASADO);
            } else {
                prestamo.setEstado(EstadoPrestamo.DEVUELTO);
            }
            
            // Marcar el juego como disponible
            Juego juego = prestamo.getJuego();
            juego.setDisponible(true);
            juegoRepository.save(juego);
            
            return prestamoRepository.save(prestamo);
        } else {
            throw new IllegalArgumentException("No se encontró el préstamo con ID: " + prestamoId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prestamo> findPrestamosAtrasados() {
        return prestamoRepository.findPrestamosAtrasados();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prestamo> findByJuegoAndActivos(Juego juego) {
        return prestamoRepository.findByJuegoAndEstado(juego, EstadoPrestamo.ACTIVO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prestamo> findActivosByUsuarioAndJuego(Usuario usuario, Juego juego) {
        return prestamoRepository.findByUsuarioAndJuegoAndEstado(usuario, juego, EstadoPrestamo.ACTIVO);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPrestamosByUsuario(Usuario usuario) {
        return prestamoRepository.countByUsuario(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPrestamosByJuego(Juego juego) {
        return prestamoRepository.countByJuego(juego);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tienePrestamosActivos(Usuario usuario) {
        return prestamoRepository.countByUsuarioAndEstado(usuario, EstadoPrestamo.ACTIVO) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tienePrestamosAtrasados(Usuario usuario) {
        LocalDate hoy = LocalDate.now();
        return prestamoRepository.countByUsuarioAndEstadoAndFechaDevolucionEstimadaBefore(
            usuario, EstadoPrestamo.ACTIVO, hoy) > 0;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Prestamo> findMostRecent(int limit) {
        return prestamoRepository.findAll(
            PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "fechaPrestamo"))
        ).getContent();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Prestamo> findByFechaPrestamo(LocalDate fechaPrestamo) {
        return prestamoRepository.findByFechaPrestamo(fechaPrestamo);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Prestamo> findByFechaPrestamoEntre(LocalDate fechaInicio, LocalDate fechaFin) {
        // Corregido: usar el nombre correcto del método en el repositorio
        return prestamoRepository.findByFechaPrestamoGreaterThanEqualAndFechaPrestamoLessThanEqual(
            fechaInicio, fechaFin);
    }
    
    @Override
    @Transactional(readOnly = true)
    public int calcularDiasRetraso(Long prestamoId) {
        Optional<Prestamo> optPrestamo = prestamoRepository.findById(prestamoId);
        if (optPrestamo.isPresent()) {
            Prestamo prestamo = optPrestamo.get();
            
            // Si el préstamo ya fue devuelto, calcular retraso con la fecha real
            if (prestamo.getFechaDevolucionReal() != null) {
                if (prestamo.getFechaDevolucionReal().isAfter(prestamo.getFechaDevolucionEstimada())) {
                    return (int) ChronoUnit.DAYS.between(
                        prestamo.getFechaDevolucionEstimada(),
                        prestamo.getFechaDevolucionReal()
                    );
                }
            } 
            // Si el préstamo está activo y ya pasó la fecha estimada, calcular con la fecha actual
            else if (prestamo.getEstado() == EstadoPrestamo.ACTIVO) {
                LocalDate hoy = LocalDate.now();
                if (hoy.isAfter(prestamo.getFechaDevolucionEstimada())) {
                    return (int) ChronoUnit.DAYS.between(
                        prestamo.getFechaDevolucionEstimada(),
                        hoy
                    );
                }
            }
        }
        
        return 0; // No hay retraso
    }
    
    @Override
    @Transactional
    public Prestamo extenderPrestamo(Long prestamoId, LocalDate nuevaFechaDevolucion) {
        Optional<Prestamo> optPrestamo = prestamoRepository.findById(prestamoId);
        if (!optPrestamo.isPresent()) {
            throw new IllegalArgumentException("No se encontró el préstamo con ID: " + prestamoId);
        }
        
        Prestamo prestamo = optPrestamo.get();
        
        // Verificar que el préstamo esté activo
        if (prestamo.getEstado() != EstadoPrestamo.ACTIVO) {
            throw new IllegalStateException("Solo se pueden extender préstamos activos");
        }
        
        // Verificar que la nueva fecha sea posterior a la fecha actual
        LocalDate hoy = LocalDate.now();
        if (nuevaFechaDevolucion.isBefore(hoy) || nuevaFechaDevolucion.isEqual(hoy)) {
            throw new IllegalArgumentException("La nueva fecha de devolución debe ser posterior a hoy");
        }
        
        // Verificar que la nueva fecha sea posterior a la fecha estimada actual
        if (nuevaFechaDevolucion.isBefore(prestamo.getFechaDevolucionEstimada()) || 
            nuevaFechaDevolucion.isEqual(prestamo.getFechaDevolucionEstimada())) {
            throw new IllegalArgumentException("La nueva fecha debe ser posterior a la fecha de devolución actual");
        }
        
        // Actualizar la fecha de devolución estimada
        prestamo.setFechaDevolucionEstimada(nuevaFechaDevolucion);
        return prestamoRepository.save(prestamo);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getEstadisticasPorUsuario() {
        return prestamoRepository.countPrestamosByUsuario();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getEstadisticasPorJuego() {
        return prestamoRepository.countPrestamosByJuego();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getEstadisticasPorMes(int anio) {
        return prestamoRepository.countPrestamosByMonth(anio);
    }
}