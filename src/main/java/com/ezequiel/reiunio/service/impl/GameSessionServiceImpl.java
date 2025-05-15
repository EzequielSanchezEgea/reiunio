package com.ezequiel.reiunio.service.impl;

import java.time.LocalDate;
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
import com.ezequiel.reiunio.repository.GameSessionPlayerRepository;
import com.ezequiel.reiunio.repository.GameSessionRepository;
import com.ezequiel.reiunio.repository.UserRepository;
import com.ezequiel.reiunio.service.GameSessionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameSessionServiceImpl implements GameSessionService {

    private final GameSessionRepository gameSessionRepository;
    private final UserRepository userRepository;
    private final GameSessionPlayerRepository gameSessionPlayerRepository;

    @Override
    @Transactional(readOnly = true)
    public List<GameSession> findAll() {
        log.debug("Finding all game sessions");
        return gameSessionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GameSession> findById(Long id) {
        log.debug("Finding game session by id: {}", id);
        return gameSessionRepository.findById(id);
    }

    @Override
    @Transactional
    public GameSession save(GameSession gameSession) {
        // If it's a new game session, ensure status is SCHEDULED
        if (gameSession.getId() == null) {
            gameSession.setStatus(GameSessionStatus.SCHEDULED);
        }
        log.debug("Saving game session: {}", gameSession.getTitle());
        return gameSessionRepository.save(gameSession);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.debug("Deleting game session by id: {}", id);
        gameSessionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSession> findByCreator(User creator) {
        log.debug("Finding game sessions by creator: {}", creator.getUsername());
        return gameSessionRepository.findByCreator(creator);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSession> findByGame(Game game) {
        log.debug("Finding game sessions by game: {}", game.getName());
        return gameSessionRepository.findByGame(game);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSession> findByStatus(GameSessionStatus status) {
        log.debug("Finding game sessions by status: {}", status);
        return gameSessionRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSession> findUpcomingSessions() {
        log.debug("Finding upcoming game sessions");
        return gameSessionRepository.findByDateAfterAndStatus(LocalDate.now(), GameSessionStatus.SCHEDULED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSession> findSessionsBetweenDates(LocalDate startDate, LocalDate endDate) {
        log.debug("Finding game sessions between dates: {} and {}", startDate, endDate);
        return gameSessionRepository.findByDateBetween(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSession> findSessionsByPlayer(Long userId) {
        log.debug("Finding game sessions by player id: {}", userId);
        return gameSessionRepository.findSessionsByPlayer(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSession> findTodaySessions() {
        log.debug("Finding today's game sessions");
        return gameSessionRepository.findTodaySessions();
    }

    @Override
    @Transactional
    public boolean addPlayerToSession(Long sessionId, Long userId) {
        log.debug("Adding player {} to session {}", userId, sessionId);
        Optional<GameSession> optGameSession = gameSessionRepository.findById(sessionId);
        Optional<User> optUser = userRepository.findById(userId);
        
        if (optGameSession.isPresent() && optUser.isPresent()) {
            GameSession gameSession = optGameSession.get();
            User user = optUser.get();
            
            // Check if player is already in the session
            boolean alreadyRegistered = gameSessionPlayerRepository.existsById(
                    new GameSessionPlayerId(sessionId, userId));
            if (alreadyRegistered) {
                return false;
            }
            
            // Check if there's available space
            long confirmedPlayers = gameSessionPlayerRepository.countByGameSessionAndConfirmed(gameSession, true);
            if (confirmedPlayers >= gameSession.getMaxPlayers()) {
                return false;
            }
            
            // Add the player
            GameSessionPlayer gameSessionPlayer = GameSessionPlayer.builder()
                    .gameSession(gameSession)
                    .user(user)
                    .joinDate(LocalDate.now())
                    .confirmed(false)
                    .build();
            
            gameSessionPlayerRepository.save(gameSessionPlayer);
            return true;
        }
        
        return false;
    }

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

    @Override
    @Transactional
    public boolean confirmPlayerInSession(Long sessionId, Long userId) {
        log.debug("Confirming player {} in session {}", userId, sessionId);
        Optional<GameSessionPlayer> optGameSessionPlayer = gameSessionPlayerRepository.findById(
                new GameSessionPlayerId(sessionId, userId));
        
        if (optGameSessionPlayer.isPresent()) {
            GameSessionPlayer gameSessionPlayer = optGameSessionPlayer.get();
            
            // Check if there's available space for confirmation
            GameSession gameSession = gameSessionPlayer.getGameSession();
            long confirmedPlayers = gameSessionPlayerRepository.countByGameSessionAndConfirmed(gameSession, true);
            
            if (confirmedPlayers >= gameSession.getMaxPlayers()) {
                return false;
            }
            
            gameSessionPlayer.setConfirmed(true);
            gameSessionPlayerRepository.save(gameSessionPlayer);
            return true;
        }
        
        return false;
    }
}