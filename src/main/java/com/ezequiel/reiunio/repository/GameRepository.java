package com.ezequiel.reiunio.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ezequiel.reiunio.entity.Game;
import com.ezequiel.reiunio.enums.GameState;

/**
 * Repository interface for accessing and managing {@link Game} entities.
 */
@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    /**
     * Finds games with names containing the specified string, case-insensitive.
     *
     * @param name the substring to search in game names
     * @return a list of matching games
     */
    List<Game> findByNameContainingIgnoreCase(String name);

    /**
     * Finds games with categories containing the specified string, case-insensitive.
     *
     * @param category the substring to search in game categories
     * @return a list of matching games
     */
    List<Game> findByCategoryContainingIgnoreCase(String category);

    /**
     * Finds games based on their availability status.
     *
     * @param available the availability status
     * @return a list of matching games
     */
    List<Game> findByAvailable(Boolean available);

    /**
     * Finds games by their state.
     *
     * @param state the state of the game
     * @return a list of matching games
     */
    List<Game> findByState(GameState state);

    /**
     * Finds games that can be played with a specified number of players.
     *
     * @param playerCount the number of players
     * @param playerCount2 the number of players
     * @return a list of games matching the player count criteria
     */
    List<Game> findByMinPlayersLessThanEqualAndMaxPlayersGreaterThanEqual(int playerCount, int playerCount2);

    /**
     * Counts the number of games based on their availability status.
     *
     * @param available the availability status
     * @return the number of matching games
     */
    long countByAvailable(Boolean available);

    /**
     * Counts the number of games by their state.
     *
     * @param state the game state
     * @return the number of matching games
     */
    long countByState(GameState state);

    /**
     * Retrieves the most borrowed games ordered by loan count (descending) and name (ascending).
     *
     * @param limit the maximum number of results to return (not enforced in query)
     * @return a list of object arrays [Game, loan count]
     */
    @Query("SELECT g, COUNT(l) FROM Game g LEFT JOIN g.loans l GROUP BY g.id ORDER BY COUNT(l) DESC, g.name ASC")
    List<Object[]> findMostBorrowedGames(@Param("limit") int limit);

    /**
     * Finds games that have never been borrowed.
     *
     * @return a list of games that have never had a loan
     */
    @Query("SELECT g FROM Game g WHERE g.id NOT IN (SELECT DISTINCT l.game.id FROM Loan l)")
    List<Game> findGamesNeverBorrowed();

    /**
     * Retrieves a list of distinct non-empty categories from games.
     *
     * @return a sorted list of distinct categories
     */
    @Query("SELECT DISTINCT g.category FROM Game g WHERE g.category IS NOT NULL AND g.category != '' ORDER BY g.category")
    List<String> findDistinctCategories();

    /**
     * Performs an advanced filtered search with pagination.
     * Uses strict player count filtering where minPlayers filter requires games with minimum players
     * greater than or equal to the specified value, and maxPlayers filter requires games with maximum
     * players less than or equal to the specified value.
     *
     * @param name part of the game name to search for
     * @param category the exact category to filter by
     * @param available availability status filter
     * @param state the game state filter
     * @param minPlayers minimum number of players filter (games must require at least this many)
     * @param maxPlayers maximum number of players filter (games must not allow more than this many)
     * @param minDuration minimum game duration in minutes
     * @param maxDuration maximum game duration in minutes
     * @param pageable pagination settings
     * @return a page of games matching the specified filters
     */
    @Query("SELECT g FROM Game g WHERE " +
           "(:name IS NULL OR :name = '' OR LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:category IS NULL OR :category = '' OR LOWER(g.category) = LOWER(:category)) AND " +
           "(:available IS NULL OR g.available = :available) AND " +
           "(:state IS NULL OR g.state = :state) AND " +
           "(:minPlayers IS NULL OR g.minPlayers >= :minPlayers) AND " +
           "(:maxPlayers IS NULL OR g.maxPlayers <= :maxPlayers) AND " +
           "(:minDuration IS NULL OR g.durationMinutes >= :minDuration) AND " +
           "(:maxDuration IS NULL OR g.durationMinutes <= :maxDuration)")
    Page<Game> findGamesWithFilters(@Param("name") String name,
                                    @Param("category") String category,
                                    @Param("available") Boolean available,
                                    @Param("state") GameState state,
                                    @Param("minPlayers") Integer minPlayers,
                                    @Param("maxPlayers") Integer maxPlayers,
                                    @Param("minDuration") Integer minDuration,
                                    @Param("maxDuration") Integer maxDuration,
                                    Pageable pageable);

    /**
     * Searches games by multiple criteria without pagination.
     * Supports text search across name, description, and category fields.
     *
     * @param searchTerm text to search in name, description, or category
     * @param category exact category filter
     * @param available availability filter
     * @param state game state filter
     * @param playerCount exact player count (game must support this number of players)
     * @param duration exact duration in minutes
     * @return a list of games matching the search criteria
     */
    @Query("SELECT g FROM Game g WHERE " +
           "(:searchTerm IS NULL OR :searchTerm = '' OR " +
           " LOWER(g.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(g.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(g.category) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:category IS NULL OR :category = '' OR LOWER(g.category) = LOWER(:category)) AND " +
           "(:available IS NULL OR g.available = :available) AND " +
           "(:state IS NULL OR g.state = :state) AND " +
           "(:playerCount IS NULL OR " +
           " (g.minPlayers <= :playerCount AND g.maxPlayers >= :playerCount)) AND " +
           "(:duration IS NULL OR g.durationMinutes = :duration)")
    List<Game> searchGames(@Param("searchTerm") String searchTerm,
                           @Param("category") String category,
                           @Param("available") Boolean available,
                           @Param("state") GameState state,
                           @Param("playerCount") Integer playerCount,
                           @Param("duration") Integer duration);

    /**
     * Finds games with duration between given minimum and maximum values.
     *
     * @param minDuration the minimum duration in minutes
     * @param maxDuration the maximum duration in minutes
     * @return a list of matching games
     */
    @Query("SELECT g FROM Game g WHERE g.durationMinutes BETWEEN :minDuration AND :maxDuration")
    List<Game> findByDurationBetween(@Param("minDuration") int minDuration,
                                     @Param("maxDuration") int maxDuration);

    /**
     * Finds games that fit within a specific player count range using strict filtering.
     * Games must have minimum players greater than or equal to minPlayersFilter and
     * maximum players less than or equal to maxPlayersFilter.
     *
     * @param minPlayersFilter minimum players the game must require
     * @param maxPlayersFilter maximum players the game should allow
     * @return a list of matching games
     */
    @Query("SELECT g FROM Game g WHERE " +
           "(:minPlayersFilter IS NULL OR g.minPlayers >= :minPlayersFilter) AND " +
           "(:maxPlayersFilter IS NULL OR g.maxPlayers <= :maxPlayersFilter)")
    List<Game> findByPlayerCountRange(@Param("minPlayersFilter") Integer minPlayersFilter,
                                      @Param("maxPlayersFilter") Integer maxPlayersFilter);

    /**
     * Searches games by name, category and availability with pagination support.
     *
     * @param name part of the game name to search for
     * @param category exact category to filter by
     * @param available availability status filter
     * @param pageable pagination settings
     * @return a page of matching games
     */
    @Query("SELECT g FROM Game g WHERE " +
           "(:name IS NULL OR :name = '' OR LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:category IS NULL OR :category = '' OR LOWER(g.category) = LOWER(:category)) AND " +
           "(:available IS NULL OR g.available = :available)")
    Page<Game> findByNameAndCategoryAndAvailable(@Param("name") String name,
                                                 @Param("category") String category,
                                                 @Param("available") Boolean available,
                                                 Pageable pageable);

    /**
     * Finds games that support a specific number of players exactly.
     * The game must be playable with the specified player count.
     *
     * @param playerCount exact number of players
     * @return a list of games that support the specified player count
     */
    @Query("SELECT g FROM Game g WHERE g.minPlayers <= :playerCount AND g.maxPlayers >= :playerCount")
    List<Game> findGamesThatSupportPlayerCount(@Param("playerCount") Integer playerCount);
}