package com.ezequiel.reiunio.entity;

import java.time.LocalDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Entity representing a user participating in a specific game session.
 * This is a join table with additional attributes (composite key: gameSession + user).
 */
@Entity
@Table(name = "game_session_players")
@IdClass(GameSessionPlayerId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSessionPlayer {

    /**
     * Reference to the game session.
     */
    @Id
    @ManyToOne
    @JoinColumn(name = "game_session_id")
    private GameSession gameSession;

    /**
     * Reference to the participating user.
     */
    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Date when the user joined the session.
     * Defaults to current date.
     */
    @NotNull
    @Column(name = "join_date")
    @Builder.Default
    private LocalDate joinDate = LocalDate.now();

    /**
     * Indicates whether the user's participation is confirmed.
     */
    @Builder.Default
    private Boolean confirmed = false;
}
