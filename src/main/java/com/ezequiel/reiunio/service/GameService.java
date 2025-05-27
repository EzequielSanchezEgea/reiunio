package com.ezequiel.reiunio.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    
    // NEW PAGINATION AND ADVANCED SEARCH METHODS
    
    /**
     * Find all games with pagination
     */
    Page<Game> findAll(Pageable pageable);
    
    /**
     * Find games with multiple filters and pagination
     */
    Page<Game> findAllWithFilters(String name, String category, Boolean available, 
                                 GameState state, Integer minPlayers, Integer maxPlayers,
                                 Integer minDuration, Integer maxDuration, Pageable pageable);
    
    /**
     * Get distinct categories from all games
     */
    List<String> findDistinctCategories();
    
    /**
     * Advanced search with multiple criteria
     */
    List<Game> searchGames(String searchTerm, String category, Boolean available, 
                          GameState state, Integer playerCount, Integer duration);
}