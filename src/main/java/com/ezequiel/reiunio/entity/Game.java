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

@Entity
@Table(name = "games")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"loans", "gameSessions"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @NotNull
    @Min(1)
    @Column(name = "min_players")
    private Integer minPlayers;

    @NotNull
    @Min(1)
    @Column(name = "max_players")
    private Integer maxPlayers;

    @NotNull
    @Min(1)
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    private String category;

    @Builder.Default
    private Boolean available = true;

    @Column(name = "acquisition_date")
    @Builder.Default
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate acquisitionDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    private GameState state;

    // Field for game photo
    @Column(name = "image_path")
    private String imagePath;

    @OneToMany(mappedBy = "game")
    @Builder.Default
    @JsonIgnore  // Evitar serialización de relaciones para el endpoint AJAX
    private List<Loan> loans = new ArrayList<>();

    @OneToMany(mappedBy = "game")
    @Builder.Default
    @JsonIgnore  // Evitar serialización de relaciones para el endpoint AJAX
    private List<GameSession> gameSessions = new ArrayList<>();

    /**
     * Returns the game image URL or a default placeholder if no image is set
     * Este método se incluirá en la serialización JSON
     */
    public String getImageUrl() {
        if (imagePath != null && !imagePath.isEmpty()) {
            return imagePath;
        }
        
        // Return a default game placeholder image
        return "/defaults/game-placeholder.jpg";
    }

    /**
     * Returns true if game has a custom image
     */
    public boolean hasCustomImage() {
        return imagePath != null && !imagePath.isEmpty();
    }
}