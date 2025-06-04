package com.ezequiel.reiunio.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ezequiel.reiunio.entity.Game;
import com.ezequiel.reiunio.entity.GameSession;
import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.GameSessionStatus;

/**
 * Repository interface for managing {@link GameSession} entities.
 */
@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

    /**
     * Finds all sessions created by a specific user.
     *
     * @param creator the user who created the session
     * @return list of game sessions created by the user
     */
    List<GameSession> findByCreator(User creator);

    /**
     * Finds all sessions that are associated with a specific game.
     *
     * @param game the game entity
     * @return list of game sessions for the given game
     */
    List<GameSession> findByGame(Game game);

    /**
     * Finds sessions with a specific status.
     *
     * @param status the game session status
     * @return list of game sessions with the given status
     */
    List<GameSession> findByStatus(GameSessionStatus status);

    /**
     * Finds sessions starting after a specific date and with a given status.
     *
     * @param date the starting date
     * @param status the game session status
     * @return list of upcoming game sessions
     */
    List<GameSession> findByStartDateAfterAndStatus(LocalDate date, GameSessionStatus status);

    /**
     * Finds sessions where either the start date or end date falls within the given ranges.
     *
     * @param startDate1 range 1 start
     * @param endDate1 range 1 end
     * @param startDate2 range 2 start
     * @param endDate2 range 2 end
     * @return list of game sessions within the given date ranges
     */
    List<GameSession> findByStartDateBetweenOrEndDateBetween(LocalDate startDate1, LocalDate endDate1, LocalDate startDate2, LocalDate endDate2);

    /**
     * Finds all sessions where the given user is a participant.
     *
     * @param userId the ID of the user
     * @return list of game sessions the user participates in
     */
    @Query("SELECT gs FROM GameSession gs JOIN gs.players gsp WHERE gsp.user.id = :userId")
    List<GameSession> findSessionsByPlayer(Long userId);

    /**
     * Finds sessions that are active today (start date <= today and end date >= today).
     *
     * @return list of today's game sessions
     */
    @Query("SELECT gs FROM GameSession gs WHERE gs.startDate <= CURRENT_DATE AND gs.endDate >= CURRENT_DATE")
    List<GameSession> findTodaySessions();

    /**
     * Finds sessions by partial match of the custom game name, ignoring case.
     *
     * @param gameName the custom game name substring
     * @return list of game sessions with matching custom names
     */
    List<GameSession> findByCustomGameNameContainingIgnoreCase(String gameName);

    /**
     * Finds all upcoming scheduled sessions that start today or later.
     *
     * @return list of upcoming game sessions
     */
    @Query("SELECT gs FROM GameSession gs WHERE gs.startDate >= CURRENT_DATE AND gs.status = 'SCHEDULED' ORDER BY gs.startDate, gs.startTime")
    List<GameSession> findUpcomingSessions();

    /**
     * Finds all sessions that fall within or span a specific date range.
     *
     * @param startDate the range start date
     * @param endDate the range end date
     * @return list of game sessions within the date range
     */
    @Query("SELECT gs FROM GameSession gs WHERE " +
           "(gs.startDate BETWEEN :startDate AND :endDate) OR " +
           "(gs.endDate BETWEEN :startDate AND :endDate) OR " +
           "(gs.startDate <= :startDate AND gs.endDate >= :endDate)")
    List<GameSession> findSessionsInDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Finds sessions that span multiple days.
     *
     * @return list of multi-day game sessions
     */
    @Query("SELECT gs FROM GameSession gs WHERE gs.startDate <> gs.endDate")
    List<GameSession> findMultiDaySessions();

    /**
     * Finds sessions that occur on a single day.
     *
     * @return list of single-day game sessions
     */
    @Query("SELECT gs FROM GameSession gs WHERE gs.startDate = gs.endDate")
    List<GameSession> findSingleDaySessions();
}
