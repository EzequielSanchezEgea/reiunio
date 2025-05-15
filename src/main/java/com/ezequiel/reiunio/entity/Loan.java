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

@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @NotNull
    @Column(name = "loan_date")
    @Builder.Default
    private LocalDate loanDate = LocalDate.now();

    @NotNull
    @Column(name = "estimated_return_date")
    private LocalDate estimatedReturnDate;

    @Column(name = "actual_return_date")
    private LocalDate actualReturnDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LoanStatus status = LoanStatus.ACTIVE;

    public void registerReturn(LocalDate returnDate) {
        this.actualReturnDate = returnDate;
        this.status = returnDate.isAfter(estimatedReturnDate) ? 
                LoanStatus.LATE : LoanStatus.RETURNED;
        
        if (this.game != null) {
            this.game.setAvailable(true);
        }
    }

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