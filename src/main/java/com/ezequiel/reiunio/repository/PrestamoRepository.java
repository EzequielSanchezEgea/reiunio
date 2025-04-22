package com.ezequiel.reiunio.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ezequiel.reiunio.entity.Juego;
import com.ezequiel.reiunio.entity.Prestamo;
import com.ezequiel.reiunio.entity.Usuario;
import com.ezequiel.reiunio.enums.EstadoPrestamo;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
    
    // Métodos básicos de búsqueda
    List<Prestamo> findByUsuario(Usuario usuario);
    
    List<Prestamo> findByJuego(Juego juego);
    
    List<Prestamo> findByEstado(EstadoPrestamo estado);
    
    List<Prestamo> findByFechaDevolucionEstimadaBeforeAndEstado(LocalDate fecha, EstadoPrestamo estado);
    
    // Búsqueda de préstamos atrasados
    @Query("SELECT p FROM Prestamo p WHERE p.fechaDevolucionEstimada < CURRENT_DATE AND p.estado = 'ACTIVO'")
    List<Prestamo> findPrestamosAtrasados();
    
    // Búsqueda por juego y estado
    List<Prestamo> findByJuegoAndEstado(Juego juego, EstadoPrestamo estado);
    
    // Búsqueda por usuario, juego y estado
    List<Prestamo> findByUsuarioAndJuegoAndEstado(Usuario usuario, Juego juego, EstadoPrestamo estado);
    
    // Conteo de préstamos
    long countByUsuario(Usuario usuario);
    
    long countByJuego(Juego juego);
    
    long countByUsuarioAndEstado(Usuario usuario, EstadoPrestamo estado);
    
    long countByUsuarioAndEstadoAndFechaDevolucionEstimadaBefore(Usuario usuario, EstadoPrestamo estado, LocalDate fecha);
    
    // Búsqueda por fecha de préstamo
    List<Prestamo> findByFechaPrestamo(LocalDate fechaPrestamo);
    
    // Corregido: asegurarse de que el nombre de la propiedad sea correcto
    List<Prestamo> findByFechaPrestamoGreaterThanEqualAndFechaPrestamoLessThanEqual(LocalDate fechaInicio, LocalDate fechaFin);
    
    // Métodos para estadísticas
    @Query("SELECT p.usuario, COUNT(p) FROM Prestamo p GROUP BY p.usuario ORDER BY COUNT(p) DESC")
    List<Object[]> countPrestamosByUsuario();
    
    @Query("SELECT p.juego, COUNT(p) FROM Prestamo p GROUP BY p.juego ORDER BY COUNT(p) DESC")
    List<Object[]> countPrestamosByJuego();
    
    @Query("SELECT FUNCTION('MONTH', p.fechaPrestamo) as mes, COUNT(p) FROM Prestamo p " +
           "WHERE FUNCTION('YEAR', p.fechaPrestamo) = :anio " +
           "GROUP BY FUNCTION('MONTH', p.fechaPrestamo) " +
           "ORDER BY mes")
    List<Object[]> countPrestamosByMonth(@Param("anio") int anio);
    
    // Préstamos activos por juego (para verificación de disponibilidad)
    @Query("SELECT COUNT(p) FROM Prestamo p WHERE p.juego = :juego AND p.estado = 'ACTIVO'")
    long countActivosByJuego(@Param("juego") Juego juego);
    
    // Préstamos pendientes de un usuario
    @Query("SELECT p FROM Prestamo p WHERE p.usuario = :usuario AND p.estado = 'ACTIVO' ORDER BY p.fechaDevolucionEstimada ASC")
    List<Prestamo> findPrestamosActivosByUsuario(@Param("usuario") Usuario usuario);
    
    // Préstamos atrasados de un usuario
    @Query("SELECT p FROM Prestamo p WHERE p.usuario = :usuario AND p.estado = 'ACTIVO' AND p.fechaDevolucionEstimada < CURRENT_DATE")
    List<Prestamo> findPrestamosAtrasadosByUsuario(@Param("usuario") Usuario usuario);
    
    // Historial de préstamos
    @Query("SELECT p FROM Prestamo p WHERE p.estado = 'DEVUELTO' OR p.estado = 'RETRASADO' ORDER BY p.fechaDevolucionReal DESC")
    List<Prestamo> findHistorialPrestamos();
}