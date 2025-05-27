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

@Service
@RequiredArgsConstructor
@Slf4j
public class GameSessionServiceImpl implements GameSessionService {

    private final GameSessionRepository gameSessionRepository;
    private final UserRepository userRepository;
    private final GameSessionPlayerRepository gameSessionPlayerRepository;
    private final GameRepository gameRepository;

    @Override
    @Transactional
    public List<GameSession> findAll() {
        log.debug("Finding all game sessions");
        autoFinishExpiredSessions();
        return gameSessionRepository.findAll();
    }

    @Override
    @Transactional
    public Optional<GameSession> findById(Long id) {
        log.debug("Finding game session by id: {}", id);
        autoFinishExpiredSessions();
        return gameSessionRepository.findById(id);
    }

    @Override
    @Transactional
    public List<GameSession> findByStatus(GameSessionStatus status) {
        log.debug("Finding game sessions by status: {}", status);
        autoFinishExpiredSessions();
        return gameSessionRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public List<GameSession> findUpcomingSessions() {
        log.debug("Finding upcoming game sessions");
        autoFinishExpiredSessions();
        return gameSessionRepository.findUpcomingSessions();
    }

    @Override
    @Transactional
    public List<GameSession> findTodaySessions() {
        log.debug("Finding today's game sessions");
        autoFinishExpiredSessions();
        return gameSessionRepository.findTodaySessions();
    }

    /**
     * Auto-finaliza sesiones que ya deberían haber terminado
     * Se ejecuta automáticamente en las consultas principales
     */
    private void autoFinishExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();
        
        // Buscar sesiones programadas que deberían haber terminado
        List<GameSession> scheduledSessions = gameSessionRepository.findByStatus(GameSessionStatus.SCHEDULED);
        
        for (GameSession session : scheduledSessions) {
            if (hasSessionExpired(session, today, currentTime)) {
                try {
                    session.setStatus(GameSessionStatus.FINISHED);
                    gameSessionRepository.save(session);
                    log.info("Sesión '{}' finalizada automáticamente por expiración", session.getTitle());
                } catch (Exception e) {
                    log.error("Error auto-finalizando sesión {}: {}", session.getId(), e.getMessage());
                }
            }
        }
    }

    /**
     * Determina si una sesión ha expirado
     */
    private boolean hasSessionExpired(GameSession session, LocalDate today, LocalTime currentTime) {
        LocalDate endDate = session.getEndDate();
        LocalTime endTime = session.getEndTime();
        
        // Si la fecha de fin es anterior a hoy
        if (endDate.isBefore(today)) {
            return true;
        }
        
        // Si es hoy y hay hora de fin específica
        if (endDate.equals(today) && endTime != null) {
            return currentTime.isAfter(endTime);
        }
        
        // Si es hoy pero no tiene hora de fin, considerar que termina al final del día
        if (endDate.equals(today) && endTime == null) {
            return currentTime.isAfter(LocalTime.of(23, 59));
        }
        
        return false;
    }

    @Override
    @Transactional
    public GameSession save(GameSession gameSession) {
        boolean isNewSession = gameSession.getId() == null;
        
        if (isNewSession) {
            // Nueva sesión - ya no modificamos la disponibilidad del juego
            gameSession.setStatus(GameSessionStatus.SCHEDULED);
            log.info("Nueva sesión creada: '{}' para juego: '{}'", 
                    gameSession.getTitle(), gameSession.getCustomGameName());
            
            if (gameSession.getGame() != null) {
                log.info("Sesión usa juego de biblioteca '{}' pero NO se modifica su disponibilidad", 
                        gameSession.getGame().getName());
            }
        }
        
        log.debug("Saving game session: {}", gameSession.getTitle());
        return gameSessionRepository.save(gameSession);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.debug("Deleting game session by id: {}", id);
        
        // Ya no necesitamos liberar el juego al eliminar la sesión
        Optional<GameSession> sessionOpt = gameSessionRepository.findById(id);
        if (sessionOpt.isPresent()) {
            GameSession session = sessionOpt.get();
            if (session.getGame() != null) {
                log.info("Eliminando sesión que usaba juego de biblioteca '{}' - NO se modifica disponibilidad", 
                        session.getGame().getName());
            }
        }
        
        gameSessionRepository.deleteById(id);
    }

    /**
     * Cambia el estado de una sesión a FINISHED (ya no modifica disponibilidad de juegos)
     */
    @Transactional
    public GameSession finishSession(Long sessionId) {
        Optional<GameSession> sessionOpt = findById(sessionId);
        if (sessionOpt.isPresent()) {
            GameSession session = sessionOpt.get();
            session.setStatus(GameSessionStatus.FINISHED);
            
            session = gameSessionRepository.save(session);
            log.info("Sesión '{}' marcada como FINISHED", session.getTitle());
            return session;
        }
        throw new IllegalArgumentException("Session not found with ID: " + sessionId);
    }

    // Resto de métodos sin cambios...

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
    public List<GameSession> findSessionsBetweenDates(LocalDate startDate, LocalDate endDate) {
        log.debug("Finding game sessions between dates: {} and {}", startDate, endDate);
        return gameSessionRepository.findSessionsInDateRange(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSession> findSessionsByPlayer(Long userId) {
        log.debug("Finding game sessions by player id: {}", userId);
        return gameSessionRepository.findSessionsByPlayer(userId);
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
            long confirmedPlayers = gameSessionPlayerRepository.countByGameSession(gameSession);
            if (confirmedPlayers >= gameSession.getMaxPlayers()) {
                return false;
            }
            
            // Add the player with automatic confirmation
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
        log.debug("Confirming player {} in session {} (auto-confirmed)", userId, sessionId);
        return true; // Los jugadores se confirman automáticamente
    }

    // Métodos adicionales

    @Override
    @Transactional(readOnly = true)
    public List<GameSession> findMultiDaySessions() {
        log.debug("Finding multi-day game sessions");
        return gameSessionRepository.findMultiDaySessions();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSession> findSingleDaySessions() {
        log.debug("Finding single-day game sessions");
        return gameSessionRepository.findSingleDaySessions();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSession> findByCustomGameName(String gameName) {
        log.debug("Finding game sessions by custom game name: {}", gameName);
        return gameSessionRepository.findByCustomGameNameContainingIgnoreCase(gameName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSession> findSessionsStartingAfter(LocalDate date) {
        log.debug("Finding game sessions starting after: {}", date);
        return gameSessionRepository.findByStartDateAfterAndStatus(date, GameSessionStatus.SCHEDULED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSession> findSessionsEndingBefore(LocalDate date) {
        log.debug("Finding game sessions ending before: {}", date);
        return gameSessionRepository.findAll().stream()
                .filter(session -> session.getEndDate().isBefore(date))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSession> findActiveSessionsOnDate(LocalDate date) {
        log.debug("Finding game sessions active on date: {}", date);
        return gameSessionRepository.findAll().stream()
                .filter(session -> !session.getStartDate().isAfter(date) && !session.getEndDate().isBefore(date))
                .toList();
    }
}