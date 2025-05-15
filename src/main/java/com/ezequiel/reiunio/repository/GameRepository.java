package com.ezequiel.reiunio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ezequiel.reiunio.entity.Game;
import com.ezequiel.reiunio.enums.GameState;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    
    List<Game> findByNameContainingIgnoreCase(String name);
    
    List<Game> findByCategoryContainingIgnoreCase(String category);
    
    List<Game> findByAvailable(Boolean available);
    
    List<Game> findByState(GameState state);
    
    List<Game> findByMinPlayersLessThanEqualAndMaxPlayersGreaterThanEqual(int playerCount, int playerCount2);
    
    long countByAvailable(Boolean available);
    
    long countByState(GameState state);
    
    @Query("SELECT g, COUNT(l) FROM Game g LEFT JOIN g.loans l GROUP BY g.id ORDER BY COUNT(l) DESC, g.name ASC")
    List<Object[]> findMostBorrowedGames(@Param("limit") int limit);
    
    @Query("SELECT g FROM Game g WHERE g.id NOT IN (SELECT DISTINCT l.game.id FROM Loan l)")
    List<Game> findGamesNeverBorrowed();
}