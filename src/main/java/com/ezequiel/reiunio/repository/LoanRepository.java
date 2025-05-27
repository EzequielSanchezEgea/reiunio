package com.ezequiel.reiunio.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ezequiel.reiunio.entity.Game;
import com.ezequiel.reiunio.entity.Loan;
import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.LoanStatus;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    
    List<Loan> findByUser(User user);
    
    /**
     * Encuentra préstamos por usuario con paginación
     */
    Page<Loan> findByUser(User user, Pageable pageable);
    
    List<Loan> findByGame(Game game);
    
    List<Loan> findByStatus(LoanStatus status);
    
    /**
     * Encuentra préstamos por estado con paginación
     */
    Page<Loan> findByStatus(LoanStatus status, Pageable pageable);
    
    /**
     * Encuentra préstamos por juego y estado
     */
    List<Loan> findByGameAndStatus(Game game, LoanStatus status);
    
    /**
     * Encuentra préstamos vencidos (fecha estimada de devolución pasada y estado ACTIVE)
     */
    @Query("SELECT l FROM Loan l WHERE l.status = 'ACTIVE' AND l.estimatedReturnDate < :currentDate")
    List<Loan> findOverdueLoans(@Param("currentDate") LocalDate currentDate);
    
    /**
     * Encuentra préstamos vencidos con paginación
     */
    @Query("SELECT l FROM Loan l WHERE l.status = 'ACTIVE' AND l.estimatedReturnDate < :currentDate")
    Page<Loan> findOverdueLoans(@Param("currentDate") LocalDate currentDate, Pageable pageable);
    
    /**
     * Encuentra préstamos vencidos usando fecha actual
     */
    default List<Loan> findOverdueLoans() {
        return findOverdueLoans(LocalDate.now());
    }
    
    /**
     * Encuentra préstamos vencidos con paginación usando fecha actual
     */
    default Page<Loan> findOverdueLoans(Pageable pageable) {
        return findOverdueLoans(LocalDate.now(), pageable);
    }
    
    /**
     * Encuentra préstamos activos de un usuario para un juego específico
     */
    @Query("SELECT l FROM Loan l WHERE l.user = :user AND l.game = :game AND l.status = 'ACTIVE'")
    List<Loan> findActiveLoansForUserAndGame(@Param("user") User user, @Param("game") Game game);
    
    /**
     * Cuenta préstamos activos para un juego
     */
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.game = :game AND l.status = 'ACTIVE'")
    long countActiveLoansForGame(@Param("game") Game game);
    
    /**
     * Encuentra el préstamo activo más reciente para un juego
     */
    @Query("SELECT l FROM Loan l WHERE l.game = :game AND l.status = 'ACTIVE' ORDER BY l.loanDate DESC")
    List<Loan> findMostRecentActiveLoanForGame(@Param("game") Game game);
    
    /**
     * Encuentra préstamos que se solapan con un período específico
     */
    @Query("SELECT l FROM Loan l WHERE l.game = :game AND l.status = 'ACTIVE' AND " +
           "l.loanDate <= :endDate AND l.estimatedReturnDate >= :startDate")
    List<Loan> findLoansOverlappingPeriod(@Param("game") Game game, 
                                         @Param("startDate") LocalDate startDate, 
                                         @Param("endDate") LocalDate endDate);
}