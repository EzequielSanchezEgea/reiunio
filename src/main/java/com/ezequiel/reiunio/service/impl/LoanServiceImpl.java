package com.ezequiel.reiunio.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ezequiel.reiunio.entity.Game;
import com.ezequiel.reiunio.entity.Loan;
import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.LoanStatus;
import com.ezequiel.reiunio.repository.GameRepository;
import com.ezequiel.reiunio.repository.LoanRepository;
import com.ezequiel.reiunio.service.LoanService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final GameRepository gameRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Loan> findAll() {
        log.debug("Finding all loans");
        return loanRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Loan> findById(Long id) {
        log.debug("Finding loan by id: {}", id);
        return loanRepository.findById(id);
    }

    @Override
    @Transactional
    public Loan save(Loan loan) {
        log.debug("Saving loan for user: {}", loan.getUser().getUsername());
        return loanRepository.save(loan);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.debug("Deleting loan by id: {}", id);
        loanRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Loan> findByUser(User user) {
        log.debug("Finding loans by user: {}", user.getUsername());
        return loanRepository.findByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Loan> findByGame(Game game) {
        log.debug("Finding loans by game: {}", game.getName());
        return loanRepository.findByGame(game);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Loan> findByStatus(LoanStatus status) {
        log.debug("Finding loans by status: {}", status);
        return loanRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public Loan createLoan(User user, Game game, LocalDate estimatedReturnDate) {
        log.debug("Creating loan: game {} for user {}", game.getName(), user.getUsername());
        
        // Verify that the game is available
        if (!game.getAvailable()) {
            throw new IllegalStateException("Game is not available for loan");
        }
        
        // Create new loan
        Loan loan = Loan.builder()
                .user(user)
                .game(game)
                .loanDate(LocalDate.now())
                .estimatedReturnDate(estimatedReturnDate)
                .status(LoanStatus.ACTIVE)
                .build();
        
        // Mark the game as unavailable
        game.setAvailable(false);
        gameRepository.save(game);
        
        return loanRepository.save(loan);
    }

    @Override
    @Transactional
    public Loan registerReturn(Long loanId, LocalDate returnDate) {
        log.debug("Registering return for loan id: {}", loanId);
        Optional<Loan> optLoan = loanRepository.findById(loanId);
        if (optLoan.isPresent()) {
            Loan loan = optLoan.get();
            
            // Verify that the loan is active
            if (loan.getStatus() != LoanStatus.ACTIVE) {
                throw new IllegalStateException("Loan is not active, cannot register return");
            }
            
            // Register the return
            loan.setActualReturnDate(returnDate);
            
            // Determine if there's a delay
            if (returnDate.isAfter(loan.getEstimatedReturnDate())) {
                loan.setStatus(LoanStatus.LATE);
            } else {
                loan.setStatus(LoanStatus.RETURNED);
            }
            
            // Mark the game as available
            Game game = loan.getGame();
            game.setAvailable(true);
            gameRepository.save(game);
            
            return loanRepository.save(loan);
        } else {
            throw new IllegalArgumentException("Loan not found with ID: " + loanId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Loan> findOverdueLoans() {
        log.debug("Finding overdue loans");
        return loanRepository.findOverdueLoans();
    }
}