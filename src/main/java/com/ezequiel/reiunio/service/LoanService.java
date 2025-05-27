package com.ezequiel.reiunio.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ezequiel.reiunio.entity.Game;
import com.ezequiel.reiunio.entity.GameSessionInfo;
import com.ezequiel.reiunio.entity.Loan;
import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.LoanStatus;
import com.ezequiel.reiunio.service.impl.LoanServiceImpl.LoanConflictInfo;

public interface LoanService {
    
    List<Loan> findAll();
    
    /**
     * Finds all loans with pagination
     */
    Page<Loan> findAllPaginated(Pageable pageable);
    
    Optional<Loan> findById(Long id);
    
    Loan save(Loan loan);
    
    void deleteById(Long id);
    
    List<Loan> findByUser(User user);
    
    /**
     * Finds loans by user with pagination
     */
    Page<Loan> findByUserPaginated(User user, Pageable pageable);
    
    List<Loan> findByGame(Game game);
    
    List<Loan> findByStatus(LoanStatus status);
    
    /**
     * Finds loans by status with pagination
     */
    Page<Loan> findByStatusPaginated(LoanStatus status, Pageable pageable);
    
    Loan createLoan(User user, Game game, LocalDate estimatedReturnDate);
    
    Loan registerReturn(Long loanId, LocalDate returnDate);
    
    List<Loan> findOverdueLoans();
    
    /**
     * Finds overdue loans with pagination
     */
    Page<Loan> findOverdueLoansPaginated(Pageable pageable);
    
    /**
     * Finds upcoming game sessions for a specific game
     * @param game The game to check for sessions
     * @return List of upcoming sessions using this game
     */
    List<GameSessionInfo> findUpcomingSessionsForGame(Game game);
    
    /**
     * Suggests a return date for a loan considering upcoming game sessions
     * @param game The game being loaned
     * @param proposedReturnDate The initially proposed return date
     * @return Suggested return date that avoids conflicts with game sessions
     */
    LocalDate suggestReturnDate(Game game, LocalDate proposedReturnDate);
    
    /**
     * Verifica si un juego está programado para ser usado en sesiones próximas
     * @param game El juego a verificar
     * @return true si hay sesiones programadas que usan este juego
     */
    boolean isGameScheduledForUpcomingSessions(Game game);
    
    /**
     * Verifica conflictos potenciales entre un préstamo y sesiones programadas
     * @param game El juego del préstamo
     * @param proposedReturnDate La fecha de devolución propuesta
     * @return Información sobre conflictos y sugerencias
     */
    LoanConflictInfo checkLoanConflicts(Game game, LocalDate proposedReturnDate);
}