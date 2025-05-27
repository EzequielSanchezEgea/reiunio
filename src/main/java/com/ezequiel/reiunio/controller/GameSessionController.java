package com.ezequiel.reiunio.controller;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ezequiel.reiunio.entity.Game;
import com.ezequiel.reiunio.entity.GameSession;
import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.GameSessionStatus;
import com.ezequiel.reiunio.enums.ActionType;
import com.ezequiel.reiunio.service.AuditLogService;
import com.ezequiel.reiunio.service.FileUploadService; // AGREGADO
import com.ezequiel.reiunio.service.GameService;
import com.ezequiel.reiunio.service.GameSessionService;
import com.ezequiel.reiunio.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/game-sessions")
@RequiredArgsConstructor
@Slf4j
public class GameSessionController {

    private final GameSessionService gameSessionService;
    private final GameService gameService;
    private final UserService userService;
    private final AuditLogService auditLogService;
    private final FileUploadService fileUploadService; // AGREGADO

    // Método principal actualizado para agrupar sesiones por días
    @GetMapping
    public String listGameSessions(Model model, 
                                  @RequestParam(required = false) String filter,
                                  Principal principal) {
        List<GameSession> gameSessions;
        String listTitle = "Game Sessions";
        
        log.debug("Listing game sessions with filter: {}", filter);
        
        // Aplicar filtros según el parámetro
        if (filter != null && !filter.isEmpty()) {
            switch (filter.toLowerCase()) {
                case "today":
                    gameSessions = gameSessionService.findTodaySessions();
                    listTitle = "Today's Sessions";
                    log.debug("Found {} today's sessions", gameSessions.size());
                    break;
                case "upcoming":
                    gameSessions = gameSessionService.findUpcomingSessions();
                    listTitle = "Upcoming Sessions";
                    log.debug("Found {} upcoming sessions", gameSessions.size());
                    break;
                case "registered":
                    gameSessions = getRegisteredSessions(principal);
                    listTitle = "My Registered Sessions";
                    log.debug("Found {} registered sessions for user", gameSessions.size());
                    break;
                case "finished":
                    gameSessions = gameSessionService.findByStatus(GameSessionStatus.FINISHED);
                    listTitle = "Finished Sessions";
                    log.debug("Found {} finished sessions", gameSessions.size());
                    break;
                default:
                    gameSessions = gameSessionService.findAll();
                    log.debug("Found {} total sessions", gameSessions.size());
                    break;
            }
        } else {
            gameSessions = gameSessionService.findAll();
            log.debug("Found {} total sessions (no filter)", gameSessions.size());
        }
        
        // Agrupar sesiones por día
        Map<LocalDate, List<GameSession>> sessionsByDay = groupSessionsByDay(gameSessions);
        log.debug("Grouped sessions into {} days", sessionsByDay.size());
        
        model.addAttribute("sessionsByDay", sessionsByDay);
        model.addAttribute("gameSessions", gameSessions); // Mantener para compatibilidad
        model.addAttribute("listTitle", listTitle);
        
        return "game-sessions/list";
    }

    /**
     * Obtiene las sesiones en las que el usuario está registrado
     */
    private List<GameSession> getRegisteredSessions(Principal principal) {
        if (principal == null) {
            return List.of();
        }
        
        Optional<User> userOpt = userService.findByUsername(principal.getName());
        if (userOpt.isPresent()) {
            return gameSessionService.findSessionsByPlayer(userOpt.get().getId());
        }
        
        return List.of();
    }

    /**
     * Agrupa las sesiones por día de inicio, ordenadas cronológicamente
     */
    private Map<LocalDate, List<GameSession>> groupSessionsByDay(List<GameSession> gameSessions) {
        if (gameSessions == null || gameSessions.isEmpty()) {
            return new LinkedHashMap<>();
        }
        
        log.debug("Grouping {} sessions by day", gameSessions.size());
        
        Map<LocalDate, List<GameSession>> grouped = gameSessions.stream()
                .sorted(Comparator.comparing(GameSession::getStartDate)
                        .thenComparing(GameSession::getStartTime))
                .collect(Collectors.groupingBy(
                        GameSession::getStartDate,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        
        // Ordenar las sesiones dentro de cada día por hora
        grouped.forEach((date, sessions) -> {
            sessions.sort(Comparator.comparing(GameSession::getStartTime));
            log.debug("Day {}: {} sessions", date, sessions.size());
        });
        
        return grouped;
    }

    // Método para ver detalles de una sesión específica
    @GetMapping("/{id}")
    public String viewGameSession(@PathVariable Long id, Model model, Principal principal) {
        Optional<GameSession> gameSession = gameSessionService.findById(id);
        
        if (gameSession.isPresent()) {
            model.addAttribute("gameSession", gameSession.get());
            
            // Check if current user is registered in the session
            if (principal != null) {
                userService.findByUsername(principal.getName()).ifPresent(user -> {
                    GameSession session = gameSession.get();
                    
                    // Check if user is registered
                    boolean registered = session.getPlayers().stream()
                            .anyMatch(gsp -> gsp.getUser().getId().equals(user.getId()));
                    model.addAttribute("userRegistered", registered);
                });
            }
            
            return "game-sessions/detail";
        } else {
            return "redirect:/game-sessions";
        }
    }

    // Form for creating a new game session
    @GetMapping("/new")
    public String newGameSession(Model model) {
        List<Game> games = gameService.findByAvailable(true);
        
        model.addAttribute("gameSession", new GameSession());
        model.addAttribute("games", games);
        model.addAttribute("editing", false);
        return "game-sessions/form";
    }

    @PostMapping("/new")
    public String createGameSession(@Valid @ModelAttribute("gameSession") GameSession gameSession,
                                   BindingResult result,
                                   @RequestParam(required = false) Long gameId,
                                   @RequestParam String customGameName,
                                   @RequestParam(required = false) String customGameDescription,
                                   @RequestParam(required = false) MultipartFile customGameImage,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
                                   Model model,
                                   RedirectAttributes redirectAttributes,
                                   Principal principal) {
        
        if (result.hasErrors()) {
            model.addAttribute("games", gameService.findAll()); // Mostrar todos los juegos
            model.addAttribute("editing", false);
            return "game-sessions/form";
        }
        
        Optional<User> user = userService.findByUsername(principal.getName());
        
        if (!user.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "User not found");
            return "redirect:/game-sessions/new";
        }
        
        // Validate custom game name
        if (customGameName == null || customGameName.trim().isEmpty()) {
            model.addAttribute("error", "Game name is required");
            model.addAttribute("games", gameService.findAll());
            model.addAttribute("editing", false);
            return "game-sessions/form";
        }
        
        // Validate dates
        if (startDate.isBefore(LocalDate.now())) {
            model.addAttribute("error", "Start date cannot be in the past");
            model.addAttribute("games", gameService.findAll());
            model.addAttribute("editing", false);
            return "game-sessions/form";
        }
        
        if (endDate.isBefore(startDate)) {
            model.addAttribute("error", "End date cannot be before start date");
            model.addAttribute("games", gameService.findAll());
            model.addAttribute("editing", false);
            return "game-sessions/form";
        }
        
        // Validate times if same day session
        if (startDate.equals(endDate) && endTime != null) {
            if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
                model.addAttribute("error", "End time must be after start time for same-day sessions");
                model.addAttribute("games", gameService.findAll());
                model.addAttribute("editing", false);
                return "game-sessions/form";
            }
        }
        
        try {
            // Set basic fields
            gameSession.setCreator(user.get());
            gameSession.setCustomGameName(customGameName.trim());
            gameSession.setCustomGameDescription(customGameDescription);
            gameSession.setStartDate(startDate);
            gameSession.setStartTime(startTime);
            gameSession.setEndDate(endDate);
            gameSession.setEndTime(endTime);
            gameSession.setStatus(GameSessionStatus.SCHEDULED);
            
            // Set library game if selected (CUALQUIER usuario puede usar juegos de biblioteca)
            if (gameId != null) {
                Optional<Game> libraryGame = gameService.findById(gameId);
                if (libraryGame.isPresent()) {
                    gameSession.setGame(libraryGame.get());
                    // YA NO marcamos el juego como no disponible
                    log.info("Library game '{}' assigned to session - availability NOT modified", 
                            libraryGame.get().getName());
                }
            }
            
            // Save session first to get ID
            gameSession = gameSessionService.save(gameSession);
            log.info("Game session created with ID: {}", gameSession.getId());
            
            // Handle custom game image upload for personal games
            if (gameId == null && customGameImage != null && !customGameImage.isEmpty()) {
                try {
                    String imagePath = fileUploadService.uploadGameSessionPhoto(customGameImage, gameSession.getId());
                    gameSession.setCustomGameImagePath(imagePath);
                    gameSession = gameSessionService.save(gameSession);
                    
                    log.info("Custom game image uploaded successfully for session {}: {}", 
                            gameSession.getId(), imagePath);
                            
                } catch (IOException e) {
                    log.error("Error uploading custom game image for session {}: {}", 
                             gameSession.getId(), e.getMessage(), e);
                    redirectAttributes.addFlashAttribute("warning", 
                        "Session created successfully, but there was an error uploading the game image: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    log.error("Invalid image file for session {}: {}", 
                             gameSession.getId(), e.getMessage());
                    redirectAttributes.addFlashAttribute("warning", 
                        "Session created successfully, but the image file was invalid: " + e.getMessage());
                }
            }
            
            // Add creator as first player (automatically confirmed)
            gameSessionService.addPlayerToSession(gameSession.getId(), user.get().getId());
            
            // Log the action
            auditLogService.logChange(
                user.get(),
                ActionType.CREATION,
                "GameSession",
                gameSession.getId(),
                "Game session created: " + gameSession.getTitle() + " for game: " + gameSession.getCustomGameName() +
                (gameSession.hasCustomGameImage() ? " (with custom image)" : "") +
                (gameSession.isLibraryGame() ? " (library game: " + gameSession.getGame().getName() + ")" : "")
            );
            
            redirectAttributes.addFlashAttribute("message", "Game session created successfully");
            return "redirect:/game-sessions/" + gameSession.getId();
        } catch (Exception e) {
            log.error("Error creating game session", e);
            model.addAttribute("error", "An error occurred while creating the session: " + e.getMessage());
            model.addAttribute("games", gameService.findAll());
            model.addAttribute("editing", false);
            return "game-sessions/form";
        }
    }


    // NUEVO: Edit game session form
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isGameSessionCreator(#id, principal.username)")
    public String editGameSession(@PathVariable Long id, Model model) {
        Optional<GameSession> gameSession = gameSessionService.findById(id);
        
        if (gameSession.isPresent()) {
            List<Game> games = gameService.findAll(); // Mostrar TODOS los juegos
            
            model.addAttribute("gameSession", gameSession.get());
            model.addAttribute("games", games);
            model.addAttribute("editing", true);
            return "game-sessions/form";
        } else {
            return "redirect:/game-sessions";
        }
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isGameSessionCreator(#id, principal.username)")
    public String updateGameSession(@PathVariable Long id,
                                   @Valid @ModelAttribute("gameSession") GameSession gameSession,
                                   BindingResult result,
                                   @RequestParam(required = false) Long gameId,
                                   @RequestParam String customGameName,
                                   @RequestParam(required = false) String customGameDescription,
                                   @RequestParam(required = false) MultipartFile customGameImage,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
                                   Model model,
                                   RedirectAttributes redirectAttributes,
                                   Principal principal) {
        
        if (result.hasErrors()) {
            List<Game> games = gameService.findByAvailable(true);
            model.addAttribute("games", games);
            model.addAttribute("editing", true);
            return "game-sessions/form";
        }
        
        Optional<GameSession> existingSessionOpt = gameSessionService.findById(id);
        if (!existingSessionOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Game session not found");
            return "redirect:/game-sessions";
        }
        
        GameSession existingSession = existingSessionOpt.get();
        Optional<User> user = userService.findByUsername(principal.getName());
        
        if (!user.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "User not found");
            return "redirect:/game-sessions/" + id;
        }
        
        // Validate custom game name
        if (customGameName == null || customGameName.trim().isEmpty()) {
            model.addAttribute("error", "Game name is required");
            List<Game> games = gameService.findByAvailable(true);
            model.addAttribute("games", games);
            model.addAttribute("editing", true);
            return "game-sessions/form";
        }
        
        try {
            // Store original game info for cleanup
            Game originalGame = existingSession.getGame();
            String originalImagePath = existingSession.getCustomGameImagePath();
            
            log.info("Updating session {}. Original game: {}, Original image: {}", 
                    id, originalGame != null ? originalGame.getName() : "none", originalImagePath);
            log.info("New gameId: {}, New customGameName: {}", gameId, customGameName);
            
            // Update basic fields
            existingSession.setTitle(gameSession.getTitle());
            existingSession.setCustomGameName(customGameName.trim());
            existingSession.setCustomGameDescription(customGameDescription);
            existingSession.setStartDate(startDate);
            existingSession.setStartTime(startTime);
            existingSession.setEndDate(endDate);
            existingSession.setEndTime(endTime);
            existingSession.setMaxPlayers(gameSession.getMaxPlayers());
            existingSession.setDescription(gameSession.getDescription());
            
            // Handle game change (library game vs personal game)
            if (gameId != null) {
                // User selected a library game
                Optional<Game> newLibraryGame = gameService.findById(gameId);
                if (newLibraryGame.isPresent()) {
                    Game game = newLibraryGame.get();
                    log.info("Setting library game: {}", game.getName());
                    
                    // Set new library game
                    existingSession.setGame(game);
                    if (game.getAvailable()) {
                        game.setAvailable(false);
                        gameService.save(game);
                        log.info("Marked library game as unavailable: {}", game.getName());
                    }
                    
                    // Clear custom image (library games don't use custom images)
                    if (originalImagePath != null) {
                        fileUploadService.deleteGameSessionImage(originalImagePath);
                        existingSession.setCustomGameImagePath(null);
                        log.info("Deleted custom image as session now uses library game");
                    }
                    
                    // Free previous library game if different
                    if (originalGame != null && !originalGame.getId().equals(gameId)) {
                        originalGame.setAvailable(true);
                        gameService.save(originalGame);
                        log.info("Freed previous library game: {}", originalGame.getName());
                    }
                }
            } else {
                // User wants to use personal game
                log.info("Converting to personal game");
                
                // Free library game if it was previously set
                if (originalGame != null) {
                    originalGame.setAvailable(true);
                    gameService.save(originalGame);
                    existingSession.setGame(null);
                    log.info("Freed library game and converted to personal game: {}", originalGame.getName());
                }
                
                // Handle custom image upload for personal games
                if (customGameImage != null && !customGameImage.isEmpty()) {
                    try {
                        // Delete old custom image if exists
                        if (originalImagePath != null) {
                            fileUploadService.deleteGameSessionImage(originalImagePath);
                            log.info("Deleted old custom image: {}", originalImagePath);
                        }
                        
                        // Upload new image
                        String imagePath = fileUploadService.uploadGameSessionPhoto(customGameImage, existingSession.getId());
                        existingSession.setCustomGameImagePath(imagePath);
                        
                        log.info("Custom game image updated for session {}: {}", id, imagePath);
                                
                    } catch (IOException e) {
                        log.error("Error uploading custom game image for session {}: {}", id, e.getMessage(), e);
                        redirectAttributes.addFlashAttribute("warning", 
                            "Session updated successfully, but there was an error uploading the new image: " + e.getMessage());
                    } catch (IllegalArgumentException e) {
                        log.error("Invalid image file for session {}: {}", id, e.getMessage());
                        redirectAttributes.addFlashAttribute("warning", 
                            "Session updated successfully, but the image file was invalid: " + e.getMessage());
                    }
                }
                // If no new image uploaded but switching to personal game, keep existing custom image
            }
            
            // Save the updated session
            existingSession = gameSessionService.save(existingSession);
            log.info("Session {} updated successfully", id);
            
            // Log the action
            auditLogService.logChange(
                user.get(),
                ActionType.MODIFICATION,
                "GameSession",
                existingSession.getId(),
                "Game session updated: " + existingSession.getTitle() + 
                (existingSession.isLibraryGame() ? " (library game: " + existingSession.getGame().getName() + ")" : " (personal game)")
            );
            
            redirectAttributes.addFlashAttribute("message", "Game session updated successfully");
            return "redirect:/game-sessions/" + existingSession.getId();
            
        } catch (Exception e) {
            log.error("Error updating game session", e);
            model.addAttribute("error", "An error occurred while updating the session: " + e.getMessage());
            List<Game> games = gameService.findByAvailable(true);
            model.addAttribute("games", games);
            model.addAttribute("editing", true);
            return "game-sessions/form";
        }
    }

    // Join a game session
    @PostMapping("/{id}/join")
    public String joinGameSession(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes,
                                 Principal principal) {
        
        userService.findByUsername(principal.getName()).ifPresent(user -> {
            boolean added = gameSessionService.addPlayerToSession(id, user.getId());
            
            if (added) {
                redirectAttributes.addFlashAttribute("message", "Successfully joined the game session");
                
                // Log the action
                gameSessionService.findById(id).ifPresent(session -> 
                    auditLogService.logChange(
                        user,
                        ActionType.MODIFICATION,
                        "GameSession",
                        id,
                        "Joined game session: " + session.getTitle()
                    )
                );
            } else {
                redirectAttributes.addFlashAttribute("error", "Could not join the game session. Session may be full or you're already registered.");
            }
        });
        
        return "redirect:/game-sessions/" + id;
    }

    // Leave a game session
    @PostMapping("/{id}/leave")
    public String leaveGameSession(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes,
                                  Principal principal) {
        
        userService.findByUsername(principal.getName()).ifPresent(user -> {
            boolean removed = gameSessionService.removePlayerFromSession(id, user.getId());
            
            if (removed) {
                redirectAttributes.addFlashAttribute("message", "Successfully left the game session");
                
                // Log the action
                gameSessionService.findById(id).ifPresent(session -> 
                    auditLogService.logChange(
                        user,
                        ActionType.MODIFICATION,
                        "GameSession",
                        id,
                        "Left game session: " + session.getTitle()
                    )
                );
            } else {
                redirectAttributes.addFlashAttribute("error", "Could not leave the game session. You may not be registered.");
            }
        });
        
        return "redirect:/game-sessions/" + id;
    }

    // Delete a game session
    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isGameSessionCreator(#id, principal.username)")
    public String deleteGameSession(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes,
                                   Principal principal) {
        
        Optional<GameSession> sessionOpt = gameSessionService.findById(id);
        Optional<User> userOpt = userService.findByUsername(principal.getName());
        
        if (sessionOpt.isPresent() && userOpt.isPresent()) {
            GameSession gameSession = sessionOpt.get();
            User user = userOpt.get();
            String sessionTitle = gameSession.getTitle();
            
            // Delete custom image if exists
            if (gameSession.hasCustomGameImage()) {
                try {
                    fileUploadService.deleteFile(gameSession.getCustomGameImagePath());
                    log.info("Custom image deleted for session {}: {}", id, gameSession.getCustomGameImagePath());
                } catch (Exception e) {
                    log.warn("Could not delete custom image for session {}: {}", id, e.getMessage());
                }
            }
            
            // YA NO liberamos el juego de biblioteca al eliminar la sesión
            if (gameSession.getGame() != null) {
                log.info("Session used library game '{}' - availability NOT modified on deletion", 
                        gameSession.getGame().getName());
            }
            
            gameSessionService.deleteById(id);
            
            // Log the action
            auditLogService.logChange(
                user,
                ActionType.DELETION,
                "GameSession",
                id,
                "Game session deleted: " + sessionTitle
            );
            
            redirectAttributes.addFlashAttribute("message", "Game session deleted successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Game session or user not found");
        }
        
        return "redirect:/game-sessions";
    }

    // View today's sessions
    @GetMapping("/today")
    public String viewTodaySessions(Model model) {
        List<GameSession> sessions = gameSessionService.findTodaySessions();
        Map<LocalDate, List<GameSession>> sessionsByDay = groupSessionsByDay(sessions);
        
        model.addAttribute("sessionsByDay", sessionsByDay);
        model.addAttribute("gameSessions", sessions);
        model.addAttribute("listTitle", "Today's Sessions");
        return "game-sessions/list";
    }
    
    // View upcoming sessions
    @GetMapping("/upcoming")
    public String viewUpcomingSessions(Model model) {
        List<GameSession> sessions = gameSessionService.findUpcomingSessions();
        Map<LocalDate, List<GameSession>> sessionsByDay = groupSessionsByDay(sessions);
        
        model.addAttribute("sessionsByDay", sessionsByDay);
        model.addAttribute("gameSessions", sessions);
        model.addAttribute("listTitle", "Upcoming Sessions");
        return "game-sessions/list";
    }
    
    // View my created sessions
    @GetMapping("/my-sessions")
    public String viewMySessions(Model model, Principal principal) {
        userService.findByUsername(principal.getName()).ifPresent(user -> {
            List<GameSession> sessions = gameSessionService.findByCreator(user);
            Map<LocalDate, List<GameSession>> sessionsByDay = groupSessionsByDay(sessions);
            
            model.addAttribute("sessionsByDay", sessionsByDay);
            model.addAttribute("gameSessions", sessions);
            model.addAttribute("listTitle", "My Created Sessions");
        });
        
        return "game-sessions/list";
    }
    
    // View sessions I'm participating in
    @GetMapping("/my-participations")
    public String viewMyParticipations(Model model, Principal principal) {
        userService.findByUsername(principal.getName()).ifPresent(user -> {
            List<GameSession> sessions = gameSessionService.findSessionsByPlayer(user.getId());
            Map<LocalDate, List<GameSession>> sessionsByDay = groupSessionsByDay(sessions);
            
            model.addAttribute("sessionsByDay", sessionsByDay);
            model.addAttribute("gameSessions", sessions);
            model.addAttribute("listTitle", "My Participations");
        });
        
        return "game-sessions/list";
    }
}