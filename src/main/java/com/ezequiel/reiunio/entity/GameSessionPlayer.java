package com.ezequiel.reiunio.entity;

import java.time.LocalDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "game_session_players")
@IdClass(GameSessionPlayerId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSessionPlayer {

    @Id
    @ManyToOne
    @JoinColumn(name = "game_session_id")
    private GameSession gameSession;

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @Column(name = "join_date")
    @Builder.Default
    private LocalDate joinDate = LocalDate.now();

    @Builder.Default
    private Boolean confirmed = false;
}
