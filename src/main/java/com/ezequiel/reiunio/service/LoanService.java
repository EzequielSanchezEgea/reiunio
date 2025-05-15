package com.ezequiel.reiunio.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.ezequiel.reiunio.entity.Game;
import com.ezequiel.reiunio.entity.Loan;
import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.LoanStatus;

public interface LoanService {
    
    List<Loan> findAll();
    
    Optional<Loan> findById(Long id);
    
    Loan save(Loan loan);
    
    void deleteById(Long id);
    
    List<Loan> findByUser(User user);
    
    List<Loan> findByGame(Game game);
    
    List<Loan> findByStatus(LoanStatus status);
    
    Loan createLoan(User user, Game game, LocalDate estimatedReturnDate);
    
    Loan registerReturn(Long loanId, LocalDate returnDate);
    
    List<Loan> findOverdueLoans();
}