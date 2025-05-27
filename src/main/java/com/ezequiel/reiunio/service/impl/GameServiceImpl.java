package com.ezequiel.reiunio.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ezequiel.reiunio.entity.Game;
import com.ezequiel.reiunio.enums.GameState;
import com.ezequiel.reiunio.repository.GameRepository;
import com.ezequiel.reiunio.repository.LoanRepository;
import com.ezequiel.reiunio.service.GameService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final LoanRepository loanRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Game> findAll() {
        log.debug("Finding all games");
        return gameRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Game> findById(Long id) {
        log.debug("Finding game by id: {}", id);
        return gameRepository.findById(id);
    }

    @Override
    @Transactional
    public Game save(Game game) {
        log.debug("Saving game: {}", game.getName());
        return gameRepository.save(game);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.debug("Deleting game by id: {}", id);
        gameRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Game> findByName(String name) {
        log.debug("Finding games by name containing: {}", name);
        return gameRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Game> findByCategory(String category) {
        log.debug("Finding games by category containing: {}", category);
        return gameRepository.findByCategoryContainingIgnoreCase(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Game> findByAvailable(Boolean available) {
        log.debug("Finding games by availability: {}", available);
        return gameRepository.findByAvailable(available);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Game> findByState(GameState state) {
        log.debug("Finding games by state: {}", state);
        return gameRepository.findByState(state);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Game> findByPlayerCount(int playerCount) {
        log.debug("Finding games for {} players", playerCount);
        return gameRepository.findByMinPlayersLessThanEqualAndMaxPlayersGreaterThanEqual(
                playerCount, playerCount);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long count() {
        log.debug("Counting all games");
        return gameRepository.count();
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countByAvailable(Boolean available) {
        log.debug("Counting games by availability: {}", available);
        return gameRepository.countByAvailable(available);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<GameState, Long> countByState() {
        log.debug("Counting games by state");
        Map<GameState, Long> result = new HashMap<>();
        
        for (GameState state : GameState.values()) {
            long count = gameRepository.countByState(state);
            result.put(state, count);
        }
        
        return result;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> countByCategory() {
        log.debug("Counting games by category");
        List<Game> allGames = gameRepository.findAll();
        
        return allGames.stream()
                .filter(game -> game.getCategory() != null && !game.getCategory().isEmpty())
                .collect(Collectors.groupingBy(
                    Game::getCategory,
                    Collectors.counting()
                ));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> findMostBorrowed(int limit) {
        log.debug("Finding {} most borrowed games", limit);
        return gameRepository.findMostBorrowedGames(limit);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Game> findNeverBorrowed() {
        log.debug("Finding games never borrowed");
        return gameRepository.findGamesNeverBorrowed();
    }

    // NEW PAGINATION AND ADVANCED SEARCH IMPLEMENTATIONS

    @Override
    @Transactional(readOnly = true)
    public Page<Game> findAll(Pageable pageable) {
        log.debug("Finding all games with pagination: page {}, size {}", 
                 pageable.getPageNumber(), pageable.getPageSize());
        return gameRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Game> findAllWithFilters(String name, String category, Boolean available, 
                                        GameState state, Integer minPlayers, Integer maxPlayers,
                                        Integer minDuration, Integer maxDuration, Pageable pageable) {
        
        log.debug("Finding games with filters - name: {}, category: {}, available: {}, state: {}, " +
                 "minPlayers: {}, maxPlayers: {}, minDuration: {}, maxDuration: {}, page: {}, size: {}", 
                 name, category, available, state, minPlayers, maxPlayers, 
                 minDuration, maxDuration, pageable.getPageNumber(), pageable.getPageSize());
        
        // Normalize string parameters - convert empty strings to null for proper query handling
        String nameFilter = normalizeStringParam(name);
        String categoryFilter = normalizeStringParam(category);
        
        // Log the normalized parameters
        log.debug("Normalized filters - name: '{}', category: '{}'", nameFilter, categoryFilter);
        
        return gameRepository.findGamesWithFilters(
            nameFilter, categoryFilter, available, state, 
            minPlayers, maxPlayers, minDuration, maxDuration, pageable);
    }

    /**
     * Helper method to normalize string parameters for queries
     * Converts null, empty, or whitespace-only strings to null
     */
    private String normalizeStringParam(String param) {
        if (param == null || param.trim().isEmpty()) {
            return null;
        }
        return param.trim();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findDistinctCategories() {
        log.debug("Finding distinct categories");
        return gameRepository.findDistinctCategories();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Game> searchGames(String searchTerm, String category, Boolean available, 
                                 GameState state, Integer playerCount, Integer duration) {
        
        log.debug("Searching games with criteria - searchTerm: {}, category: {}, available: {}, " +
                 "state: {}, playerCount: {}, duration: {}", 
                 searchTerm, category, available, state, playerCount, duration);
        
        // Convert empty strings to null for proper query handling
        String searchFilter = (searchTerm != null && searchTerm.trim().isEmpty()) ? null : searchTerm;
        String categoryFilter = (category != null && category.trim().isEmpty()) ? null : category;
        
        return gameRepository.searchGames(searchFilter, categoryFilter, available, 
                                         state, playerCount, duration);
    }
}