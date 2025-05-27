package com.ezequiel.reiunio.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ezequiel.reiunio.entity.Game;
import com.ezequiel.reiunio.entity.GameSession;
import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.GameSessionStatus;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    
    List<GameSession> findByCreator(User creator);
    
    List<GameSession> findByGame(Game game);
    
    List<GameSession> findByStatus(GameSessionStatus status);
    
    // Updated to use startDate instead of date
    List<GameSession> findByStartDateAfterAndStatus(LocalDate date, GameSessionStatus status);
    
    // Updated to use startDate and endDate for date range queries
    List<GameSession> findByStartDateBetweenOrEndDateBetween(LocalDate startDate1, LocalDate endDate1, LocalDate startDate2, LocalDate endDate2);
    
    @Query("SELECT gs FROM GameSession gs JOIN gs.players gsp WHERE gsp.user.id = :userId")
    List<GameSession> findSessionsByPlayer(Long userId);
    
    // Updated to check both start and end dates for today's sessions
    @Query("SELECT gs FROM GameSession gs WHERE gs.startDate <= CURRENT_DATE AND gs.endDate >= CURRENT_DATE")
    List<GameSession> findTodaySessions();
    
    // Find sessions by custom game name (for personal games)
    List<GameSession> findByCustomGameNameContainingIgnoreCase(String gameName);
    
    // Find upcoming sessions (starting today or later)
    @Query("SELECT gs FROM GameSession gs WHERE gs.startDate >= CURRENT_DATE AND gs.status = 'SCHEDULED' ORDER BY gs.startDate, gs.startTime")
    List<GameSession> findUpcomingSessions();
    
    // Find sessions by date range (useful for calendar views)
    @Query("SELECT gs FROM GameSession gs WHERE " +
           "(gs.startDate BETWEEN :startDate AND :endDate) OR " +
           "(gs.endDate BETWEEN :startDate AND :endDate) OR " +
           "(gs.startDate <= :startDate AND gs.endDate >= :endDate)")
    List<GameSession> findSessionsInDateRange(LocalDate startDate, LocalDate endDate);
    
    // Find multi-day sessions
    @Query("SELECT gs FROM GameSession gs WHERE gs.startDate <> gs.endDate")
    List<GameSession> findMultiDaySessions();
    
    // Find single-day sessions
    @Query("SELECT gs FROM GameSession gs WHERE gs.startDate = gs.endDate")
    List<GameSession> findSingleDaySessions();
}