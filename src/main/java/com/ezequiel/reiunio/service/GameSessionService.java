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
}