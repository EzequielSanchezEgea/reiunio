package com.ezequiel.reiunio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ezequiel.reiunio.entity.Juego;
import com.ezequiel.reiunio.enums.EstadoJuego;

@Repository
public interface JuegoRepository extends JpaRepository<Juego, Long> {
    
    List<Juego> findByNombreContainingIgnoreCase(String nombre);
    
    List<Juego> findByCategoriaContainingIgnoreCase(String categoria);
    
    List<Juego> findByDisponible(Boolean disponible);
    
    long countByDisponible(Boolean disponible);
    
    long countByEstado(EstadoJuego estado);
    
    List<Juego> findByEstado(EstadoJuego estado);
    
    List<Juego> findByMinJugadoresLessThanEqualAndMaxJugadoresGreaterThanEqual(int numJugadores, int numJugadores2);
    
    @Query("SELECT j.categoria, COUNT(j) FROM Juego j GROUP BY j.categoria")
    List<Object[]> countByCategoria();
    
    @Query("SELECT j, COUNT(p) as prestamosCount FROM Juego j LEFT JOIN j.prestamos p GROUP BY j ORDER BY prestamosCount DESC")
    List<Object[]> findMostBorrowedJuegos(int limit);
    
    @Query("SELECT j FROM Juego j WHERE j.id NOT IN (SELECT DISTINCT p.juego.id FROM Prestamo p)")
    List<Juego> findJuegosNeverBorrowed();
    
    @Query("SELECT COUNT(j) FROM Juego j WHERE j.disponible = :disponible")
    long countJuegosByDisponible(@Param("disponible") Boolean disponible);
}