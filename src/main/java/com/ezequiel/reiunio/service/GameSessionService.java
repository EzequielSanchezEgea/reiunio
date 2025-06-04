package com.ezequiel.reiunio.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.ezequiel.reiunio.entity.Game;
import com.ezequiel.reiunio.entity.GameSession;
import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.GameSessionStatus;

/**
 * Service interface for managing game sessions.
 */
public interface GameSessionService {

    /**
     * Retrieves all game sessions.
     * 
     * @return a list of all game sessions
     */
    List<GameSession> findAll();

    /**
     * Retrieves a game session by its ID.
     * 
     * @param id the ID of the session
     * @return an Optional containing the session if found
     */
    Optional<GameSession> findById(Long id);

    /**
     * Saves or updates a game session.
     * 
     * @param gameSession the session to save
     * @return the saved session
     */
    GameSession save(GameSession gameSession);

    /**
     * Deletes a game session by its ID.
     * 
     * @param id the ID of the session to delete
     */
    void deleteById(Long id);

    /**
     * Retrieves sessions created by a specific user.
     * 
     * @param creator the user who created the session
     * @return a list of sessions created by the user
     */
    List<GameSession> findByCreator(User creator);

    /**
     * Retrieves sessions associated with a specific game.
     * 
     * @param game the game entity
     * @return a list of sessions using the given game
     */
    List<GameSession> findByGame(Game game);

    /**
     * Retrieves sessions by their status.
     * 
     * @param status the session status
     * @return a list of sessions with the given status
     */
    List<GameSession> findByStatus(GameSessionStatus status);

    /**
     * Retrieves upcoming sessions (starting today or later).
     * 
     * @return a list of upcoming sessions
     */
    List<GameSession> findUpcomingSessions();

    /**
     * Retrieves sessions that take place between two dates.
     * 
     * @param startDate the start of the range
     * @param endDate the end of the range
     * @return a list of sessions within the date range
     */
    List<GameSession> findSessionsBetweenDates(LocalDate startDate, LocalDate endDate);

    /**
     * Retrieves sessions that a specific user is participating in.
     * 
     * @param userId the ID of the user
     * @return a list of sessions involving the user
     */
    List<GameSession> findSessionsByPlayer(Long userId);

    /**
     * Retrieves sessions that are scheduled for today.
     * 
     * @return a list of today’s sessions
     */
    List<GameSession> findTodaySessions();

    /**
     * Adds a player to a session.
     * 
     * @param sessionId the session ID
     * @param userId the user ID to add
     * @return true if the player was added successfully
     */
    boolean addPlayerToSession(Long sessionId, Long userId);

    /**
     * Removes a player from a session.
     * 
     * @param sessionId the session ID
     * @param userId the user ID to remove
     * @return true if the player was removed successfully
     */
    boolean removePlayerFromSession(Long sessionId, Long userId);

    /**
     * Confirms a player's participation in a session.
     * 
     * @param sessionId the session ID
     * @param userId the user ID to confirm
     * @return true if the confirmation was successful
     */
    boolean confirmPlayerInSession(Long sessionId, Long userId);

    /**
     * Retrieves sessions that span multiple days.
     * 
     * @return a list of multi-day sessions
     */
    List<GameSession> findMultiDaySessions();

    /**
     * Retrieves sessions that occur on a single day.
     * 
     * @return a list of single-day sessions
     */
    List<GameSession> findSingleDaySessions();

    /**
     * Retrieves sessions by a custom game name (e.g., for user-created games).
     * 
     * @param gameName the custom game name
     * @return a list of matching sessions
     */
    List<GameSession> findByCustomGameName(String gameName);

    /**
     * Retrieves sessions that start after a specific date.
     * 
     * @param date the start date threshold
     * @return a list of sessions starting after the date
     */
    List<GameSession> findSessionsStartingAfter(LocalDate date);

    /**
     * Retrieves sessions that end before a specific date.
     * 
     * @param date the end date threshold
     * @return a list of sessions ending before the date
     */
    List<GameSession> findSessionsEndingBefore(LocalDate date);

    /**
     * Retrieves sessions that are active (ongoing) on a specific date.
     * 
     * @param date the date to check
     * @return a list of sessions active on that date
     */
    List<GameSession> findActiveSessionsOnDate(LocalDate date);
}
