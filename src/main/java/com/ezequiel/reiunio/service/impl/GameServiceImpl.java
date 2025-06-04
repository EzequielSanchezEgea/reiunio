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

/**
 * Implementation of the {@link GameService} interface.
 *
 * <p>This service provides business logic for managing game entities in the application.
 * It supports operations such as retrieving, saving, filtering, and counting games,
 * with additional support for advanced search and analytics like most borrowed games.
 *
 * <p>Key features:
 * <ul>
 *   <li>Basic CRUD operations</li>
 *   <li>Filtering by name, category, availability, state, player count, and duration</li>
 *   <li>Pagination and dynamic query building</li>
 *   <li>Game statistics including borrowing history</li>
 * </ul>
 *
 * @see Game
 * @see GameRepository
 * @see GameService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final LoanRepository loanRepository;

    /**
     * Retrieves all available games from the repository.
     *
     * @return a list of all games
     */
    @Override
    @Transactional(readOnly = true)
    public List<Game> findAll() {
        log.debug("Finding all games");
        return gameRepository.findAll();
    }

    /**
     * Retrieves a game by its unique identifier.
     *
     * @param id the ID of the game to retrieve
     * @return an Optional containing the game if found, or empty otherwise
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Game> findById(Long id) {
        log.debug("Finding game by id: {}", id);
        return gameRepository.findById(id);
    }

    /**
     * Saves a new or updates an existing game in the repository.
     *
     * @param game the game entity to save or update
     * @return the saved game entity
     */
    @Override
    @Transactional
    public Game save(Game game) {
        log.debug("Saving game: {}", game.getName());
        return gameRepository.save(game);
    }

    /**
     * Deletes a game by its unique identifier.
     *
     * @param id the ID of the game to delete
     */
    @Override
    @Transactional
    public void deleteById(Long id) {
        log.debug("Deleting game by id: {}", id);
        gameRepository.deleteById(id);
    }

    /**
     * Retrieves games whose names contain the given substring (case-insensitive).
     *
     * @param name the partial name to search for
     * @return a list of matching games
     */
    @Override
    @Transactional(readOnly = true)
    public List<Game> findByName(String name) {
        log.debug("Finding games by name containing: {}", name);
        return gameRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Retrieves games whose categories contain the given substring (case-insensitive).
     *
     * @param category the partial category to search for
     * @return a list of matching games
     */
    @Override
    @Transactional(readOnly = true)
    public List<Game> findByCategory(String category) {
        log.debug("Finding games by category containing: {}", category);
        return gameRepository.findByCategoryContainingIgnoreCase(category);
    }

    /**
     * Retrieves games based on their availability status.
     *
     * @param available true to find available games, false for unavailable ones
     * @return a list of games with the specified availability
     */
    @Override
    @Transactional(readOnly = true)
    public List<Game> findByAvailable(Boolean available) {
        log.debug("Finding games by availability: {}", available);
        return gameRepository.findByAvailable(available);
    }

    /**
     * Retrieves games that match the specified game state.
     *
     * @param state the game state to filter by
     * @return a list of games with the given state
     */
    @Override
    @Transactional(readOnly = true)
    public List<Game> findByState(GameState state) {
        log.debug("Finding games by state: {}", state);
        return gameRepository.findByState(state);
    }

    /**
     * Retrieves games that can accommodate the specified number of players.
     *
     * @param playerCount the number of players to support
     * @return a list of games suitable for the given player count
     */
    @Override
    @Transactional(readOnly = true)
    public List<Game> findByPlayerCount(int playerCount) {
        log.debug("Finding games for {} players", playerCount);
        return gameRepository.findByMinPlayersLessThanEqualAndMaxPlayersGreaterThanEqual(
                playerCount, playerCount);
    }

    /**
     * Returns the total number of games in the system.
     *
     * @return the total game count
     */
    @Override
    @Transactional(readOnly = true)
    public long count() {
        log.debug("Counting all games");
        return gameRepository.count();
    }

    /**
     * Returns the number of games with the specified availability status.
     *
     * @param available true for available games, false for unavailable
     * @return the count of games with the given availability
     */
    @Override
    @Transactional(readOnly = true)
    public long countByAvailable(Boolean available) {
        log.debug("Counting games by availability: {}", available);
        return gameRepository.countByAvailable(available);
    }

    /**
     * Returns the number of games grouped by their current state.
     *
     * @return a map where keys are game states and values are counts
     */
    @Override
    @Transactional(readOnly = true)
    public Map<GameState, Long> countByState() {
        log.debug("Counting games by state");
        Map<GameState, Long> result = new HashMap<>();
        for (GameState state : GameState.values()) {
            result.put(state, gameRepository.countByState(state));
        }
        return result;
    }

    /**
     * Returns the number of games grouped by their category.
     *
     * @return a map where keys are categories and values are counts
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> countByCategory() {
        log.debug("Counting games by category");
        return gameRepository.findAll().stream()
                .filter(game -> game.getCategory() != null && !game.getCategory().isEmpty())
                .collect(Collectors.groupingBy(Game::getCategory, Collectors.counting()));
    }

    /**
     * Retrieves a list of the most borrowed games, limited by the specified number.
     *
     * @param limit the maximum number of results to return
     * @return a list of arrays containing game data and borrow counts
     */
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> findMostBorrowed(int limit) {
        log.debug("Finding {} most borrowed games", limit);
        return gameRepository.findMostBorrowedGames(limit);
    }

    /**
     * Retrieves games that have never been borrowed.
     *
     * @return a list of games that have never been borrowed
     */
    @Override
    @Transactional(readOnly = true)
    public List<Game> findNeverBorrowed() {
        log.debug("Finding games never borrowed");
        return gameRepository.findGamesNeverBorrowed();
    }

    /**
     * Retrieves all games using pagination.
     *
     * @param pageable the pagination information
     * @return a page of game entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Game> findAll(Pageable pageable) {
        log.debug("Finding all games with pagination: page {}, size {}", 
                 pageable.getPageNumber(), pageable.getPageSize());
        return gameRepository.findAll(pageable);
    }

    /**
     * Retrieves games filtered by multiple criteria using pagination.
     *
     * @param name the name or part of the name to search for
     * @param category the category or part of the category to search for
     * @param available the availability status to filter by
     * @param state the game state to filter by
     * @param minPlayers the minimum number of supported players
     * @param maxPlayers the maximum number of supported players
     * @param minDuration the minimum game duration in minutes
     * @param maxDuration the maximum game duration in minutes
     * @param pageable the pagination information
     * @return a page of games matching the specified filters
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Game> findAllWithFilters(String name, String category, Boolean available, 
                                        GameState state, Integer minPlayers, Integer maxPlayers,
                                        Integer minDuration, Integer maxDuration, Pageable pageable) {
        log.debug("Finding games with filters - name: {}, category: {}, available: {}, state: {}, " +
                 "minPlayers: {}, maxPlayers: {}, minDuration: {}, maxDuration: {}, page: {}, size: {}", 
                 name, category, available, state, minPlayers, maxPlayers, 
                 minDuration, maxDuration, pageable.getPageNumber(), pageable.getPageSize());
        
        String nameFilter = normalizeStringParam(name);
        String categoryFilter = normalizeStringParam(category);

        log.debug("Normalized filters - name: '{}', category: '{}'", nameFilter, categoryFilter);
        
        return gameRepository.findGamesWithFilters(
            nameFilter, categoryFilter, available, state, 
            minPlayers, maxPlayers, minDuration, maxDuration, pageable);
    }

    /**
     * Retrieves a list of distinct game categories used in the system.
     *
     * @return a list of unique game categories
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> findDistinctCategories() {
        log.debug("Finding distinct categories");
        return gameRepository.findDistinctCategories();
    }

    /**
     * Performs an advanced search for games using various parameters.
     *
     * @param searchTerm the term to search in game names or descriptions
     * @param category the category to filter by
     * @param available the availability status to filter by
     * @param state the game state to filter by
     * @param playerCount the number of players to support
     * @param duration the approximate duration of the game in minutes
     * @return a list of games matching the search criteria
     */
    @Override
    @Transactional(readOnly = true)
    public List<Game> searchGames(String searchTerm, String category, Boolean available, 
                                 GameState state, Integer playerCount, Integer duration) {
        log.debug("Searching games with criteria - searchTerm: {}, category: {}, available: {}, " +
                 "state: {}, playerCount: {}, duration: {}", 
                 searchTerm, category, available, state, playerCount, duration);
        
        String searchFilter = (searchTerm != null && searchTerm.trim().isEmpty()) ? null : searchTerm;
        String categoryFilter = (category != null && category.trim().isEmpty()) ? null : category;
        
        return gameRepository.searchGames(searchFilter, categoryFilter, available, 
                                         state, playerCount, duration);
    }

    /**
     * Normalizes a string parameter by trimming whitespace and converting empty strings to null.
     *
     * @param param the string to normalize
     * @return the trimmed string or null if it was empty or null
     */
    private String normalizeStringParam(String param) {
        if (param == null || param.trim().isEmpty()) {
            return null;
        }
        return param.trim();
    }
}