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

/**
 * Repository interface for managing {@link Loan} entities.
 */
@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    /**
     * Finds all loans associated with a specific user.
     *
     * @param user the user entity
     * @return list of loans for the given user
     */
    List<Loan> findByUser(User user);

    /**
     * Finds all loans by a specific user with pagination support.
     *
     * @param user the user entity
     * @param pageable pagination information
     * @return paginated list of loans
     */
    Page<Loan> findByUser(User user, Pageable pageable);

    /**
     * Finds all loans for a specific game.
     *
     * @param game the game entity
     * @return list of loans for the game
     */
    List<Loan> findByGame(Game game);

    /**
     * Finds all loans with a specific status.
     *
     * @param status the loan status
     * @return list of loans with the specified status
     */
    List<Loan> findByStatus(LoanStatus status);

    /**
     * Finds all loans with a specific status and paginates the results.
     *
     * @param status the loan status
     * @param pageable pagination information
     * @return paginated list of loans with the specified status
     */
    Page<Loan> findByStatus(LoanStatus status, Pageable pageable);

    /**
     * Finds loans for a specific game and status.
     *
     * @param game the game entity
     * @param status the loan status
     * @return list of matching loans
     */
    List<Loan> findByGameAndStatus(Game game, LoanStatus status);

    /**
     * Finds loans that are overdue (estimated return date before the current date and status is ACTIVE).
     *
     * @param currentDate the reference date for checking overdue loans
     * @return list of overdue loans
     */
    @Query("SELECT l FROM Loan l WHERE l.status = 'ACTIVE' AND l.estimatedReturnDate < :currentDate")
    List<Loan> findOverdueLoans(@Param("currentDate") LocalDate currentDate);

    /**
     * Finds overdue loans with pagination.
     *
     * @param currentDate the reference date
     * @param pageable pagination information
     * @return paginated list of overdue loans
     */
    @Query("SELECT l FROM Loan l WHERE l.status = 'ACTIVE' AND l.estimatedReturnDate < :currentDate")
    Page<Loan> findOverdueLoans(@Param("currentDate") LocalDate currentDate, Pageable pageable);

    /**
     * Finds overdue loans using the current date.
     *
     * @return list of overdue loans
     */
    default List<Loan> findOverdueLoans() {
        return findOverdueLoans(LocalDate.now());
    }

    /**
     * Finds overdue loans using the current date with pagination.
     *
     * @param pageable pagination information
     * @return paginated list of overdue loans
     */
    default Page<Loan> findOverdueLoans(Pageable pageable) {
        return findOverdueLoans(LocalDate.now(), pageable);
    }

    /**
     * Finds active loans for a specific user and game.
     *
     * @param user the user entity
     * @param game the game entity
     * @return list of active loans
     */
    @Query("SELECT l FROM Loan l WHERE l.user = :user AND l.game = :game AND l.status = 'ACTIVE'")
    List<Loan> findActiveLoansForUserAndGame(@Param("user") User user, @Param("game") Game game);

    /**
     * Counts the number of active loans for a given game.
     *
     * @param game the game entity
     * @return number of active loans
     */
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.game = :game AND l.status = 'ACTIVE'")
    long countActiveLoansForGame(@Param("game") Game game);

    /**
     * Finds the most recent active loan for a game, ordered by loan date.
     *
     * @param game the game entity
     * @return list with the most recent active loan (first item)
     */
    @Query("SELECT l FROM Loan l WHERE l.game = :game AND l.status = 'ACTIVE' ORDER BY l.loanDate DESC")
    List<Loan> findMostRecentActiveLoanForGame(@Param("game") Game game);

    /**
     * Finds active loans that overlap with a specified date range.
     *
     * @param game the game entity
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return list of overlapping loans
     */
    @Query("SELECT l FROM Loan l WHERE l.game = :game AND l.status = 'ACTIVE' AND " +
           "l.loanDate <= :endDate AND l.estimatedReturnDate >= :startDate")
    List<Loan> findLoansOverlappingPeriod(@Param("game") Game game,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);
}
