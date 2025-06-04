package com.ezequiel.reiunio.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.ezequiel.reiunio.enums.GameState;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

/**
 * Entity representing a board game.
 * Includes metadata such as name, description, player limits, acquisition date, state, and associations with loans and sessions.
 */
@Entity
@Table(name = "games")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"loans", "gameSessions"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Game {

    /**
     * Unique identifier for the game.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the game.
     */
    @NotBlank
    @Column(length = 100)
    private String name;

    /**
     * Optional description of the game.
     */
    @Column(length = 500)
    private String description;

    /**
     * Minimum number of players required to play the game.
     */
    @NotNull
    @Min(1)
    @Column(name = "min_players")
    private Integer minPlayers;

    /**
     * Maximum number of players allowed to play the game.
     */
    @NotNull
    @Min(1)
    @Column(name = "max_players")
    private Integer maxPlayers;

    /**
     * Estimated duration of the game in minutes.
     */
    @NotNull
    @Min(1)
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    /**
     * Optional category for the game (e.g. strategy, family, card).
     */
    private String category;

    /**
     * Availability status of the game.
     */
    @Builder.Default
    private Boolean available = true;

    /**
     * Date when the game was acquired.
     */
    @Column(name = "acquisition_date")
    @Builder.Default
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate acquisitionDate = LocalDate.now();

    /**
     * Current state of the game (e.g. NEW, USED, DAMAGED).
     */
    @Enumerated(EnumType.STRING)
    private GameState state;

    /**
     * Path to the image representing the game.
     */
    @Column(name = "image_path")
    private String imagePath;

    /**
     * List of loans associated with the game.
     * Ignored during JSON serialization.
     */
    @OneToMany(mappedBy = "game")
    @Builder.Default
    @JsonIgnore
    private List<Loan> loans = new ArrayList<>();

    /**
     * List of game sessions that have used this game.
     * Ignored during JSON serialization.
     */
    @OneToMany(mappedBy = "game")
    @Builder.Default
    @JsonIgnore
    private List<GameSession> gameSessions = new ArrayList<>();

    /**
     * Returns the URL of the game image, or a default placeholder if no custom image is set.
     *
     * @return image URL string
     */
    public String getImageUrl() {
        if (imagePath != null && !imagePath.isEmpty()) {
            return imagePath;
        }

        return "/defaults/game-placeholder.jpg";
    }

    /**
     * Checks whether the game has a custom image set.
     *
     * @return true if a custom image exists, false otherwise
     */
    public boolean hasCustomImage() {
        return imagePath != null && !imagePath.isEmpty();
    }
}
