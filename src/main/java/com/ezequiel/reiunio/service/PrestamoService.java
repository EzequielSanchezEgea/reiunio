package com.ezequiel.reiunio.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.ezequiel.reiunio.entity.Juego;
import com.ezequiel.reiunio.entity.Prestamo;
import com.ezequiel.reiunio.entity.Usuario;
import com.ezequiel.reiunio.enums.EstadoPrestamo;

public interface PrestamoService {
    
    /**
     * Devuelve todos los préstamos registrados en el sistema
     */
    List<Prestamo> findAll();
    
    /**
     * Busca un préstamo por su ID
     */
    Optional<Prestamo> findById(Long id);
    
    /**
     * Guarda un préstamo en la base de datos
     */
    Prestamo save(Prestamo prestamo);
    
    /**
     * Elimina un préstamo por su ID
     */
    void deleteById(Long id);
    
    /**
     * Busca todos los préstamos de un usuario específico
     */
    List<Prestamo> findByUsuario(Usuario usuario);
    
    /**
     * Busca todos los préstamos de un juego específico
     */
    List<Prestamo> findByJuego(Juego juego);
    
    /**
     * Busca préstamos por su estado (ACTIVO, DEVUELTO, RETRASADO)
     */
    List<Prestamo> findByEstado(EstadoPrestamo estado);
    
    /**
     * Realiza un nuevo préstamo
     */
    Prestamo realizarPrestamo(Usuario usuario, Juego juego, LocalDate fechaDevolucionEstimada);
    
    /**
     * Registra la devolución de un préstamo
     */
    Prestamo registrarDevolucion(Long prestamoId, LocalDate fechaDevolucion);
    
    /**
     * Busca préstamos que están atrasados (no devueltos después de la fecha estimada)
     */
    List<Prestamo> findPrestamosAtrasados();
    
    /**
     * Busca préstamos activos de un juego específico
     */
    List<Prestamo> findByJuegoAndActivos(Juego juego);
    
    /**
     * Busca préstamos activos de un usuario y juego específicos
     */
    List<Prestamo> findActivosByUsuarioAndJuego(Usuario usuario, Juego juego);
    
    /**
     * Cuenta la cantidad total de préstamos realizados por un usuario
     */
    long countPrestamosByUsuario(Usuario usuario);
    
    /**
     * Cuenta la cantidad total de préstamos realizados de un juego
     */
    long countPrestamosByJuego(Juego juego);
    
    /**
     * Verifica si un usuario tiene préstamos activos
     */
    boolean tienePrestamosActivos(Usuario usuario);
    
    /**
     * Verifica si un usuario tiene préstamos atrasados
     */
    boolean tienePrestamosAtrasados(Usuario usuario);
    
    /**
     * Busca los préstamos más recientes
     */
    List<Prestamo> findMostRecent(int limit);
    
    /**
     * Busca préstamos por fecha de préstamo
     */
    List<Prestamo> findByFechaPrestamo(LocalDate fechaPrestamo);
    
    /**
     * Busca préstamos en un rango de fechas
     */
    List<Prestamo> findByFechaPrestamoEntre(LocalDate fechaInicio, LocalDate fechaFin);
    
    /**
     * Calcula los días de retraso de un préstamo
     */
    int calcularDiasRetraso(Long prestamoId);
    
    /**
     * Extiende la fecha de devolución de un préstamo
     */
    Prestamo extenderPrestamo(Long prestamoId, LocalDate nuevaFechaDevolucion);
    
    /**
     * Obtiene estadísticas de préstamos por usuario
     */
    List<Object[]> getEstadisticasPorUsuario();
    
    /**
     * Obtiene estadísticas de préstamos por juego
     */
    List<Object[]> getEstadisticasPorJuego();
    
    /**
     * Obtiene estadísticas de préstamos por mes
     */
    List<Object[]> getEstadisticasPorMes(int anio);
}