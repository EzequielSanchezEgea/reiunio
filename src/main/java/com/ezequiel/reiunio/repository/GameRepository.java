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
    
    // NEW PAGINATION AND ADVANCED SEARCH QUERIES
    
    /**
     * Get distinct categories
     */
    @Query("SELECT DISTINCT g.category FROM Game g WHERE g.category IS NOT NULL AND g.category != '' ORDER BY g.category")
    List<String> findDistinctCategories();
    
    /**
     * Advanced search with multiple filters - handles ALL possible combinations
     */
    @Query("SELECT g FROM Game g WHERE " +
           "(:name IS NULL OR :name = '' OR LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:category IS NULL OR :category = '' OR g.category = :category) AND " +
           "(:available IS NULL OR g.available = :available) AND " +
           "(:state IS NULL OR g.state = :state) AND " +
           "(:minPlayers IS NULL OR g.maxPlayers >= :minPlayers) AND " +
           "(:maxPlayers IS NULL OR g.minPlayers <= :maxPlayers) AND " +
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
     * Search games by multiple criteria without pagination (for legacy support)
     */
    @Query("SELECT g FROM Game g WHERE " +
           "(:searchTerm IS NULL OR " +
           " LOWER(g.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(g.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(g.category) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:category IS NULL OR g.category = :category) AND " +
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
     * Find games by duration range
     */
    @Query("SELECT g FROM Game g WHERE g.durationMinutes BETWEEN :minDuration AND :maxDuration")
    List<Game> findByDurationBetween(@Param("minDuration") int minDuration, 
                                    @Param("maxDuration") int maxDuration);
    
    /**
     * Find games by player count range
     */
    @Query("SELECT g FROM Game g WHERE " +
           "(:minPlayers IS NULL OR g.maxPlayers >= :minPlayers) AND " +
           "(:maxPlayers IS NULL OR g.minPlayers <= :maxPlayers)")
    List<Game> findByPlayerCountRange(@Param("minPlayers") Integer minPlayers,
                                     @Param("maxPlayers") Integer maxPlayers);
    
    /**
     * Complex search combining name, category and availability
     */
    @Query("SELECT g FROM Game g WHERE " +
           "(:name IS NULL OR LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:category IS NULL OR g.category = :category) AND " +
           "(:available IS NULL OR g.available = :available)")
    Page<Game> findByNameAndCategoryAndAvailable(@Param("name") String name,
                                                @Param("category") String category,
                                                @Param("available") Boolean available,
                                                Pageable pageable);
}