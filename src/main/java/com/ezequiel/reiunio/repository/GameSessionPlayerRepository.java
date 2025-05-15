package com.ezequiel.reiunio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ezequiel.reiunio.entity.GameSession;
import com.ezequiel.reiunio.entity.GameSessionPlayer;
import com.ezequiel.reiunio.entity.GameSessionPlayerId;
import com.ezequiel.reiunio.entity.User;

@Repository
public interface GameSessionPlayerRepository extends JpaRepository<GameSessionPlayer, GameSessionPlayerId> {
    
    List<GameSessionPlayer> findByUser(User user);
    
    List<GameSessionPlayer> findByGameSession(GameSession gameSession);
    
    List<GameSessionPlayer> findByGameSessionAndConfirmed(GameSession gameSession, Boolean confirmed);
    
    List<GameSessionPlayer> findByUserAndConfirmed(User user, Boolean confirmed);
    
    long countByGameSessionAndConfirmed(GameSession gameSession, Boolean confirmed);
}