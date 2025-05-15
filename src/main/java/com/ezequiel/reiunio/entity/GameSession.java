package com.ezequiel.reiunio.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.ezequiel.reiunio.enums.GameSessionStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Entity
@Table(name = "game_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"players"})
@EqualsAndHashCode(exclude = {"players"})
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @NotBlank
    @Column(length = 100)
    private String title;

    @NotNull
    private LocalDate date;

    @NotNull
    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @NotNull
    @Min(1)
    @Column(name = "max_players")
    private Integer maxPlayers;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private GameSessionStatus status = GameSessionStatus.SCHEDULED;

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GameSessionPlayer> players = new ArrayList<>();

    public boolean addPlayer(User user) {
        boolean alreadyRegistered = players.stream()
                .anyMatch(gsp -> gsp.getUser().getId().equals(user.getId()));
        
        if (alreadyRegistered) {
            return false;
        }
        
        if (getConfirmedPlayersCount() >= maxPlayers) {
            return false;
        }
        
        GameSessionPlayer gameSessionPlayer = GameSessionPlayer.builder()
                .user(user)
                .gameSession(this)
                .joinDate(LocalDate.now())
                .confirmed(false)
                .build();
        
        players.add(gameSessionPlayer);
        return true;
    }

    public boolean removePlayer(User user) {
        return players.removeIf(gameSessionPlayer -> 
            gameSessionPlayer.getUser().getId().equals(user.getId()));
    }

    public int getConfirmedPlayersCount() {
        return (int) players.stream()
                .filter(GameSessionPlayer::getConfirmed)
                .count();
    }

    public boolean isFull() {
        return getConfirmedPlayersCount() >= maxPlayers;
    }
}