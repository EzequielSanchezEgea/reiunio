package com.ezequiel.reiunio.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ezequiel.reiunio.entity.Game;
import com.ezequiel.reiunio.entity.GameSession;
import com.ezequiel.reiunio.entity.GameSessionInfo;
import com.ezequiel.reiunio.entity.Loan;
import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.GameSessionStatus;
import com.ezequiel.reiunio.enums.LoanStatus;
import com.ezequiel.reiunio.repository.GameRepository;
import com.ezequiel.reiunio.repository.GameSessionRepository;
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
    private final GameSessionRepository gameSessionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Loan> findAll() {
        log.debug("Finding all loans");
        return loanRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Loan> findAllPaginated(Pageable pageable) {
        log.debug("Finding all loans with pagination");
        return loanRepository.findAll(pageable);
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
    public Page<Loan> findByUserPaginated(User user, Pageable pageable) {
        log.debug("Finding loans by user: {} with pagination", user.getUsername());
        return loanRepository.findByUser(user, pageable);
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
    @Transactional(readOnly = true)
    public Page<Loan> findByStatusPaginated(LoanStatus status, Pageable pageable) {
        log.debug("Finding loans by status: {} with pagination", status);
        return loanRepository.findByStatus(status, pageable);
    }

    @Override
    @Transactional
    public Loan createLoan(User user, Game game, LocalDate estimatedReturnDate) {
        log.debug("Creating loan: game {} for user {}", game.getName(), user.getUsername());
        
        // Verificar que el juego esté disponible para préstamos
        if (!game.getAvailable()) {
            throw new IllegalStateException("Game '" + game.getName() + "' is not available for loans");
        }
        
        // Verificar que el usuario no tenga un préstamo activo de este juego
        List<Loan> activeLoansForGame = loanRepository.findByGameAndStatus(game, LoanStatus.ACTIVE);
        Optional<Loan> userActiveLoan = activeLoansForGame.stream()
                .filter(loan -> loan.getUser().getId().equals(user.getId()))
                .findFirst();
        
        if (userActiveLoan.isPresent()) {
            throw new IllegalStateException("User '" + user.getUsername() + "' already has an active loan for game '" + game.getName() + "'");
        }
        
        // Verificar si hay otros préstamos activos para este juego
        if (!activeLoansForGame.isEmpty()) {
            Loan existingLoan = activeLoansForGame.get(0);
            throw new IllegalStateException("Game '" + game.getName() + "' is already on loan to user '" + 
                    existingLoan.getUser().getUsername() + "' until " + 
                    existingLoan.getEstimatedReturnDate());
        }
        
        Loan loan = Loan.builder()
                .user(user)
                .game(game)
                .loanDate(LocalDate.now())
                .estimatedReturnDate(estimatedReturnDate)
                .status(LoanStatus.ACTIVE)
                .build();
        
        // Marcar el juego como no disponible para préstamos
        game.setAvailable(false);
        gameRepository.save(game);
        
        log.info("Loan created for game '{}' - game marked as unavailable for loans", game.getName());
        
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
            
            // Marcar el juego como disponible para préstamos nuevamente
            Game game = loan.getGame();
            game.setAvailable(true);
            gameRepository.save(game);
            
            log.info("Loan returned for game '{}' - game marked as available for loans", 
                    game.getName());
            
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

    @Override
    @Transactional(readOnly = true)
    public Page<Loan> findOverdueLoansPaginated(Pageable pageable) {
        log.debug("Finding overdue loans with pagination");
        return loanRepository.findOverdueLoans(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSessionInfo> findUpcomingSessionsForGame(Game game) {
        log.debug("Finding upcoming sessions for game: {}", game.getName());
        
        LocalDate today = LocalDate.now();
        
        // Buscar sesiones programadas que usan este juego y empiezan hoy o en el futuro
        List<GameSession> upcomingSessions = gameSessionRepository.findByGame(game)
                .stream()
                .filter(session -> session.getStatus() == GameSessionStatus.SCHEDULED)
                .filter(session -> !session.getStartDate().isBefore(today))
                .sorted((s1, s2) -> s1.getStartDate().compareTo(s2.getStartDate()))
                .collect(Collectors.toList());
        
        return upcomingSessions.stream()
                .map(this::convertToGameSessionInfo)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LocalDate suggestReturnDate(Game game, LocalDate proposedReturnDate) {
        log.debug("Suggesting return date for game: {} with proposed date: {}", 
                 game.getName(), proposedReturnDate);
        
        List<GameSessionInfo> upcomingSessions = findUpcomingSessionsForGame(game);
        
        if (upcomingSessions.isEmpty()) {
            // No hay sesiones programadas, la fecha propuesta está bien
            return proposedReturnDate;
        }
        
        // Buscar la primera sesión que conflicte con el período de préstamo
        for (GameSessionInfo session : upcomingSessions) {
            LocalDate sessionStart = session.getStartDate();
            LocalDate sessionEnd = session.getEndDate();
            
            // Si la fecha propuesta de devolución está durante una sesión, sugerir antes
            if (!proposedReturnDate.isBefore(sessionStart) && !proposedReturnDate.isAfter(sessionEnd)) {
                // Sugerir el día antes del inicio de la sesión
                LocalDate suggestedDate = sessionStart.minusDays(1);
                
                // Si la fecha sugerida es antes de hoy, mantener el día propuesto
                if (suggestedDate.isBefore(LocalDate.now())) {
                    return proposedReturnDate;
                }
                
                log.info("Suggesting return date {} (day before session starting {}) instead of {}", 
                        suggestedDate, sessionStart, proposedReturnDate);
                return suggestedDate;
            }
            
            // Si hay una sesión que empieza antes de la fecha propuesta pero termina después
            if (sessionStart.isBefore(proposedReturnDate) && sessionEnd.isAfter(proposedReturnDate)) {
                // Sugerir el día antes del inicio de la sesión
                LocalDate suggestedDate = sessionStart.minusDays(1);
                
                if (suggestedDate.isBefore(LocalDate.now())) {
                    return proposedReturnDate;
                }
                
                log.info("Suggesting return date {} (avoiding session conflict) instead of {}", 
                        suggestedDate, proposedReturnDate);
                return suggestedDate;
            }
        }
        
        // No hay conflictos, mantener fecha propuesta
        return proposedReturnDate;
    }

    /**
     * Verifica si un juego está programado para ser usado en sesiones próximas
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isGameScheduledForUpcomingSessions(Game game) {
        List<GameSessionInfo> upcomingSessions = findUpcomingSessionsForGame(game);
        return !upcomingSessions.isEmpty();
    }

    /**
     * Obtiene información detallada sobre conflictos potenciales de un préstamo
     */
    @Override
    @Transactional(readOnly = true)
    public LoanConflictInfo checkLoanConflicts(Game game, LocalDate proposedReturnDate) {
        List<GameSessionInfo> upcomingSessions = findUpcomingSessionsForGame(game);
        
        if (upcomingSessions.isEmpty()) {
            return new LoanConflictInfo(false, null, proposedReturnDate, "No upcoming sessions scheduled for this game.");
        }
        
        // Buscar sesiones que conflicten con el período de préstamo
        List<GameSessionInfo> conflictingSessions = upcomingSessions.stream()
                .filter(session -> {
                    LocalDate sessionStart = session.getStartDate();
                    LocalDate sessionEnd = session.getEndDate();
                    
                    // Verificar si la fecha de devolución propuesta cae durante una sesión
                    return !proposedReturnDate.isBefore(sessionStart) && !proposedReturnDate.isAfter(sessionEnd);
                })
                .collect(Collectors.toList());
        
        if (conflictingSessions.isEmpty()) {
            return new LoanConflictInfo(false, upcomingSessions, proposedReturnDate, 
                    "No conflicts with upcoming sessions.");
        }
        
        // Hay conflictos, sugerir nueva fecha
        GameSessionInfo firstConflict = conflictingSessions.get(0);
        LocalDate suggestedDate = firstConflict.getStartDate().minusDays(1);
        
        if (suggestedDate.isBefore(LocalDate.now())) {
            suggestedDate = proposedReturnDate; // Mantener fecha original si no se puede sugerir antes
        }
        
        String warningMessage = String.format(
                "Warning: The proposed return date conflicts with the session '%s' on %s. " +
                "Consider returning the game by %s to avoid conflicts.",
                firstConflict.getSessionTitle(),
                firstConflict.getFormattedDateRange(),
                suggestedDate
        );
        
        return new LoanConflictInfo(true, upcomingSessions, suggestedDate, warningMessage);
    }

    private GameSessionInfo convertToGameSessionInfo(GameSession session) {
        return GameSessionInfo.builder()
                .sessionId(session.getId())
                .sessionTitle(session.getTitle())
                .startDate(session.getStartDate())
                .startTime(session.getStartTime())
                .endDate(session.getEndDate())
                .endTime(session.getEndTime())
                .creatorName(session.getCreator().getFullName())
                .status(session.getStatus().toString())
                .isMultiDay(session.isMultiDay())
                .build();
    }

    /**
     * Clase para encapsular información sobre conflictos de préstamos
     */
    public static class LoanConflictInfo {
        private final boolean hasConflicts;
        private final List<GameSessionInfo> upcomingSessions;
        private final LocalDate suggestedReturnDate;
        private final String warningMessage;

        public LoanConflictInfo(boolean hasConflicts, List<GameSessionInfo> upcomingSessions, 
                               LocalDate suggestedReturnDate, String warningMessage) {
            this.hasConflicts = hasConflicts;
            this.upcomingSessions = upcomingSessions;
            this.suggestedReturnDate = suggestedReturnDate;
            this.warningMessage = warningMessage;
        }

        public boolean hasConflicts() { return hasConflicts; }
        public List<GameSessionInfo> getUpcomingSessions() { return upcomingSessions; }
        public LocalDate getSuggestedReturnDate() { return suggestedReturnDate; }
        public String getWarningMessage() { return warningMessage; }
    }
}