package com.ezequiel.reiunio.service.impl;

import java.time.LocalDate;
import java.util.Comparator;
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

/**
 * Service implementation for managing game loans.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final GameRepository gameRepository;
    private final GameSessionRepository gameSessionRepository;

    /**
     * Retrieves all loans.
     *
     * @return a list of all loans
     */
    @Override
    @Transactional(readOnly = true)
    public List<Loan> findAll() {
        log.debug("Finding all loans");
        return loanRepository.findAll();
    }

    /**
     * Retrieves all loans with pagination support.
     *
     * @param pageable the pagination information
     * @return a paginated list of loans
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Loan> findAllPaginated(Pageable pageable) {
        log.debug("Finding all loans with pagination");
        return loanRepository.findAll(pageable);
    }

    /**
     * Finds a loan by its ID.
     *
     * @param id the ID of the loan
     * @return an optional loan
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Loan> findById(Long id) {
        log.debug("Finding loan by id: {}", id);
        return loanRepository.findById(id);
    }

    /**
     * Saves a loan entity.
     *
     * @param loan the loan to save
     * @return the persisted loan
     */
    @Override
    @Transactional
    public Loan save(Loan loan) {
        log.debug("Saving loan for user: {}", loan.getUser().getUsername());
        return loanRepository.save(loan);
    }

    /**
     * Deletes a loan by ID.
     *
     * @param id the ID of the loan to delete
     */
    @Override
    @Transactional
    public void deleteById(Long id) {
        log.debug("Deleting loan by id: {}", id);
        loanRepository.deleteById(id);
    }

    /**
     * Finds loans by user.
     *
     * @param user the user
     * @return a list of loans for the specified user
     */
    @Override
    @Transactional(readOnly = true)
    public List<Loan> findByUser(User user) {
        log.debug("Finding loans by user: {}", user.getUsername());
        return loanRepository.findByUser(user);
    }

    /**
     * Finds loans by user with pagination.
     *
     * @param user     the user
     * @param pageable the pagination information
     * @return a paginated list of loans
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Loan> findByUserPaginated(User user, Pageable pageable) {
        log.debug("Finding loans by user: {} with pagination", user.getUsername());
        return loanRepository.findByUser(user, pageable);
    }

    /**
     * Finds loans for a specific game.
     *
     * @param game the game
     * @return a list of loans for the game
     */
    @Override
    @Transactional(readOnly = true)
    public List<Loan> findByGame(Game game) {
        log.debug("Finding loans by game: {}", game.getName());
        return loanRepository.findByGame(game);
    }

    /**
     * Finds loans by status.
     *
     * @param status the loan status
     * @return a list of loans with the given status
     */
    @Override
    @Transactional(readOnly = true)
    public List<Loan> findByStatus(LoanStatus status) {
        log.debug("Finding loans by status: {}", status);
        return loanRepository.findByStatus(status);
    }

    /**
     * Finds loans by status with pagination.
     *
     * @param status   the loan status
     * @param pageable the pagination information
     * @return a paginated list of loans
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Loan> findByStatusPaginated(LoanStatus status, Pageable pageable) {
        log.debug("Finding loans by status: {} with pagination", status);
        return loanRepository.findByStatus(status, pageable);
    }

    /**
     * Creates a new loan for a game and user, checking availability and conflicts.
     *
     * @param user                the user
     * @param game                the game
     * @param estimatedReturnDate the estimated return date
     * @return the created loan
     */
    @Override
    @Transactional
    public Loan createLoan(User user, Game game, LocalDate estimatedReturnDate) {
        log.debug("Creating loan: game {} for user {}", game.getName(), user.getUsername());

        if (!game.getAvailable()) {
            throw new IllegalStateException("Game is not available for loans");
        }

        List<Loan> activeLoansForGame = loanRepository.findByGameAndStatus(game, LoanStatus.ACTIVE);
        Optional<Loan> userActiveLoan = activeLoansForGame.stream()
                .filter(loan -> loan.getUser().getId().equals(user.getId()))
                .findFirst();

        if (userActiveLoan.isPresent()) {
            throw new IllegalStateException("User already has an active loan for this game");
        }

        if (!activeLoansForGame.isEmpty()) {
            throw new IllegalStateException("Game is already loaned to another user");
        }

        Loan loan = Loan.builder()
                .user(user)
                .game(game)
                .loanDate(LocalDate.now())
                .estimatedReturnDate(estimatedReturnDate)
                .status(LoanStatus.ACTIVE)
                .build();

        game.setAvailable(false);
        gameRepository.save(game);

        return loanRepository.save(loan);
    }

    /**
     * Registers the return of a loan and updates the loan status accordingly.
     *
     * @param loanId     the ID of the loan
     * @param returnDate the actual return date
     * @return the updated loan
     */
    @Override
    @Transactional
    public Loan registerReturn(Long loanId, LocalDate returnDate) {
        log.debug("Registering return for loan id: {}", loanId);
        Optional<Loan> optLoan = loanRepository.findById(loanId);
        if (optLoan.isPresent()) {
            Loan loan = optLoan.get();

            if (loan.getStatus() != LoanStatus.ACTIVE) {
                throw new IllegalStateException("Loan is not active");
            }

            loan.setActualReturnDate(returnDate);
            loan.setStatus(returnDate.isAfter(loan.getEstimatedReturnDate()) ? LoanStatus.LATE : LoanStatus.RETURNED);

            Game game = loan.getGame();
            game.setAvailable(true);
            gameRepository.save(game);

            return loanRepository.save(loan);
        } else {
            throw new IllegalArgumentException("Loan not found");
        }
    }

    /**
     * Finds all overdue loans.
     *
     * @return a list of overdue loans
     */
    @Override
    @Transactional(readOnly = true)
    public List<Loan> findOverdueLoans() {
        log.debug("Finding overdue loans");
        return loanRepository.findOverdueLoans();
    }

    /**
     * Finds overdue loans with pagination.
     *
     * @param pageable pagination info
     * @return a paginated list of overdue loans
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Loan> findOverdueLoansPaginated(Pageable pageable) {
        log.debug("Finding overdue loans with pagination");
        return loanRepository.findOverdueLoans(pageable);
    }

    /**
     * Finds upcoming scheduled sessions for a specific game.
     *
     * @param game the game
     * @return a list of upcoming game sessions
     */
    @Override
    @Transactional(readOnly = true)
    public List<GameSessionInfo> findUpcomingSessionsForGame(Game game) {
        log.debug("Finding upcoming sessions for game: {}", game.getName());

        LocalDate today = LocalDate.now();

        return gameSessionRepository.findByGame(game).stream()
                .filter(session -> session.getStatus() == GameSessionStatus.SCHEDULED)
                .filter(session -> !session.getStartDate().isBefore(today))
                .sorted(Comparator.comparing(GameSession::getStartDate))
                .map(this::convertToGameSessionInfo)
                .collect(Collectors.toList());
    }

    /**
     * Suggests a return date based on upcoming sessions for the game.
     *
     * @param game               the game
     * @param proposedReturnDate the proposed return date
     * @return a suggested return date
     */
    @Override
    @Transactional(readOnly = true)
    public LocalDate suggestReturnDate(Game game, LocalDate proposedReturnDate) {
        log.debug("Suggesting return date for game: {} with proposed date: {}", game.getName(), proposedReturnDate);

        List<GameSessionInfo> upcomingSessions = findUpcomingSessionsForGame(game);

        if (upcomingSessions.isEmpty()) return proposedReturnDate;

        return upcomingSessions.stream()
                .map(GameSessionInfo::getStartDate)
                .filter(date -> !date.isAfter(proposedReturnDate))
                .min(LocalDate::compareTo)
                .map(conflictDate -> {
                    LocalDate suggestion = conflictDate.minusDays(1);
                    return suggestion.isBefore(LocalDate.now().plusDays(1)) ? LocalDate.now().plusDays(1) : suggestion;
                })
                .orElse(proposedReturnDate);
    }

    /**
     * Checks if a game is scheduled for any upcoming sessions.
     *
     * @param game the game
     * @return true if the game is scheduled, false otherwise
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isGameScheduledForUpcomingSessions(Game game) {
        return !findUpcomingSessionsForGame(game).isEmpty();
    }

    /**
     * Checks for scheduling conflicts between a proposed loan return date and future sessions.
     *
     * @param game               the game
     * @param proposedReturnDate the proposed return date
     * @return a LoanConflictInfo object describing any conflicts
     */
    @Override
    @Transactional(readOnly = true)
    public LoanConflictInfo checkLoanConflicts(Game game, LocalDate proposedReturnDate) {
        List<GameSessionInfo> sessions = findUpcomingSessionsForGame(game);
        if (sessions.isEmpty()) {
            return new LoanConflictInfo(false, null, proposedReturnDate, "No upcoming sessions for this game.");
        }

        GameSessionInfo first = sessions.stream()
                .min(Comparator.comparing(GameSessionInfo::getStartDate))
                .orElse(sessions.get(0));

        LocalDate suggestion = first.getStartDate().minusDays(1);
        if (suggestion.isBefore(LocalDate.now().plusDays(1))) {
            suggestion = LocalDate.now().plusDays(1);
        }

        String warning = String.format(
                "This game is scheduled for an upcoming session: '%s' on %s. Please return it by %s.",
                first.getSessionTitle(),
                first.getStartDate(),
                suggestion
        );

        return new LoanConflictInfo(true, sessions, suggestion, warning);
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
     * Contains details about potential loan conflicts with upcoming game sessions.
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
