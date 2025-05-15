package com.ezequiel.reiunio.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.ezequiel.reiunio.enums.GameState;

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
    private LocalDate acquisitionDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    private GameState state;

    @OneToMany(mappedBy = "game")
    @Builder.Default
    private List<Loan> loans = new ArrayList<>();

    @OneToMany(mappedBy = "game")
    @Builder.Default
    private List<GameSession> gameSessions = new ArrayList<>();
}