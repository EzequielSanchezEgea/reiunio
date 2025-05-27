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
    
    // Conteo simple - todos los jugadores están automáticamente confirmados
    long countByGameSession(GameSession gameSession);
    
    // Métodos mantenidos por compatibilidad, pero todos los jugadores están confirmados
    default List<GameSessionPlayer> findByGameSessionAndConfirmed(GameSession gameSession, Boolean confirmed) {
        // Como todos están confirmados, si buscan confirmed=true, devolvemos todos
        // Si buscan confirmed=false, devolvemos lista vacía
        if (confirmed == null || confirmed) {
            return findByGameSession(gameSession);
        } else {
            return List.of(); // Lista vacía
        }
    }
    
    default List<GameSessionPlayer> findByUserAndConfirmed(User user, Boolean confirmed) {
        // Como todos están confirmados, si buscan confirmed=true, devolvemos todos
        // Si buscan confirmed=false, devolvemos lista vacía
        if (confirmed == null || confirmed) {
            return findByUser(user);
        } else {
            return List.of(); // Lista vacía
        }
    }
    
    default long countByGameSessionAndConfirmed(GameSession gameSession, Boolean confirmed) {
        // Como todos están confirmados, si buscan confirmed=true, devolvemos el conteo total
        // Si buscan confirmed=false, devolvemos 0
        if (confirmed == null || confirmed) {
            return countByGameSession(gameSession);
        } else {
            return 0L;
        }
    }
}