package com.ezequiel.reiunio.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ezequiel.reiunio.entity.Game;
import com.ezequiel.reiunio.entity.GameSession;
import com.ezequiel.reiunio.entity.GameSessionPlayer;
import com.ezequiel.reiunio.entity.GameSessionPlayerId;
import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.GameSessionStatus;
import com.ezequiel.reiunio.repository.GameRepository;
import com.ezequiel.reiunio.repository.GameSessionPlayerRepository;
import com.ezequiel.reiunio.repository.GameSessionRepository;
import com.ezequiel.reiunio.repository.UserRepository;
import com.ezequiel.reiunio.service.GameSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the {@link GameSessionService} interface.
 *
 * <p>This service handles all business logic related to game sessions,
 * including creation, retrieval, updating, and deletion operations.
 * It also manages player participation and ensures session status consistency,
 * especially for expired sessions.
 *
 * <p>Key features:
 * <ul>
 *   <li>Automatic finalization of expired sessions</li>
 *   <li>Support for filtering sessions by date, status, creator, or game</li>
 *   <li>Player management (add/remove/confirm)</li>
 *   <li>Multi-day and single-day session handling</li>
 * </ul>
 *
 * @see GameSession
 * @see GameSessionRepository
 * @see GameSessionService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GameSessionServiceImpl implements GameSessionService {

    private final GameSessionRepository gameSessionRepository;
    private final UserRepository userRepository;
    private final GameSessionPlayerRepository gameSessionPlayerRepository;
    private final GameRepository gameRepository;

    /**
     * Retrieves all existing game sessions after checking and automatically finishing any that have expired.
     *
     * @return a list of all game sessions
     */
    @Override
    @Transactional
    public List<GameSession> findAll() {
        log.debug("Finding all game sessions");
        autoFinishExpiredSessions();
        return gameSessionRepository.findAll();
    }

    /**
     * Retrieves a game session by its unique identifier.
     *
     * @param id the ID of the session to retrieve
     * @return an Optional containing the session if found, or empty otherwise
     */
    @Override
    @Transactional
    public Optional<GameSession> findById(Long id) {
        log.debug("Finding game session by id: {}", id);
        autoFinishExpiredSessions();
        return gameSessionRepository.findById(id);
    }

    /**
     * Retrieves all game sessions with the specified status.
     *
     * @param status the status to filter by
     * @return a list of matching game sessions
     */
    @Override
    @Transactional
    public List<GameSession> findByStatus(GameSessionStatus status) {
        log.debug("Finding game sessions by status: {}", status);
        autoFinishExpiredSessions();
        return gameSessionRepository.findByStatus(status);
    }

    /**
     * Retrieves all upcoming game sessions (those scheduled in the future).
     *
     * @return a list of upcoming sessions
     */
    @Override
    @Transactional
    public List<GameSession> findUpcomingSessions() {
        log.debug("Finding upcoming game sessions");
        autoFinishExpiredSessions();
        return gameSessionRepository.findUpcomingSessions();
    }

    /**
     * Retrieves all game sessions scheduled for today.
     *
     * @return a list of today's sessions
     */
    @Override
    @Transactional
    public List<GameSession> findTodaySessions() {
        log.debug("Finding today's game sessions");
        autoFinishExpiredSessions();
        return gameSessionRepository.findTodaySessions();
    }

    /**
     * Automatically checks and finishes expired sessions based on current date and time.
     * This method ensures data integrity by updating session statuses accordingly.
     */
    private void autoFinishExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();
        List<GameSession> scheduledSessions = gameSessionRepository.findByStatus(GameSessionStatus.SCHEDULED);
        for (GameSession session : scheduledSessions) {
            if (hasSessionExpired(session, today, currentTime)) {
                try {
                    session.setStatus(GameSessionStatus.FINISHED);
                    gameSessionRepository.save(session);
                    log.info("Session '{}' automatically marked as FINISHED", session.getTitle());
                } catch (Exception e) {
                    log.error("Error finalizing session {}: {}", session.getId(), e.getMessage());
                }
            }
        }
    }

    /**
     * Determines whether the given session has expired based on current time.
     *
     * @param session the session to check
     * @param today the current date
     * @param currentTime the current time
     * @return true if the session is expired, false otherwise
     */
    private boolean hasSessionExpired(GameSession session, LocalDate today, LocalTime currentTime) {
        LocalDate endDate = session.getEndDate();
        LocalTime endTime = session.getEndTime();
        if (endDate.isBefore(today)) {
            return true;
        }
        if (endDate.equals(today) && endTime != null) {
            return currentTime.isAfter(endTime);
        }
        if (endDate.equals(today) && endTime == null) {
            return currentTime.isAfter(LocalTime.of(23, 59));
        }
        return false;
    }

    /**
     * Saves a new or updates an existing game session.
     *
     * @param gameSession the session to save or update
     * @return the saved session
     */
    @Override
    @Transactional
    public GameSession save(GameSession gameSession) {
        boolean isNewSession = gameSession.getId() == null;
        if (isNewSession) {
            gameSession.setStatus(GameSessionStatus.SCHEDULED);
            log.info("New session created: '{}' for game '{}'",
                    gameSession.getTitle(), gameSession.getCustomGameName());
            if (gameSession.getGame() != null) {
                log.info("Session uses library game '{}', availability not changed",
                        gameSession.getGame().getName());
            }
        }
        log.debug("Saving game session: {}", gameSession.getTitle());
        return gameSessionRepository.save(gameSession);
    }

    /**
     * Deletes a game session by its ID.
     *
     * @param id the ID of the session to delete
     */
    @Override
    @Transactional
    public void deleteById(Long id) {
        log.debug("Deleting game session by id: {}", id);
        Optional<GameSession> sessionOpt = gameSessionRepository.findById(id);
        if (sessionOpt.isPresent()) {
            GameSession session = sessionOpt.get();
            if (session.getGame() != null) {
                log.info("Deleting session with library game '{}', availability not changed",
                        session.getGame().getName());
            }
        }
        gameSessionRepository.deleteById(id);
    }

    /**
     * Manually marks a session as finished.
     *
     * @param sessionId the ID of the session to finish
     * @return the updated session
     * @throws IllegalArgumentException if the session is not found
     */
    @Transactional
    public GameSession finishSession(Long sessionId) {
        Optional<GameSession> sessionOpt = findById(sessionId);
        if (sessionOpt.isPresent()) {
            GameSession session = sessionOpt.get();
            session.setStatus(GameSessionStatus.FINISHED);
            session = gameSessionRepository.save(session);
            log.info("Session '{}' marked as FINISHED", session.getTitle());
            return session;
        }
        throw new IllegalArgumentException("Session not found with ID: " + sessionId);
    }

    /**
     * Retrieves all game sessions created by the specified user.
     *
     * @param creator the user who created the sessions
     * @return a list of sessions created by the user
     */
    @Override
    @Transactional(readOnly = true)
    public List<GameSession> findByCreator(User creator) {
        log.debug("Finding game sessions by creator: {}", creator.getUsername());
        return gameSessionRepository.findByCreator(creator);
    }

    /**
     * Retrieves all game sessions associated with the specified game.
     *
     * @param game the game to search for
     * @return a list of sessions using the specified game
     */
    @Override
    @Transactional(readOnly = true)
    public List<GameSession> findByGame(Game game) {
        log.debug("Finding game sessions by game: {}", game.getName());
        return gameSessionRepository.findByGame(game);
    }

    /**
     * Retrieves all game sessions occurring between two dates.
     *
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return a list of sessions within the specified date range
     */
    @Override
    @Transactional(readOnly = true)
    public List<GameSession> findSessionsBetweenDates(LocalDate startDate, LocalDate endDate) {
        log.debug("Finding game sessions between dates: {} and {}", startDate, endDate);
        return gameSessionRepository.findSessionsInDateRange(startDate, endDate);
    }

    /**
     * Retrieves all game sessions where the specified user is registered as a player.
     *
     * @param userId the ID of the user to search for
     * @return a list of sessions where the user participates
     */
    @Override
    @Transactional(readOnly = true)
    public List<GameSession> findSessionsByPlayer(Long userId) {
        log.debug("Finding game sessions by player id: {}", userId);
        return gameSessionRepository.findSessionsByPlayer(userId);
    }

    /**
     * Adds a player to a game session if there is available space.
     *
     * @param sessionId the ID of the session
     * @param userId the ID of the player
     * @return true if the player was successfully added, false otherwise
     */
    @Override
    @Transactional
    public boolean addPlayerToSession(Long sessionId, Long userId) {
        log.debug("Adding player {} to session {}", userId, sessionId);
        Optional<GameSession> optGameSession = gameSessionRepository.findById(sessionId);
        Optional<User> optUser = userRepository.findById(userId);
        if (optGameSession.isPresent() && optUser.isPresent()) {
            GameSession gameSession = optGameSession.get();
            User user = optUser.get();
            boolean alreadyRegistered = gameSessionPlayerRepository.existsById(
                    new GameSessionPlayerId(sessionId, userId));
            if (alreadyRegistered) {
                return false;
            }
            long confirmedPlayers = gameSessionPlayerRepository.countByGameSession(gameSession);
            if (confirmedPlayers >= gameSession.getMaxPlayers()) {
                return false;
            }
            GameSessionPlayer gameSessionPlayer = GameSessionPlayer.builder()
                    .gameSession(gameSession)
                    .user(user)
                    .joinDate(LocalDate.now())
                    .confirmed(true)
                    .build();
            gameSessionPlayerRepository.save(gameSessionPlayer);
            return true;
        }
        return false;
    }

    /**
     * Removes a player from a game session.
     *
     * @param sessionId the ID of the session
     * @param userId the ID of the player
     * @return true if the player was removed successfully, false otherwise
     */
    @Override
    @Transactional
    public boolean removePlayerFromSession(Long sessionId, Long userId) {
        log.debug("Removing player {} from session {}", userId, sessionId);
        Optional<GameSessionPlayer> optGameSessionPlayer = gameSessionPlayerRepository.findById(
                new GameSessionPlayerId(sessionId, userId));
        if (optGameSessionPlayer.isPresent()) {
            gameSessionPlayerRepository.delete(optGameSessionPlayer.get());
            return true;
        }
        return false;
    }

    /**
     * Confirms a player's participation in a session.
     * In this implementation, players are auto-confirmed.
     *
     * @param sessionId the ID of the session
     * @param userId the ID of the player
     * @return always returns true (auto-confirmation)
     */
    @Override
    @Transactional
    public boolean confirmPlayerInSession(Long sessionId, Long userId) {
        log.debug("Confirming player {} in session {} (auto-confirmed)", userId, sessionId);
        return true;
    }

    /**
     * Retrieves all multi-day game sessions (sessions spanning more than one day).
     *
     * @return a list of multi-day sessions
     */
    @Override
    @Transactional(readOnly = true)
    public List<GameSession> findMultiDaySessions() {
        log.debug("Finding multi-day game sessions");
        return gameSessionRepository.findMultiDaySessions();
    }

    /**
     * Retrieves all single-day game sessions (sessions starting and ending on the same day).
     *
     * @return a list of single-day sessions
     */
    @Override
    @Transactional(readOnly = true)
    public List<GameSession> findSingleDaySessions() {
        log.debug("Finding single-day game sessions");
        return gameSessionRepository.findSingleDaySessions();
    }

    /**
     * Retrieves game sessions by custom game name (case-insensitive partial match).
     *
     * @param gameName the custom game name to search for
     * @return a list of sessions with matching custom game names
     */
    @Override
    @Transactional(readOnly = true)
    public List<GameSession> findByCustomGameName(String gameName) {
        log.debug("Finding game sessions by custom game name: {}", gameName);
        return gameSessionRepository.findByCustomGameNameContainingIgnoreCase(gameName);
    }

    /**
     * Retrieves all sessions that start after the specified date.
     *
     * @param date the cutoff date
     * @return a list of sessions starting after the given date
     */
    @Override
    @Transactional(readOnly = true)
    public List<GameSession> findSessionsStartingAfter(LocalDate date) {
        log.debug("Finding game sessions starting after: {}", date);
        return gameSessionRepository.findByStartDateAfterAndStatus(date, GameSessionStatus.SCHEDULED);
    }

    /**
     * Retrieves all sessions that ended before the specified date.
     *
     * @param date the cutoff date
     * @return a list of sessions ending before the given date
     */
    @Override
    @Transactional(readOnly = true)
    public List<GameSession> findSessionsEndingBefore(LocalDate date) {
        log.debug("Finding game sessions ending before: {}", date);
        return gameSessionRepository.findAll().stream()
                .filter(session -> session.getEndDate().isBefore(date))
                .toList();
    }

    /**
     * Retrieves all sessions active on the specified date.
     *
     * @param date the date to check
     * @return a list of sessions active on the given date
     */
    @Override
    @Transactional(readOnly = true)
    public List<GameSession> findActiveSessionsOnDate(LocalDate date) {
        log.debug("Finding game sessions active on date: {}", date);
        return gameSessionRepository.findAll().stream()
                .filter(session -> !session.getStartDate().isAfter(date) && !session.getEndDate().isBefore(date))
                .toList();
    }
}