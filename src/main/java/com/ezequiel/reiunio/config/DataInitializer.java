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
//@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final LoanRepository loanRepository;
    private final GameSessionRepository gameSessionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Only initialize data if no users exist in the database
        if (userRepository.count() == 0) {
            log.info("Initializing sample data...");
            initUsers();
            initGames();
            initLoans();
            initGameSessions();
            log.info("Sample data initialized successfully");
        }
    }

    private void initUsers() {
        log.debug("Initializing users...");

        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin"))
                .email("admin@reiunio.com")
                .firstName("Administrator")
                .lastName("System")
                .role(Role.ADMIN)
                .registrationDate(LocalDate.now())
                .build();

        User user1 = User.builder()
                .username("user1")
                .password(passwordEncoder.encode("password"))
                .email("user1@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(Role.BASIC_USER)
                .registrationDate(LocalDate.now())
                .build();

        User user2 = User.builder()
                .username("user2")
                .password(passwordEncoder.encode("password"))
                .email("user2@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .role(Role.EXTENDED_USER)
                .registrationDate(LocalDate.now())
                .build();

        userRepository.saveAll(Arrays.asList(admin, user1, user2));
    }

    private void initGames() {
        log.debug("Initializing games...");

        Game game1 = Game.builder()
                .name("Catan")
                .description("Strategy and trading game set on an island")
                .minPlayers(3)
                .maxPlayers(4)
                .durationMinutes(120)
                .category("Strategy")
                .available(true)
                .state(GameState.GOOD)
                .acquisitionDate(LocalDate.now().minusMonths(2))
                .build();

        Game game2 = Game.builder()
                .name("Carcassonne")
                .description("Tile-placement game for building cities and roads")
                .minPlayers(2)
                .maxPlayers(5)
                .durationMinutes(45)
                .category("Strategy")
                .available(true)
                .state(GameState.NEW)
                .acquisitionDate(LocalDate.now().minusMonths(1))
                .build();

        Game game3 = Game.builder()
                .name("Dixit")
                .description("Card game with illustrations for guessing stories")
                .minPlayers(3)
                .maxPlayers(6)
                .durationMinutes(30)
                .category("Cards")
                .available(true)
                .state(GameState.GOOD)
                .acquisitionDate(LocalDate.now().minusMonths(3))
                .build();

        gameRepository.saveAll(Arrays.asList(game1, game2, game3));
    }

    private void initLoans() {
        log.debug("Initializing loans...");

        // Get users and games
        User user1 = userRepository.findByUsername("user1").orElseThrow();
        User user2 = userRepository.findByUsername("user2").orElseThrow();
        
        Game catan = gameRepository.findByNameContainingIgnoreCase("Catan").get(0);
        Game carcassonne = gameRepository.findByNameContainingIgnoreCase("Carcassonne").get(0);

        // Create loans
        Loan loan1 = Loan.builder()
                .user(user1)
                .game(catan)
                .loanDate(LocalDate.now().minusDays(5))
                .estimatedReturnDate(LocalDate.now().plusDays(2))
                .status(com.ezequiel.reiunio.enums.LoanStatus.ACTIVE)
                .build();
        
        // Mark the game as unavailable
        catan.setAvailable(false);
        gameRepository.save(catan);

        loanRepository.save(loan1);
    }

    private void initGameSessions() {
        log.debug("Initializing game sessions...");

        // Get users and games
        User admin = userRepository.findByUsername("admin").orElseThrow();
        User user1 = userRepository.findByUsername("user1").orElseThrow();
        
        Game dixit = gameRepository.findByNameContainingIgnoreCase("Dixit").get(0);
        Game carcassonne = gameRepository.findByNameContainingIgnoreCase("Carcassonne").get(0);

        // Create game sessions
        GameSession session1 = GameSession.builder()
                .creator(admin)
                .game(dixit)
                .title("Dixit Afternoon")
                .description("Game session for beginners")
                .date(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(17, 0))
                .endTime(LocalTime.of(19, 0))
                .maxPlayers(6)
                .status(GameSessionStatus.SCHEDULED)
                .build();

        GameSession session2 = GameSession.builder()
                .creator(user1)
                .game(carcassonne)
                .title("Carcassonne Championship")
                .description("Official tournament")
                .date(LocalDate.now().plusDays(7))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(14, 0))
                .maxPlayers(5)
                .status(GameSessionStatus.SCHEDULED)
                .build();

        gameSessionRepository.saveAll(Arrays.asList(session1, session2));
    }
}
