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

/**
 * Service interface for managing game loans.
 */
public interface LoanService {

    /**
     * Retrieves all loans.
     * 
     * @return a list of all loans
     */
    List<Loan> findAll();

    /**
     * Retrieves all loans with pagination.
     * 
     * @param pageable pagination information
     * @return a page of loans
     */
    Page<Loan> findAllPaginated(Pageable pageable);

    /**
     * Finds a loan by its ID.
     * 
     * @param id the ID of the loan
     * @return an Optional containing the loan if found
     */
    Optional<Loan> findById(Long id);

    /**
     * Saves or updates a loan.
     * 
     * @param loan the loan to save
     * @return the saved loan
     */
    Loan save(Loan loan);

    /**
     * Deletes a loan by its ID.
     * 
     * @param id the ID of the loan to delete
     */
    void deleteById(Long id);

    /**
     * Retrieves all loans by user.
     * 
     * @param user the user
     * @return a list of loans for the given user
     */
    List<Loan> findByUser(User user);

    /**
     * Retrieves loans by user with pagination.
     * 
     * @param user the user
     * @param pageable pagination information
     * @return a page of loans for the user
     */
    Page<Loan> findByUserPaginated(User user, Pageable pageable);

    /**
     * Retrieves all loans for a specific game.
     * 
     * @param game the game
     * @return a list of loans for the given game
     */
    List<Loan> findByGame(Game game);

    /**
     * Retrieves loans by status.
     * 
     * @param status the loan status
     * @return a list of loans with the given status
     */
    List<Loan> findByStatus(LoanStatus status);

    /**
     * Retrieves loans by status with pagination.
     * 
     * @param status the loan status
     * @param pageable pagination information
     * @return a page of loans with the given status
     */
    Page<Loan> findByStatusPaginated(LoanStatus status, Pageable pageable);

    /**
     * Creates a new loan.
     * 
     * @param user the user requesting the loan
     * @param game the game to loan
     * @param estimatedReturnDate the expected return date
     * @return the created loan
     */
    Loan createLoan(User user, Game game, LocalDate estimatedReturnDate);

    /**
     * Registers the return of a loan.
     * 
     * @param loanId the loan ID
     * @param returnDate the actual return date
     * @return the updated loan with return registered
     */
    Loan registerReturn(Long loanId, LocalDate returnDate);

    /**
     * Finds all overdue loans.
     * 
     * @return a list of overdue loans
     */
    List<Loan> findOverdueLoans();

    /**
     * Finds all overdue loans with pagination.
     * 
     * @param pageable pagination information
     * @return a page of overdue loans
     */
    Page<Loan> findOverdueLoansPaginated(Pageable pageable);

    /**
     * Retrieves upcoming game sessions using a specific game.
     * 
     * @param game the game
     * @return a list of upcoming sessions that include the game
     */
    List<GameSessionInfo> findUpcomingSessionsForGame(Game game);

    /**
     * Suggests a return date that avoids conflicts with scheduled sessions.
     * 
     * @param game the game to loan
     * @param proposedReturnDate the user's proposed return date
     * @return an adjusted return date if a conflict exists, or the same date otherwise
     */
    LocalDate suggestReturnDate(Game game, LocalDate proposedReturnDate);

    /**
     * Checks if a game is scheduled to be used in upcoming sessions.
     * 
     * @param game the game to check
     * @return true if the game is scheduled, false otherwise
     */
    boolean isGameScheduledForUpcomingSessions(Game game);

    /**
     * Checks for potential conflicts between a loan and upcoming sessions.
     * 
     * @param game the game to loan
     * @param proposedReturnDate the proposed return date
     * @return conflict information including suggestions
     */
    LoanConflictInfo checkLoanConflicts(Game game, LocalDate proposedReturnDate);
}
