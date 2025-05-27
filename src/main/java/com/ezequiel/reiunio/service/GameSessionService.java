package com.ezequiel.reiunio.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.ezequiel.reiunio.entity.Game;
import com.ezequiel.reiunio.entity.GameSession;
import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.GameSessionStatus;

public interface GameSessionService {
    
    List<GameSession> findAll();
    
    Optional<GameSession> findById(Long id);
    
    GameSession save(GameSession gameSession);
    
    void deleteById(Long id);
    
    List<GameSession> findByCreator(User creator);
    
    List<GameSession> findByGame(Game game);
    
    List<GameSession> findByStatus(GameSessionStatus status);
    
    List<GameSession> findUpcomingSessions();
    
    List<GameSession> findSessionsBetweenDates(LocalDate startDate, LocalDate endDate);
    
    List<GameSession> findSessionsByPlayer(Long userId);
    
    List<GameSession> findTodaySessions();
    
    boolean addPlayerToSession(Long sessionId, Long userId);
    
    boolean removePlayerFromSession(Long sessionId, Long userId);
    
    boolean confirmPlayerInSession(Long sessionId, Long userId);
    
    // New methods for enhanced functionality
    
    /**
     * Find sessions that span multiple days
     */
    List<GameSession> findMultiDaySessions();
    
    /**
     * Find sessions that occur on a single day
     */
    List<GameSession> findSingleDaySessions();
    
    /**
     * Find sessions by custom game name (for personal games)
     */
    List<GameSession> findByCustomGameName(String gameName);
    
    /**
     * Find sessions starting after a specific date
     */
    List<GameSession> findSessionsStartingAfter(LocalDate date);
    
    /**
     * Find sessions ending before a specific date
     */
    List<GameSession> findSessionsEndingBefore(LocalDate date);
    
    /**
     * Find sessions that are active (ongoing) on a specific date
     */
    List<GameSession> findActiveSessionsOnDate(LocalDate date);
}