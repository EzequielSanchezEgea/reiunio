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

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final GameSessionService gameSessionService;
    private final LoanService loanService;
    private final UserService userService;

    /**
     * Checks if a user is the creator of a game session
     * @param sessionId Game session ID
     * @param username Username
     * @return true if the user is the creator of the game session, false otherwise
     */
    public boolean isGameSessionCreator(Long sessionId, String username) {
        Optional<GameSession> gameSession = gameSessionService.findById(sessionId);
        return gameSession.isPresent() && 
               gameSession.get().getCreator().getUsername().equals(username);
    }

    /**
     * Checks if a loan belongs to a user
     * @param loanId Loan ID
     * @param username Username
     * @return true if the loan belongs to the user, false otherwise
     */
    public boolean isUserLoan(Long loanId, String username) {
        Optional<Loan> loan = loanService.findById(loanId);
        return loan.isPresent() && 
               loan.get().getUser().getUsername().equals(username);
    }

    /**
     * Checks if the current user is the same as the one being viewed
     * @param userId User ID to check
     * @param currentUsername Current logged in username
     * @return true if it's the same user, false otherwise
     */
    public boolean isCurrentUser(Long userId, String currentUsername) {
        Optional<User> user = userService.findById(userId);
        return user.isPresent() && 
               user.get().getUsername().equals(currentUsername);
    }
}