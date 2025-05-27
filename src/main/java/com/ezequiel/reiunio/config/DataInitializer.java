package com.ezequiel.reiunio.config;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ezequiel.reiunio.entity.Game;
import com.ezequiel.reiunio.entity.GameSession;
import com.ezequiel.reiunio.entity.Loan;
import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.GameState;
import com.ezequiel.reiunio.enums.GameSessionStatus;
import com.ezequiel.reiunio.enums.Role;
import com.ezequiel.reiunio.repository.GameRepository;
import com.ezequiel.reiunio.repository.GameSessionRepository;
import com.ezequiel.reiunio.repository.LoanRepository;
import com.ezequiel.reiunio.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This class initializes sample data for the application.
 * Only runs when the application starts with "dev" profile.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    @Bean
    public CommandLineRunner initData(UserService userService, GameService gameService) {
        return args -> {
            // Solo inicializar si no hay datos
            if (userService.findAll().isEmpty()) {
                log.info("Inicializando datos de ejemplo...");
                initializeUsers(userService);
                initializeGames(gameService);
                log.info("Datos de ejemplo inicializados correctamente");
            } else {
                log.info("Los datos ya existen, saltando inicialización");
            }
        };
    }

    private void initializeUsers(UserService userService) {
        log.info("Creando usuarios de ejemplo...");

        // Usuario administrador
        User admin = User.builder()
                .username("admin")
                .password("admin123")
                .email("admin@reiunio.es")
                .firstName("Administrador")
                .lastName("Reiunio")
                .role(Role.ADMIN)
                .registrationDate(LocalDate.now().minusMonths(12))
                .build();

        // Usuario con permisos extendidos
        User extendedUser = User.builder()
                .username("manager")
                .password("manager123")
                .email("manager@reiunio.es")
                .firstName("Miguel")
                .lastName("Gestor")
                .role(Role.EXTENDED_USER)
                .registrationDate(LocalDate.now().minusMonths(8))
                .build();

        // Usuarios básicos
        User basicUser1 = User.builder()
                .username("jugador1")
                .password("jugador123")
                .email("ana@example.com")
                .firstName("Ana")
                .lastName("García")
                .role(Role.BASIC_USER)
                .registrationDate(LocalDate.now().minusMonths(6))
                .build();

        User basicUser2 = User.builder()
                .username("jugador2")
                .password("jugador123")
                .email("carlos@example.com")
                .firstName("Carlos")
                .lastName("López")
                .role(Role.BASIC_USER)
                .registrationDate(LocalDate.now().minusMonths(4))
                .build();

        User basicUser3 = User.builder()
                .username("jugador3")
                .password("jugador123")
                .email("maria@example.com")
                .firstName("María")
                .lastName("Rodríguez")
                .role(Role.BASIC_USER)
                .registrationDate(LocalDate.now().minusMonths(3))
                .build();

        User basicUser4 = User.builder()
                .username("jugador4")
                .password("jugador123")
                .email("david@example.com")
                .firstName("David")
                .lastName("Martín")
                .role(Role.BASIC_USER)
                .registrationDate(LocalDate.now().minusMonths(2))
                .build();

        User basicUser5 = User.builder()
                .username("jugador5")
                .password("jugador123")
                .email("lucia@example.com")
                .firstName("Lucía")
                .lastName("Fernández")
                .role(Role.BASIC_USER)
                .registrationDate(LocalDate.now().minusMonths(1))
                .build();

        List<User> users = Arrays.asList(admin, extendedUser, basicUser1, basicUser2, basicUser3, basicUser4, basicUser5);
        
        for (User user : users) {
            userService.save(user);
            log.info("Usuario creado: {} ({})", user.getUsername(), user.getRole());
        }
    }

    private void initializeGames(GameService gameService) {
        log.info("Creando biblioteca de juegos...");

        List<Game> games = Arrays.asList(
            // Juegos de estrategia
            Game.builder()
                .name("Catan")
                .description("Juego de construcción y comercio en una isla")
                .category("Estrategia")
                .minPlayers(3)
                .maxPlayers(4)
                .durationMinutes(75)
                .state(GameState.GOOD)
                .available(true)
                .acquisitionDate(LocalDate.now().minusYears(2))
                .build(),

            Game.builder()
                .name("Ticket to Ride")
                .description("Aventura ferroviaria por todo el mundo")
                .category("Estrategia")
                .minPlayers(2)
                .maxPlayers(5)
                .durationMinutes(60)
                .state(GameState.NEW)
                .available(true)
                .acquisitionDate(LocalDate.now().minusMonths(6))
                .build(),

            Game.builder()
                .name("Wingspan")
                .description("Estrategia sobre conservación de aves")
                .category("Estrategia")
                .minPlayers(1)
                .maxPlayers(5)
                .durationMinutes(70)
                .state(GameState.GOOD)
                .available(true)
                .acquisitionDate(LocalDate.now().minusMonths(8))
                .build(),

            Game.builder()
                .name("Azul")
                .description("Juego de colocación de azulejos")
                .category("Estrategia")
                .minPlayers(2)
                .maxPlayers(4)
                .durationMinutes(45)
                .state(GameState.GOOD)
                .available(true)
                .acquisitionDate(LocalDate.now().minusMonths(10))
                .build(),

            Game.builder()
                .name("7 Wonders")
                .description("Construye una de las siete maravillas del mundo")
                .category("Estrategia")
                .minPlayers(2)
                .maxPlayers(7)
                .durationMinutes(30)
                .state(GameState.ACCEPTABLE)
                .available(true)
                .acquisitionDate(LocalDate.now().minusYears(3))
                .build(),

            Game.builder()
                .name("Splendor")
                .description("Juego de comercio de gemas renacentista")
                .category("Estrategia")
                .minPlayers(2)
                .maxPlayers(4)
                .durationMinutes(30)
                .state(GameState.GOOD)
                .available(true)
                .acquisitionDate(LocalDate.now().minusMonths(4))
                .build(),

            Game.builder()
                .name("King of Tokyo")
                .description("Monstruos mutantes compiten por Tokyo")
                .category("Estrategia")
                .minPlayers(2)
                .maxPlayers(6)
                .durationMinutes(45)
                .state(GameState.GOOD)
                .available(true)
                .acquisitionDate(LocalDate.now().minusMonths(7))
                .build(),

            // Juegos familiares
            Game.builder()
                .name("Dixit")
                .description("Juego de creatividad e imaginación")
                .category("Familiar")
                .minPlayers(3)
                .maxPlayers(8)
                .durationMinutes(30)
                .state(GameState.GOOD)
                .available(true)
                .acquisitionDate(LocalDate.now().minusMonths(5))
                .build(),

            Game.builder()
                .name("Uno")
                .description("El clásico juego de cartas para toda la familia")
                .category("Familiar")
                .minPlayers(2)
                .maxPlayers(10)
                .durationMinutes(15)
                .state(GameState.ACCEPTABLE)
                .available(true)
                .acquisitionDate(LocalDate.now().minusYears(5))
                .build(),

            Game.builder()
                .name("Monopoly")
                .description("Compra, vende y negocia propiedades")
                .category("Familiar")
                .minPlayers(2)
                .maxPlayers(8)
                .durationMinutes(180)
                .state(GameState.ACCEPTABLE)
                .available(true)
                .acquisitionDate(LocalDate.now().minusYears(4))
                .build(),

            Game.builder()
                .name("Scrabble")
                .description("Forma palabras y gana puntos")
                .category("Familiar")
                .minPlayers(2)
                .maxPlayers(4)
                .durationMinutes(90)
                .state(GameState.GOOD)
                .available(true)
                .acquisitionDate(LocalDate.now().minusYears(1))
                .build(),

            Game.builder()
                .name("Pictionary")
                .description("Dibuja y adivina para ganar")
                .category("Familiar")
                .minPlayers(3)
                .maxPlayers(16)
                .durationMinutes(60)
                .state(GameState.GOOD)
                .available(true)
                .acquisitionDate(LocalDate.now().minusMonths(9))
                .build(),

            Game.builder()
                .name("Trivial Pursuit")
                .description("El juego de preguntas y respuestas más famoso")
                .category("Familiar")
                .minPlayers(2)
                .maxPlayers(6)
                .durationMinutes(120)
                .state(GameState.GOOD)
                .available(true)
                .acquisitionDate(LocalDate.now().minusYears(2))
                .build(),

            // Juegos de fiesta
            Game.builder()
                .name("Telestrations")
                .description("El juego del teléfono estropeado con dibujos")
                .category("Fiesta")
                .minPlayers(4)
                .maxPlayers(8)
                .durationMinutes(30)
                .state(GameState.GOOD)
                .available(true)
                .acquisitionDate(LocalDate.now().minusMonths(3))
                .build(),

            Game.builder()
                .name("Codenames")
                .description("Juego de palabras clave y espías")
                .category("Fiesta")
                .minPlayers(2)
                .maxPlayers(8)
                .durationMinutes(15)
                .state(GameState.NEW)
                .available(true)
                .acquisitionDate(LocalDate.now().minusMonths(2))
                .build(),

            Game.builder()
                .name("Just One")
                .description("Juego cooperativo de adivinanzas")
                .category("Fiesta")
                .minPlayers(3)
                .maxPlayers(8)
                .durationMinutes(20)
                .state(GameState.GOOD)
                .available(true)
                .acquisitionDate(LocalDate.now().minusMonths(4))
                .build(),

            Game.builder()
                .name("Sushi Go!")
                .description("Juego de cartas rápido sobre sushi")
                .category("Fiesta")
                .minPlayers(2)
                .maxPlayers(5)
                .durationMinutes(15)
                .state(GameState.GOOD)
                .available(true)
                .acquisitionDate(LocalDate.now().minusMonths(6))
                .build(),

            // Juegos cooperativos
            Game.builder()
                .name("Pandemic")
                .description("Salva al mundo de enfermedades mortales")
                .category("Cooperativo")
                .minPlayers(2)
                .maxPlayers(4)
                .durationMinutes(45)
                .state(GameState.GOOD)
                .available(true)
                .acquisitionDate(LocalDate.now().minusMonths(11))
                .build(),

            Game.builder()
                .name("Forbidden Island")
                .description("Trabajo en equipo para encontrar tesoros")
                .category("Cooperativo")
                .minPlayers(2)
                .maxPlayers(4)
                .durationMinutes(30)
                .state(GameState.GOOD)
                .available(true)
                .acquisitionDate(LocalDate.now().minusMonths(8))
                .build(),

            Game.builder()
                .name("Flash Point: Fire Rescue")
                .description("Cooperad como bomberos para salvar vidas")
                .category("Cooperativo")
                .minPlayers(2)
                .maxPlayers(6)
                .durationMinutes(45)
                .state(GameState.ACCEPTABLE)
                .available(true)
                .acquisitionDate(LocalDate.now().minusYears(1))
                .build(),

            // Juegos adicionales para más variedad
            Game.builder()
                .name("Chess")
                .description("El clásico juego de estrategia")
                .category("Abstracto")
                .minPlayers(2)
                .maxPlayers(2)
                .durationMinutes(60)
                .state(GameState.ACCEPTABLE)
                .available(true)
                .acquisitionDate(LocalDate.now().minusYears(10))
                .build(),

            Game.builder()
                .name("Checkers")
                .description("Juego de estrategia con fichas")
                .category("Abstracto")
                .minPlayers(2)
                .maxPlayers(2)
                .durationMinutes(30)
                .state(GameState.GOOD)
                .available(true)
                .acquisitionDate(LocalDate.now().minusYears(5))
                .build(),

            Game.builder()
                .name("Risk")
                .description("Conquista el mundo en este juego de estrategia")
                .category("Estrategia")
                .minPlayers(2)
                .maxPlayers(6)
                .durationMinutes(180)
                .state(GameState.ACCEPTABLE)
                .available(true)
                .acquisitionDate(LocalDate.now().minusYears(3))
                .build(),

            Game.builder()
                .name("Exploding Kittens")
                .description("Juego de cartas explosivo y divertido")
                .category("Fiesta")
                .minPlayers(2)
                .maxPlayers(5)
                .durationMinutes(15)
                .state(GameState.NEW)
                .available(true)
                .acquisitionDate(LocalDate.now().minusMonths(1))
                .build()
        );

        // ¡AQUÍ ESTABA EL PROBLEMA! Hay que guardar cada juego
        for (Game game : games) {
            gameService.save(game);
            log.info("Juego creado: {} ({})", game.getName(), game.getCategory());
        }
        
        log.info("Se crearon {} juegos en la biblioteca", games.size());
    }
}
