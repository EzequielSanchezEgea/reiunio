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

    // Juego de la biblioteca (opcional)
    @ManyToOne
    @JoinColumn(name = "game_id", nullable = true)
    private Game game;

    // Nombre del juego personalizado (requerido)
    @NotBlank
    @Column(name = "custom_game_name", length = 200)
    private String customGameName;

    // Descripción del juego personalizado (opcional)
    @Column(name = "custom_game_description", length = 500)
    private String customGameDescription;

    @Column(name = "custom_game_image_path")
    private String customGameImagePath;

    @NotBlank
    @Column(length = 100)
    private String title;

    @NotNull
    @Column(name = "start_date")
    private LocalDate startDate;

    @NotNull
    @Column(name = "start_time")
    private LocalTime startTime;

    @NotNull
    @Column(name = "end_date")
    private LocalDate endDate;

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
        
        if (players.size() >= maxPlayers) {
            return false;
        }
        
        GameSessionPlayer gameSessionPlayer = GameSessionPlayer.builder()
                .user(user)
                .gameSession(this)
                .joinDate(LocalDate.now())
                .confirmed(true) // Automáticamente confirmado
                .build();
        
        players.add(gameSessionPlayer);
        return true;
    }

    public boolean removePlayer(User user) {
        return players.removeIf(gameSessionPlayer -> 
            gameSessionPlayer.getUser().getId().equals(user.getId()));
    }

    public int getConfirmedPlayersCount() {
        // Como todos los jugadores están automáticamente confirmados,
        // simplemente devolvemos el tamaño de la lista
        return players.size();
    }

    public boolean isFull() {
        return players.size() >= maxPlayers;
    }

    // Helper method para obtener el nombre del juego a mostrar
    public String getDisplayGameName() {
        return customGameName;
    }

    // Helper method para saber si es un juego de la biblioteca
    public boolean isLibraryGame() {
        return game != null;
    }

    // Helper method para saber si es una sesión de múltiples días
    public boolean isMultiDay() {
        if (startDate == null || endDate == null) {
            return false; // Default to single day if dates are null
        }
        return !startDate.equals(endDate);
    }

    /**
     * Returns the appropriate image URL for the game session
     * Priority: Custom game image > Library game image > Default placeholder
     */
    public String getGameImageUrl() {
        // If it's a personal game with custom image
        if (!isLibraryGame() && hasCustomGameImage()) {
            return customGameImagePath;
        }
        
        // If it's a library game
        if (isLibraryGame() && game != null) {
            return game.getImageUrl();
        }
        
        // Default placeholder
        return "/defaults/game-placeholder.jpg";
    }
    
    /**
     * Returns true if the session has a custom game image
     */
    public boolean hasCustomGameImage() {
        return customGameImagePath != null && !customGameImagePath.isEmpty();
    }

    /**
     * NUEVO: Retorna true si puede tener imagen personalizada (no es juego de biblioteca)
     */
    public boolean canHaveCustomImage() {
        return !isLibraryGame();
    }
}