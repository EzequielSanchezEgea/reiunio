package com.ezequiel.reiunio.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.ezequiel.reiunio.enums.GameSessionStatus;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Entity representing a scheduled game session.
 * Can be created using a game from the library or a custom-defined game.
 */
@Entity
@Table(name = "game_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"players"})
@EqualsAndHashCode(exclude = {"players"})
public class GameSession {

    /**
     * Unique identifier for the game session.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who created the game session.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    /**
     * Optional reference to a library game.
     */
    @ManyToOne
    @JoinColumn(name = "game_id", nullable = true)
    private Game game;

    /**
     * Name of the custom game (required if no library game is selected).
     */
    @NotBlank
    @Column(name = "custom_game_name", length = 200)
    private String customGameName;

    /**
     * Optional description for the custom game.
     */
    @Column(name = "custom_game_description", length = 500)
    private String customGameDescription;

    /**
     * Optional path to the image for the custom game.
     */
    @Column(name = "custom_game_image_path")
    private String customGameImagePath;

    /**
     * Title of the game session.
     */
    @NotBlank
    @Column(length = 100)
    private String title;

    /**
     * Start date of the session.
     */
    @NotNull
    @Column(name = "start_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * Start time of the session.
     */
    @NotNull
    @Column(name = "start_time")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime startTime;

    /**
     * End date of the session.
     */
    @NotNull
    @Column(name = "end_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * End time of the session.
     */
    @Column(name = "end_time")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime endTime;

    /**
     * Maximum number of players allowed in the session.
     */
    @NotNull
    @Min(1)
    @Column(name = "max_players")
    private Integer maxPlayers;

    /**
     * Optional description for the session.
     */
    @Column(length = 500)
    private String description;

    /**
     * Current status of the session (e.g., SCHEDULED, CANCELLED, COMPLETED).
     */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private GameSessionStatus status = GameSessionStatus.SCHEDULED;

    /**
     * List of players participating in the session.
     */
    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GameSessionPlayer> players = new ArrayList<>();

    /**
     * Adds a player to the session if not already added and capacity is not full.
     *
     * @param user the user to be added
     * @return true if added successfully, false otherwise
     */
    public boolean addPlayer(User user) {
        boolean alreadyRegistered = players.stream()
                .anyMatch(gsp -> gsp.getUser().getId().equals(user.getId()));

        if (alreadyRegistered || players.size() >= maxPlayers) {
            return false;
        }

        GameSessionPlayer gameSessionPlayer = GameSessionPlayer.builder()
                .user(user)
                .gameSession(this)
                .joinDate(LocalDate.now())
                .confirmed(true)
                .build();

        players.add(gameSessionPlayer);
        return true;
    }

    /**
     * Removes a player from the session.
     *
     * @param user the user to be removed
     * @return true if removed successfully
     */
    public boolean removePlayer(User user) {
        return players.removeIf(gameSessionPlayer ->
                gameSessionPlayer.getUser().getId().equals(user.getId()));
    }

    /**
     * Returns the number of confirmed players.
     *
     * @return number of players
     */
    public int getConfirmedPlayersCount() {
        return players.size();
    }

    /**
     * Checks if the session is full.
     *
     * @return true if the session is full
     */
    public boolean isFull() {
        return players.size() >= maxPlayers;
    }

    /**
     * Returns the display name of the game.
     * Uses the custom name if no library game is selected.
     *
     * @return game name
     */
    public String getDisplayGameName() {
        return customGameName;
    }

    /**
     * Checks if the session is using a game from the library.
     *
     * @return true if using a library game
     */
    public boolean isLibraryGame() {
        return game != null;
    }

    /**
     * Checks if the session spans multiple days.
     *
     * @return true if the session is multi-day
     */
    public boolean isMultiDay() {
        if (startDate == null || endDate == null) {
            return false;
        }
        return !startDate.equals(endDate);
    }

    /**
     * Returns the appropriate image URL for the session.
     * Priority: Custom image > Library game image > Default placeholder.
     *
     * @return image URL
     */
    public String getGameImageUrl() {
        if (!isLibraryGame() && hasCustomGameImage()) {
            return customGameImagePath;
        }

        if (isLibraryGame() && game != null) {
            return game.getImageUrl();
        }

        return "/defaults/game-placeholder.jpg";
    }

    /**
     * Checks if the session has a custom game image.
     *
     * @return true if a custom image is set
     */
    public boolean hasCustomGameImage() {
        return customGameImagePath != null && !customGameImagePath.isEmpty();
    }

    /**
     * Returns true if the session allows a custom image (i.e., not a library game).
     *
     * @return true if a custom image can be set
     */
    public boolean canHaveCustomImage() {
        return !isLibraryGame();
    }
}