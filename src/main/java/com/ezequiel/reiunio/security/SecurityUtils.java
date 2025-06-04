package com.ezequiel.reiunio.security;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.ezequiel.reiunio.entity.GameSession;
import com.ezequiel.reiunio.entity.Loan;
import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.service.GameSessionService;
import com.ezequiel.reiunio.service.LoanService;
import com.ezequiel.reiunio.service.UserService;

import lombok.RequiredArgsConstructor;

/**
 * Utility class for performing security-related checks 
 * involving game sessions, loans, and users.
 */
@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final GameSessionService gameSessionService;
    private final LoanService loanService;
    private final UserService userService;

    /**
     * Determines if the given username matches the creator of the specified game session.
     *
     * @param sessionId the ID of the game session
     * @param username the username to check
     * @return true if the user is the creator, false otherwise
     */
    public boolean isGameSessionCreator(Long sessionId, String username) {
        Optional<GameSession> gameSession = gameSessionService.findById(sessionId);
        return gameSession.isPresent() &&
               gameSession.get().getCreator().getUsername().equals(username);
    }

    /**
     * Determines if the specified loan belongs to the given username.
     *
     * @param loanId the ID of the loan
     * @param username the username to check
     * @return true if the loan belongs to the user, false otherwise
     */
    public boolean isUserLoan(Long loanId, String username) {
        Optional<Loan> loan = loanService.findById(loanId);
        return loan.isPresent() &&
               loan.get().getUser().getUsername().equals(username);
    }

    /**
     * Determines if the specified user ID belongs to the currently authenticated user.
     *
     * @param userId the ID of the user to check
     * @param currentUsername the username of the currently logged-in user
     * @return true if the user ID matches the current user, false otherwise
     */
    public boolean isCurrentUser(Long userId, String currentUsername) {
        Optional<User> user = userService.findById(userId);
        return user.isPresent() &&
               user.get().getUsername().equals(currentUsername);
    }
}
