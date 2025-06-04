package com.ezequiel.reiunio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ezequiel.reiunio.entity.GameSession;
import com.ezequiel.reiunio.entity.GameSessionPlayer;
import com.ezequiel.reiunio.entity.GameSessionPlayerId;
import com.ezequiel.reiunio.entity.User;

/**
 * Repository interface for accessing {@link GameSessionPlayer} entities,
 * which represent the relationship between users and game sessions.
 */
@Repository
public interface GameSessionPlayerRepository extends JpaRepository<GameSessionPlayer, GameSessionPlayerId> {

    /**
     * Finds all game session player records associated with a specific user.
     *
     * @param user the user
     * @return a list of {@link GameSessionPlayer} for the given user
     */
    List<GameSessionPlayer> findByUser(User user);

    /**
     * Finds all players in a specific game session.
     *
     * @param gameSession the game session
     * @return a list of {@link GameSessionPlayer} for the given session
     */
    List<GameSessionPlayer> findByGameSession(GameSession gameSession);

    /**
     * Counts the number of players in a given game session.
     *
     * @param gameSession the game session
     * @return the number of players
     */
    long countByGameSession(GameSession gameSession);

    /**
     * Finds players in a specific session based on confirmation status.
     * Since all players are assumed to be confirmed, this method returns:
     * <ul>
     *   <li>All players if {@code confirmed == null} or {@code true}</li>
     *   <li>An empty list if {@code confirmed == false}</li>
     * </ul>
     *
     * @param gameSession the game session
     * @param confirmed the confirmation flag
     * @return a list of confirmed players or empty list
     */
    default List<GameSessionPlayer> findByGameSessionAndConfirmed(GameSession gameSession, Boolean confirmed) {
        if (confirmed == null || confirmed) {
            return findByGameSession(gameSession);
        } else {
            return List.of();
        }
    }

    /**
     * Finds player records for a user based on confirmation status.
     * Since all players are assumed to be confirmed, this method returns:
     * <ul>
     *   <li>All records if {@code confirmed == null} or {@code true}</li>
     *   <li>An empty list if {@code confirmed == false}</li>
     * </ul>
     *
     * @param user the user
     * @param confirmed the confirmation flag
     * @return a list of confirmed player records or empty list
     */
    default List<GameSessionPlayer> findByUserAndConfirmed(User user, Boolean confirmed) {
        if (confirmed == null || confirmed) {
            return findByUser(user);
        } else {
            return List.of();
        }
    }

    /**
     * Counts the number of confirmed players in a session.
     * Since all players are assumed to be confirmed, this method returns:
     * <ul>
     *   <li>Total player count if {@code confirmed == null} or {@code true}</li>
     *   <li>Zero if {@code confirmed == false}</li>
     * </ul>
     *
     * @param gameSession the game session
     * @param confirmed the confirmation flag
     * @return the count of confirmed players or zero
     */
    default long countByGameSessionAndConfirmed(GameSession gameSession, Boolean confirmed) {
        if (confirmed == null || confirmed) {
            return countByGameSession(gameSession);
        } else {
            return 0L;
        }
    }
}
