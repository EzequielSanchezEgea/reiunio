package com.ezequiel.reiunio.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ezequiel.reiunio.entity.Game;
import com.ezequiel.reiunio.enums.GameState;

/**
 * Service interface for managing Game entities.
 */
public interface GameService {

    /**
     * Retrieves all games.
     * 
     * @return a list of all games
     */
    List<Game> findAll();

    /**
     * Retrieves a game by its ID.
     * 
     * @param id the ID of the game
     * @return an Optional containing the game if found, otherwise empty
     */
    Optional<Game> findById(Long id);

    /**
     * Saves or updates a game.
     * 
     * @param game the game to save
     * @return the saved game
     */
    Game save(Game game);

    /**
     * Deletes a game by its ID.
     * 
     * @param id the ID of the game to delete
     */
    void deleteById(Long id);

    /**
     * Retrieves games matching the given name.
     * 
     * @param name the game name to search for
     * @return a list of matching games
     */
    List<Game> findByName(String name);

    /**
     * Retrieves games belonging to the given category.
     * 
     * @param category the category name
     * @return a list of games in the category
     */
    List<Game> findByCategory(String category);

    /**
     * Retrieves games by their availability status.
     * 
     * @param available true for available games, false otherwise
     * @return a list of games with the given availability
     */
    List<Game> findByAvailable(Boolean available);

    /**
     * Retrieves games by their current state.
     * 
     * @param state the game state
     * @return a list of games with the specified state
     */
    List<Game> findByState(GameState state);

    /**
     * Retrieves games that support the given number of players.
     * 
     * @param playerCount the number of players
     * @return a list of matching games
     */
    List<Game> findByPlayerCount(int playerCount);

    /**
     * Returns the total number of games.
     * 
     * @return the count of all games
     */
    long count();

    /**
     * Returns the number of games by availability.
     * 
     * @param available true for available, false for unavailable
     * @return the count of games by availability
     */
    long countByAvailable(Boolean available);

    /**
     * Returns the number of games grouped by their state.
     * 
     * @return a map of game states and their respective counts
     */
    Map<GameState, Long> countByState();

    /**
     * Returns the number of games grouped by category.
     * 
     * @return a map of category names and their respective counts
     */
    Map<String, Long> countByCategory();

    /**
     * Retrieves the most borrowed games.
     * 
     * @param limit the maximum number of results to return
     * @return a list of objects containing game info and borrow count
     */
    List<Object[]> findMostBorrowed(int limit);

    /**
     * Retrieves games that have never been borrowed.
     * 
     * @return a list of games that were never borrowed
     */
    List<Game> findNeverBorrowed();

    /**
     * Retrieves all games with pagination.
     * 
     * @param pageable the pagination and sorting information
     * @return a page of games
     */
    Page<Game> findAll(Pageable pageable);

    /**
     * Retrieves games filtered by multiple criteria with pagination.
     * 
     * @param name the name to filter by
     * @param category the category to filter by
     * @param available the availability status
     * @param state the game state
     * @param minPlayers the minimum number of players
     * @param maxPlayers the maximum number of players
     * @param minDuration the minimum game duration
     * @param maxDuration the maximum game duration
     * @param pageable pagination and sorting information
     * @return a page of filtered games
     */
    Page<Game> findAllWithFilters(String name, String category, Boolean available, 
                                  GameState state, Integer minPlayers, Integer maxPlayers,
                                  Integer minDuration, Integer maxDuration, Pageable pageable);

    /**
     * Retrieves a list of distinct game categories.
     * 
     * @return a list of unique category names
     */
    List<String> findDistinctCategories();

    /**
     * Searches for games using various optional filters.
     * 
     * @param searchTerm text to search for in game fields
     * @param category category filter
     * @param available availability filter
     * @param state game state filter
     * @param playerCount number of players filter
     * @param duration game duration filter
     * @return a list of matching games
     */
    List<Game> searchGames(String searchTerm, String category, Boolean available, 
                           GameState state, Integer playerCount, Integer duration);
}
