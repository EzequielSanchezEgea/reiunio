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
    
    List<GameSession> findByDateAfterAndStatus(LocalDate date, GameSessionStatus status);
    
    List<GameSession> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT gs FROM GameSession gs JOIN gs.players gsp WHERE gsp.user.id = :userId")
    List<GameSession> findSessionsByPlayer(Long userId);
    
    @Query("SELECT gs FROM GameSession gs WHERE gs.date = CURRENT_DATE")
    List<GameSession> findTodaySessions();
}