//package com.ezequiel.reiunio.config;
//
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.util.Arrays;
//
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Profile;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.ezequiel.reiunio.entity.Game;
//import com.ezequiel.reiunio.entity.GameSession;
//import com.ezequiel.reiunio.entity.Loan;
//import com.ezequiel.reiunio.entity.User;
//import com.ezequiel.reiunio.enums.GameState;
//import com.ezequiel.reiunio.enums.GameSessionStatus;
//import com.ezequiel.reiunio.enums.Role;
//import com.ezequiel.reiunio.repository.GameRepository;
//import com.ezequiel.reiunio.repository.GameSessionRepository;
//import com.ezequiel.reiunio.repository.LoanRepository;
//import com.ezequiel.reiunio.repository.UserRepository;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
///**
// * This class initializes sample data for the application.
// * Only runs when the application starts with "dev" profile.
// */
//@Component
//@Profile("dev")
//@RequiredArgsConstructor
//@Slf4j
//public class DataInitializer implements CommandLineRunner {
//
//    private final UserRepository userRepository;
//    private final GameRepository gameRepository;
//    private final LoanRepository loanRepository;
//    private final GameSessionRepository gameSessionRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @Override
//    @Transactional
//    public void run(String... args) throws Exception {
//        // Only initialize data if no users exist in the database
//        if (userRepository.count() == 0) {
//            log.info("Initializing sample data...");
//            initUsers();
//            initGames();
//            initLoans();
//            initGameSessions();
//            log.info("Sample data initialized successfully");
//        }
//    }
//
//    private void initUsers() {
//        log.debug("Initializing users...");
//
//        User admin = User.builder()
//                .username("admin")
//                .password(passwordEncoder.encode("admin"))
//                .email("admin@reiunio.com")
//                .firstName("Administrator")
//                .lastName("System")
//                .role(Role.ADMIN)
//                .registrationDate(LocalDate.now())
//                .build();
//
//        User user1 = User.builder()
//                .username("user1")
//                .password(passwordEncoder.encode("password"))
//                .email("user1@example.com")
//                .firstName("John")
//                .lastName("Doe")
//                .role(Role.BASIC_USER)
//                .registrationDate(LocalDate.now())
//                .build();
//
//        User user2 = User.builder()
//                .username("user2")
//                .password(passwordEncoder.encode("password"))
//                .email("user2@example.com")
//                .firstName("Jane")
//                .lastName("Smith")
//                .role(Role.EXTENDED_USER)
//                .registrationDate(LocalDate.now())
//                .build();
//
//        User user3 = User.builder()
//                .username("maria")
//                .password(passwordEncoder.encode("password"))
//                .email("maria@example.com")
//                .firstName("María")
//                .lastName("García")
//                .role(Role.BASIC_USER)
//                .registrationDate(LocalDate.now())
//                .build();
//
//        userRepository.saveAll(Arrays.asList(admin, user1, user2, user3));
//    }
//
//    private void initGames() {
//        log.debug("Initializing games...");
//
//        Game game1 = Game.builder()
//                .name("Catan")
//                .description("Strategy and trading game set on an island")
//                .minPlayers(3)
//                .maxPlayers(4)
//                .durationMinutes(120)
//                .category("Strategy")
//                .available(true)
//                .state(GameState.GOOD)
//                .acquisitionDate(LocalDate.now().minusMonths(2))
//                .build();
//
//        Game game2 = Game.builder()
//                .name("Carcassonne")
//                .description("Tile-placement game for building cities and roads")
//                .minPlayers(2)
//                .maxPlayers(5)
//                .durationMinutes(45)
//                .category("Strategy")
//                .available(true)
//                .state(GameState.NEW)
//                .acquisitionDate(LocalDate.now().minusMonths(1))
//                .build();
//
//        Game game3 = Game.builder()
//                .name("Dixit")
//                .description("Card game with illustrations for guessing stories")
//                .minPlayers(3)
//                .maxPlayers(6)
//                .durationMinutes(30)
//                .category("Cards")
//                .available(true)
//                .state(GameState.GOOD)
//                .acquisitionDate(LocalDate.now().minusMonths(3))
//                .build();
//
//        Game game4 = Game.builder()
//                .name("Ticket to Ride")
//                .description("Railway-themed board game")
//                .minPlayers(2)
//                .maxPlayers(5)
//                .durationMinutes(60)
//                .category("Strategy")
//                .available(true)
//                .state(GameState.NEW)
//                .acquisitionDate(LocalDate.now().minusWeeks(2))
//                .build();
//
//        Game game5 = Game.builder()
//                .name("Azul")
//                .description("Tile-laying game inspired by Portuguese tiles")
//                .minPlayers(2)
//                .maxPlayers(4)
//                .durationMinutes(45)
//                .category("Abstract")
//                .available(true)
//                .state(GameState.GOOD)
//                .acquisitionDate(LocalDate.now().minusWeeks(3))
//                .build();
//
//        gameRepository.saveAll(Arrays.asList(game1, game2, game3, game4, game5));
//    }
//
//    private void initLoans() {
//        log.debug("Initializing loans...");
//
//        // Get users and games
//        User user1 = userRepository.findByUsername("user1").orElseThrow();
//        User user2 = userRepository.findByUsername("user2").orElseThrow();
//        
//        Game catan = gameRepository.findByNameContainingIgnoreCase("Catan").get(0);
//        Game carcassonne = gameRepository.findByNameContainingIgnoreCase("Carcassonne").get(0);
//
//        // Create loans
//        Loan loan1 = Loan.builder()
//                .user(user1)
//                .game(catan)
//                .loanDate(LocalDate.now().minusDays(5))
//                .estimatedReturnDate(LocalDate.now().plusDays(2))
//                .status(com.ezequiel.reiunio.enums.LoanStatus.ACTIVE)
//                .build();
//        
//        Loan loan2 = Loan.builder()
//                .user(user2)
//                .game(carcassonne)
//                .loanDate(LocalDate.now().minusDays(10))
//                .estimatedReturnDate(LocalDate.now().minusDays(3))
//                .actualReturnDate(LocalDate.now().minusDays(1))
//                .status(com.ezequiel.reiunio.enums.LoanStatus.LATE)
//                .build();
//        
//        // Mark the games as unavailable/available
//        catan.setAvailable(false);
//        carcassonne.setAvailable(true); // Returned
//        
//        gameRepository.saveAll(Arrays.asList(catan, carcassonne));
//        loanRepository.saveAll(Arrays.asList(loan1, loan2));
//    }
//
//    private void initGameSessions() {
//        log.debug("Initializing game sessions...");
//
//        // Get users and games
//        User admin = userRepository.findByUsername("admin").orElseThrow();
//        User user1 = userRepository.findByUsername("user1").orElseThrow();
//        User user2 = userRepository.findByUsername("user2").orElseThrow();
//        User maria = userRepository.findByUsername("maria").orElseThrow();
//        
//        Game dixit = gameRepository.findByNameContainingIgnoreCase("Dixit").get(0);
//        Game ticketToRide = gameRepository.findByNameContainingIgnoreCase("Ticket").get(0);
//        Game azul = gameRepository.findByNameContainingIgnoreCase("Azul").get(0);
//
//        // 1. Library game session - single day
//        GameSession session1 = GameSession.builder()
//                .creator(admin)
//                .game(dixit)
//                .customGameName(dixit.getName())
//                .customGameDescription("Great storytelling card game from our library")
//                .title("Dixit Afternoon")
//                .description("Perfect for beginners! Come and enjoy this creative storytelling game.")
//                .startDate(LocalDate.now().plusDays(1))
//                .startTime(LocalTime.of(17, 0))
//                .endDate(LocalDate.now().plusDays(1))
//                .endTime(LocalTime.of(19, 0))
//                .maxPlayers(6)
//                .status(GameSessionStatus.SCHEDULED)
//                .build();
//
//        // 2. Library game session - tournament style
//        GameSession session2 = GameSession.builder()
//                .creator(user1)
//                .game(ticketToRide)
//                .customGameName(ticketToRide.getName())
//                .customGameDescription("Railway adventure across Europe")
//                .title("Ticket to Ride Championship")
//                .description("Official tournament with prizes! Bring your A-game.")
//                .startDate(LocalDate.now().plusDays(7))
//                .startTime(LocalTime.of(10, 0))
//                .endDate(LocalDate.now().plusDays(7))
//                .endTime(LocalTime.of(14, 0))
//                .maxPlayers(5)
//                .status(GameSessionStatus.SCHEDULED)
//                .build();
//
//        // 3. Personal game session - single day
//        GameSession session3 = GameSession.builder()
//                .creator(user2)
//                .game(null) // Personal game, not from library
//                .customGameName("Dungeons & Dragons 5e")
//                .customGameDescription("Fantasy tabletop RPG campaign")
//                .title("The Lost Mines of Phandelver")
//                .description("Beginner-friendly D&D campaign. All materials provided. No experience necessary!")
//                .startDate(LocalDate.now().plusDays(3))
//                .startTime(LocalTime.of(19, 0))
//                .endDate(LocalDate.now().plusDays(3))
//                .endTime(LocalTime.of(23, 0))
//                .maxPlayers(4)
//                .status(GameSessionStatus.SCHEDULED)
//                .build();
//
//        // 4. Multi-day event - personal game
//        GameSession session4 = GameSession.builder()
//                .creator(maria)
//                .game(null) // Personal game
//                .customGameName("Magic: The Gathering")
//                .customGameDescription("Trading card game tournament")
//                .title("MTG Weekend Tournament")
//                .description("Two-day Magic tournament with multiple formats. Bring your decks!")
//                .startDate(LocalDate.now().plusDays(14))
//                .startTime(LocalTime.of(10, 0))
//                .endDate(LocalDate.now().plusDays(15))
//                .endTime(LocalTime.of(18, 0))
//                .maxPlayers(8)
//                .status(GameSessionStatus.SCHEDULED)
//                .build();
//
//        // 5. Library game session - evening
//        GameSession session5 = GameSession.builder()
//                .creator(admin)
//                .game(azul)
//                .customGameName(azul.getName())
//                .customGameDescription("Beautiful tile-laying strategy game")
//                .title("Azul Evening Games")
//                .description("Relaxed evening session with this gorgeous strategy game.")
//                .startDate(LocalDate.now().plusDays(5))
//                .startTime(LocalTime.of(20, 0))
//                .endDate(LocalDate.now().plusDays(5))
//                .endTime(LocalTime.of(22, 0))
//                .maxPlayers(4)
//                .status(GameSessionStatus.SCHEDULED)
//                .build();
//
//        // 6. Today's session
//        GameSession todaySession = GameSession.builder()
//                .creator(user1)
//                .game(null) // Personal game
//                .customGameName("Uno")
//                .customGameDescription("Classic card game")
//                .title("Quick Uno Match")
//                .description("Fast-paced card game session during lunch break.")
//                .startDate(LocalDate.now())
//                .startTime(LocalTime.of(12, 30))
//                .endDate(LocalDate.now())
//                .endTime(LocalTime.of(13, 30))
//                .maxPlayers(4)
//                .status(GameSessionStatus.IN_PROGRESS)
//                .build();
//
//        // 7. Finished session
//        GameSession finishedSession = GameSession.builder()
//                .creator(user2)
//                .game(azul)
//                .customGameName(azul.getName())
//                .customGameDescription("Completed tournament")
//                .title("Azul Mini Tournament")
//                .description("Completed mini tournament from last week.")
//                .startDate(LocalDate.now().minusDays(3))
//                .startTime(LocalTime.of(15, 0))
//                .endDate(LocalDate.now().minusDays(3))
//                .endTime(LocalTime.of(18, 0))
//                .maxPlayers(4)
//                .status(GameSessionStatus.FINISHED)
//                .build();
//
//        gameSessionRepository.saveAll(Arrays.asList(
//            session1, session2, session3, session4, session5, todaySession, finishedSession
//        ));
//
//        log.info("Created {} game sessions with various types and statuses", 7);
//    }
//}