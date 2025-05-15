package com.ezequiel.reiunio.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ezequiel.reiunio.entity.Game;
import com.ezequiel.reiunio.enums.GameState;

public interface GameService {
    
    List<Game> findAll();
    
    Optional<Game> findById(Long id);
    
    Game save(Game game);
    
    void deleteById(Long id);
    
    List<Game> findByName(String name);
    
    List<Game> findByCategory(String category);
    
    List<Game> findByAvailable(Boolean available);
    
    List<Game> findByState(GameState state);
    
    List<Game> findByPlayerCount(int playerCount);
    
    long count();
    
    long countByAvailable(Boolean available);
    
    Map<GameState, Long> countByState();
    
    Map<String, Long> countByCategory();
    
    List<Object[]> findMostBorrowed(int limit);
    
    List<Game> findNeverBorrowed();
}