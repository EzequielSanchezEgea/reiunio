package com.ezequiel.reiunio.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ezequiel.reiunio.entity.Game;
import com.ezequiel.reiunio.entity.Loan;
import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.LoanStatus;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    
    List<Loan> findByUser(User user);
    
    List<Loan> findByGame(Game game);
    
    List<Loan> findByStatus(LoanStatus status);
    
    List<Loan> findByEstimatedReturnDateBeforeAndStatus(LocalDate date, LoanStatus status);
    
    @Query("SELECT l FROM Loan l WHERE l.estimatedReturnDate < CURRENT_DATE AND l.status = 'ACTIVE'")
    List<Loan> findOverdueLoans();
}