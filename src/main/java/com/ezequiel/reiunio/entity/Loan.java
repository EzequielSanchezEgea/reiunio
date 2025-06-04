package com.ezequiel.reiunio.entity;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.ezequiel.reiunio.enums.LoanStatus;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Entity representing a loan of a board game to a user.
 * Includes loan dates, return tracking, and loan status.
 */
@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    /**
     * Primary key for the loan entry.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who borrowed the game.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The game that was borrowed.
     */
    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    /**
     * Date the loan was issued. Defaults to current date.
     */
    @NotNull
    @Column(name = "loan_date")
    @Builder.Default
    private LocalDate loanDate = LocalDate.now();

    /**
     * Estimated date by which the game should be returned.
     */
    @NotNull
    @Column(name = "estimated_return_date")
    private LocalDate estimatedReturnDate;

    /**
     * The actual date the game was returned, if returned.
     */
    @Column(name = "actual_return_date")
    private LocalDate actualReturnDate;

    /**
     * Status of the loan: ACTIVE, RETURNED, or LATE.
     */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LoanStatus status = LoanStatus.ACTIVE;

    /**
     * Registers the return of the game.
     * Updates the return date and adjusts the status accordingly.
     * Also sets the game as available again.
     *
     * @param returnDate The date the game was returned.
     */
    public void registerReturn(LocalDate returnDate) {
        this.actualReturnDate = returnDate;
        this.status = returnDate.isAfter(estimatedReturnDate) ? 
                LoanStatus.LATE : LoanStatus.RETURNED;

        if (this.game != null) {
            this.game.setAvailable(true);
        }
    }

    /**
     * Calculates how many days late the return is.
     *
     * @return Number of delayed days, or 0 if returned on time or early.
     */
    public int calculateDelayDays() {
        if (actualReturnDate == null) {
            LocalDate today = LocalDate.now();
            if (today.isAfter(estimatedReturnDate)) {
                return (int) ChronoUnit.DAYS.between(estimatedReturnDate, today);
            }
        } else if (actualReturnDate.isAfter(estimatedReturnDate)) {
            return (int) ChronoUnit.DAYS.between(estimatedReturnDate, actualReturnDate);
        }
        return 0;
    }
}
