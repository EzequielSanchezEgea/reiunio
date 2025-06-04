package com.ezequiel.reiunio.controller;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ezequiel.reiunio.entity.Game;
import com.ezequiel.reiunio.entity.GameSession;
import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.ActionType;
import com.ezequiel.reiunio.enums.GameSessionStatus;
import com.ezequiel.reiunio.service.AuditLogService;
import com.ezequiel.reiunio.service.FileUploadService;
import com.ezequiel.reiunio.service.GameService;
import com.ezequiel.reiunio.service.GameSessionService;
import com.ezequiel.reiunio.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller responsible for managing game session operations in the Reiunio application.
 * This controller handles the complete lifecycle of game sessions including creation, modification,
 * deletion, and participation management. It provides advanced session organization features
 * such as filtering, grouping by date, and player registration management.
 * 
 * <p>Key features include:</p>
 * <ul>
 *   <li>Game session creation with support for both library games and personal games</li>
 *   <li>Custom game image upload for personal game sessions</li>
 *   <li>Session filtering by date, status, and user participation</li>
 *   <li>Automatic session grouping by date for improved organization</li>
 *   <li>Player registration and unregistration management</li>
 *   <li>Role-based access control for session management</li>
 *   <li>Comprehensive audit logging for all operations</li>
 *   <li>AJAX endpoints for dynamic game information retrieval</li>
 * </ul>
 * 
 * <p>The controller supports two types of game sessions:</p>
 * <ul>
 *   <li><strong>Library Game Sessions:</strong> Using games from the central library</li>
 *   <li><strong>Personal Game Sessions:</strong> Using custom games with user-uploaded images</li>
 * </ul>
 * 
 * <p>Security is enforced through Spring Security annotations, ensuring that users can only
 * modify sessions they created or that administrators can manage all sessions.</p>
 * 
 * @author Ezequiel
 * @version 1.0
 * @since 1.0
 */
@Controller
@RequestMapping("/game-sessions")
@RequiredArgsConstructor
@Slf4j
public class GameSessionController {

    /** Service for game session operations and data management */
    private final GameSessionService gameSessionService;
    
    /** Service for game-related operations and library management */
    private final GameService gameService;
    
    /** Service for user-related operations and authentication */
    private final UserService userService;
    
    /** Service for logging audit events and user actions */
    private final AuditLogService auditLogService;
    
    /** Service for handling file upload operations */
    private final FileUploadService fileUploadService;

    /**
     * Displays a filtered and organized list of game sessions grouped by date.
     * This main endpoint provides comprehensive filtering capabilities and automatically
     * organizes sessions by day for improved readability and navigation.
     * 
     * <p>Available filter options include:</p>
     * <ul>
     *   <li><strong>today:</strong> Sessions scheduled for today</li>
     *   <li><strong>upcoming:</strong> Future sessions</li>
     *   <li><strong>registered:</strong> Sessions where the current user is registered</li>
     *   <li><strong>finished:</strong> Completed sessions</li>
     *   <li><strong>null/empty:</strong> All sessions</li>
     * </ul>
     * 
     * <p>Sessions are automatically grouped by date and sorted chronologically within each day.
     * This organization helps users quickly find sessions for specific dates and understand
     * the temporal distribution of gaming activities.</p>
     * 
     * @param model the Spring MVC model for passing data to the view
     * @param filter optional filter parameter to narrow down the session list
     * @param principal the currently authenticated user's security principal
     * @return the name of the game sessions list view template
     */
    @GetMapping
    public String listGameSessions(Model model, 
                                  @RequestParam(required = false) String filter,
                                  Principal principal) {
        List<GameSession> gameSessions;
        String listTitle = "Game Sessions";
        
        log.debug("Listing game sessions with filter: {}", filter);
        
        // Apply filters based on parameter
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
        
        // Group sessions by day
        Map<LocalDate, List<GameSession>> sessionsByDay = groupSessionsByDay(gameSessions);
        log.debug("Grouped sessions into {} days", sessionsByDay.size());
        
        model.addAttribute("sessionsByDay", sessionsByDay);
        model.addAttribute("gameSessions", gameSessions); // Maintain for compatibility
        model.addAttribute("listTitle", listTitle);
        
        return "game-sessions/list";
    }

    /**
     * Retrieves the game sessions where the current user is registered as a player.
     * This private helper method is used to filter sessions based on user participation,
     * providing personalized views for users to track their gaming commitments.
     * 
     * @param principal the currently authenticated user's security principal
     * @return a list of game sessions where the user is registered, or empty list if user not found
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
     * Groups game sessions by their start date for organized display.
     * This method creates a chronologically ordered map where sessions are grouped by date
     * and sorted by time within each day. This organization improves user experience
     * by presenting sessions in a calendar-like structure.
     * 
     * <p>The grouping process:</p>
     * <ul>
     *   <li>Sessions are first sorted by start date, then by start time</li>
     *   <li>Sessions are grouped into a LinkedHashMap to preserve order</li>
     *   <li>Within each day, sessions are sorted by start time</li>
     *   <li>Empty input returns an empty LinkedHashMap</li>
     * </ul>
     * 
     * @param gameSessions the list of game sessions to group
     * @return a LinkedHashMap with dates as keys and lists of sessions as values,
     *         ordered chronologically
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
        
        // Sort sessions within each day by time
        grouped.forEach((date, sessions) -> {
            sessions.sort(Comparator.comparing(GameSession::getStartTime));
            log.debug("Day {}: {} sessions", date, sessions.size());
        });
        
        return grouped;
    }

    /**
     * Displays the detailed view of a specific game session.
     * This endpoint shows comprehensive information about a single game session including
     * all participants, game details, timing, and the current user's registration status.
     * 
     * <p>The view includes:</p>
     * <ul>
     *   <li>Complete session information (title, description, timing)</li>
     *   <li>Game details (library game or custom game information)</li>
     *   <li>List of registered players</li>
     *   <li>User's registration status for conditional UI elements</li>
     *   <li>Custom game image if applicable</li>
     * </ul>
     * 
     * @param id the unique identifier of the game session to display
     * @param model the Spring MVC model for passing data to the view
     * @param principal the currently authenticated user's security principal
     * @return the name of the game session detail view template, or redirect to sessions list if not found
     */
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

    /**
     * Displays the form for creating a new game session.
     * This endpoint provides a form with all necessary fields for session creation,
     * including available library games for selection.
     * 
     * @param model the Spring MVC model for passing data to the view
     * @return the name of the game session form view template
     */
    @GetMapping("/new")
    public String newGameSession(Model model) {
        List<Game> games = gameService.findByAvailable(true);
        
        model.addAttribute("gameSession", new GameSession());
        model.addAttribute("games", games);
        model.addAttribute("editing", false);
        return "game-sessions/form";
    }

    /**
     * Handles the creation of a new game session with comprehensive validation and support
     * for both library games and personal games with custom images.
     * 
     * <p>The creation process includes:</p>
     * <ul>
     *   <li>Form validation with comprehensive error handling</li>
     *   <li>Date and time validation to prevent invalid schedules</li>
     *   <li>Library game assignment (optional)</li>
     *   <li>Custom game image upload for personal games</li>
     *   <li>Automatic creator registration as first player</li>
     *   <li>Audit logging of the creation action</li>
     * </ul>
     * 
     * <p>Validation rules include:</p>
     * <ul>
     *   <li>Custom game name is required</li>
     *   <li>Start date cannot be in the past</li>
     *   <li>End date cannot be before start date</li>
     *   <li>For same-day sessions, end time must be after start time</li>
     * </ul>
     * 
     * <p>Note: Library games can be used by any user without affecting game availability.</p>
     * 
     * @param gameSession the game session entity to be created, validated through Spring Validation
     * @param result the binding result containing validation errors, if any
     * @param gameId optional ID of library game to associate with the session
     * @param customGameName required name for the custom game
     * @param customGameDescription optional description for the custom game
     * @param customGameImage optional multipart file containing custom game image
     * @param startDate the session start date
     * @param startTime the session start time
     * @param endDate the session end date
     * @param endTime optional session end time
     * @param model the Spring MVC model for passing data to the view
     * @param redirectAttributes Spring MVC attributes for passing messages between requests
     * @param principal the currently authenticated user's security principal
     * @return redirect to session detail on success, or form view on validation errors
     * 
     * @throws IOException if there's an error during custom image upload
     * @throws IllegalArgumentException if the uploaded image file is invalid
     */
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
            model.addAttribute("games", gameService.findAll()); // Show all games
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
            
            // Set library game if selected (ANY user can use library games)
            if (gameId != null) {
                Optional<Game> libraryGame = gameService.findById(gameId);
                if (libraryGame.isPresent()) {
                    gameSession.setGame(libraryGame.get());
                    // NO longer mark game as unavailable
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

    /**
     * Displays the form for editing an existing game session.
     * This endpoint is restricted to session creators and administrators, and pre-populates
     * the form with current session data. It handles the special case of including
     * currently selected library games even if they're marked as unavailable.
     * 
     * @param id the unique identifier of the game session to edit
     * @param model the Spring MVC model for passing data to the view
     * @return the name of the game session form view template, or redirect to sessions list if not found
     */
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isGameSessionCreator(#id, principal.username)")
    public String editGameSession(@PathVariable Long id, Model model) {
        Optional<GameSession> gameSession = gameSessionService.findById(id);
        
        if (gameSession.isPresent()) {
            GameSession session = gameSession.get();
            
            // Get available games
            List<Game> availableGames = gameService.findByAvailable(true);
            
            // If the session currently uses a library game, add it to the list 
            // even if it's not available (so it appears selected)
            if (session.getGame() != null) {
                Game currentGame = session.getGame();
                // Only add if not already in the available list
                boolean isAlreadyInList = availableGames.stream()
                        .anyMatch(game -> game.getId().equals(currentGame.getId()));
                
                if (!isAlreadyInList) {
                    // Create a new mutable list and add the current game
                    availableGames = new ArrayList<>(availableGames);
                    availableGames.add(currentGame);
                    
                    // Sort the list by name to maintain consistency
                    availableGames.sort(Comparator.comparing(Game::getName));
                }
            }
            
            model.addAttribute("gameSession", session);
            model.addAttribute("games", availableGames);
            model.addAttribute("editing", true);
            return "game-sessions/form";
        } else {
            return "redirect:/game-sessions";
        }
    }

    /**
     * Handles the update of an existing game session with comprehensive support for
     * switching between library games and personal games, including image management.
     * 
     * <p>The update process includes:</p>
     * <ul>
     *   <li>Form validation with error handling</li>
     *   <li>Game type switching (library to personal or vice versa)</li>
     *   <li>Custom image management for personal games</li>
     *   <li>Library game availability management</li>
     *   <li>Preservation of existing data where appropriate</li>
     *   <li>Audit logging of modifications</li>
     * </ul>
     * 
     * <p>Special handling for game type changes:</p>
     * <ul>
     *   <li>Library to personal: Frees the library game and allows custom image upload</li>
     *   <li>Personal to library: Deletes custom image and assigns library game</li>
     *   <li>Library to library: Manages availability between old and new games</li>
     * </ul>
     * 
     * @param id the unique identifier of the game session to update
     * @param gameSession the updated game session entity with new values
     * @param result the binding result containing validation errors, if any
     * @param gameId optional ID of library game to associate with the session
     * @param customGameName required name for the custom game
     * @param customGameDescription optional description for the custom game
     * @param customGameImage optional multipart file containing new custom game image
     * @param startDate the updated session start date
     * @param startTime the updated session start time
     * @param endDate the updated session end date
     * @param endTime optional updated session end time
     * @param model the Spring MVC model for passing data to the view
     * @param redirectAttributes Spring MVC attributes for passing messages between requests
     * @param principal the currently authenticated user's security principal
     * @return redirect to session detail on success, or form view on validation errors
     * 
     * @throws IOException if there's an error during custom image upload
     * @throws IllegalArgumentException if the uploaded image file is invalid
     */
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

    /**
     * Handles user registration to join a game session.
     * This endpoint allows authenticated users to register themselves as players
     * in a game session, subject to availability and session constraints.
     * 
     * <p>The registration process includes:</p>
     * <ul>
     *   <li>Session capacity validation</li>
     *   <li>Duplicate registration prevention</li>
     *   <li>Player addition to session</li>
     *   <li>Audit logging of the join action</li>
     *   <li>User feedback through flash messages</li>
     * </ul>
     * 
     * @param id the unique identifier of the game session to join
     * @param redirectAttributes Spring MVC attributes for passing messages between requests
     * @param principal the currently authenticated user's security principal
     * @return redirect to session detail page with appropriate success or error message
     */
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

    /**
     * Handles user unregistration from a game session.
     * This endpoint allows authenticated users to remove themselves from a game session
     * they have previously joined, with appropriate validations and logging.
     * 
     * <p>The unregistration process includes:</p>
     * <ul>
     *   <li>Registration status validation</li>
     *   <li>Player removal from session</li>
     *   <li>Audit logging of the leave action</li>
     *   <li>User feedback through flash messages</li>
     * </ul>
     * 
     * @param id the unique identifier of the game session to leave
     * @param redirectAttributes Spring MVC attributes for passing messages between requests
     * @param principal the currently authenticated user's security principal
     * @return redirect to session detail page with appropriate success or error message
     */
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

    /**
     * Handles the deletion of a game session with proper cleanup and validation.
     * This endpoint is restricted to session creators and administrators, and performs
     * comprehensive cleanup including custom image deletion and audit logging.
     * 
     * <p>The deletion process includes:</p>
     * <ul>
     *   <li>Session existence validation</li>
     *   <li>Custom image file cleanup if applicable</li>
     *   <li>Session entity deletion from database</li>
     *   <li>Audit logging of the deletion action</li>
     *   <li>User feedback through flash messages</li>
     * </ul>
     * 
     * <p>Note: Library games are no longer freed automatically upon session deletion
     * to maintain consistency with the new availability management approach.</p>
     * 
     * @param id the unique identifier of the game session to delete
     * @param redirectAttributes Spring MVC attributes for passing messages between requests
     * @param principal the currently authenticated user's security principal
     * @return redirect to sessions list with appropriate success or error message
     */
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
            
            // NO longer free library game upon session deletion
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

    /**
     * Displays today's game sessions in an organized view.
     * This convenience endpoint filters sessions to show only those scheduled for today,
     * automatically grouped by date for easy navigation.
     * 
     * @param model the Spring MVC model for passing data to the view
     * @return the name of the game sessions list view template with today's sessions
     */
    @GetMapping("/today")
    public String viewTodaySessions(Model model) {
        List<GameSession> sessions = gameSessionService.findTodaySessions();
        Map<LocalDate, List<GameSession>> sessionsByDay = groupSessionsByDay(sessions);
        
        model.addAttribute("sessionsByDay", sessionsByDay);
        model.addAttribute("gameSessions", sessions);
        model.addAttribute("listTitle", "Today's Sessions");
        return "game-sessions/list";
    }
    
    /**
     * Displays upcoming game sessions in an organized view.
     * This convenience endpoint filters sessions to show only future sessions,
     * automatically grouped by date for improved planning and navigation.
     * 
     * @param model the Spring MVC model for passing data to the view
     * @return the name of the game sessions list view template with upcoming sessions
     */
    @GetMapping("/upcoming")
    public String viewUpcomingSessions(Model model) {
        List<GameSession> sessions = gameSessionService.findUpcomingSessions();
        Map<LocalDate, List<GameSession>> sessionsByDay = groupSessionsByDay(sessions);
        
        model.addAttribute("sessionsByDay", sessionsByDay);
        model.addAttribute("gameSessions", sessions);
        model.addAttribute("listTitle", "Upcoming Sessions");
        return "game-sessions/list";
    }
    
    /**
     * Displays game sessions created by the current user.
     * This personalized view shows only sessions where the current user is the creator,
     * allowing for easy management of their organized gaming events.
     * 
     * @param model the Spring MVC model for passing data to the view
     * @param principal the currently authenticated user's security principal
     * @return the name of the game sessions list view template with user's created sessions
     */
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
    
    /**
     * Displays game sessions where the current user is registered as a participant.
     * This personalized view shows all sessions where the user has registered to play,
     * providing a convenient overview of their gaming commitments.
     * 
     * @param model the Spring MVC model for passing data to the view
     * @param principal the currently authenticated user's security principal
     * @return the name of the game sessions list view template with user's participations
     */
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

    /**
     * AJAX endpoint for retrieving comprehensive game information including image data.
     * This endpoint provides dynamic game information for form interactions, allowing
     * the frontend to display game details without page refreshes.
     * 
     * <p>The response includes complete game information such as:</p>
     * <ul>
     *   <li>Game name and description</li>
     *   <li>Player count recommendations</li>
     *   <li>Duration information</li>
     *   <li>Game image path if available</li>
     *   <li>Current availability status</li>
     * </ul>
     * 
     * @param gameId the unique identifier of the game to retrieve information for
     * @return a ResponseEntity containing a GameInfoResponse with game details or error information
     */
    @GetMapping("/api/game-info/{gameId}")
    @ResponseBody
    public ResponseEntity<GameInfoResponse> getGameInfo(@PathVariable Long gameId) {
        try {
            Optional<Game> gameOpt = gameService.findById(gameId);
            
            if (!gameOpt.isPresent()) {
                return ResponseEntity.badRequest().body(new GameInfoResponse(false, "Game not found", null));
            }
            
            Game game = gameOpt.get();
            return ResponseEntity.ok(new GameInfoResponse(true, "Game information loaded successfully", game));
            
        } catch (Exception e) {
            log.error("Error getting game info for gameId: " + gameId, e);
            return ResponseEntity.internalServerError().body(new GameInfoResponse(false, "Error loading game information", null));
        }
    }

    /**
     * Data Transfer Object representing the response for AJAX game information requests.
     * This class encapsulates the result of game information retrieval attempts, providing
     * both success status and game details for client-side processing.
     * 
     * <p>The response includes:</p>
     * <ul>
     *   <li>Success flag indicating if the operation completed successfully</li>
     *   <li>Message providing details about the operation result</li>
     *   <li>Complete game object for successful requests (null for failures)</li>
     * </ul>
     * 
     * @since 1.0
     */
    public static class GameInfoResponse {
        /** Indicates whether the game information retrieval was successful */
        private boolean success;
        
        /** Human-readable message describing the operation result */
        private String message;
        
        /** Complete game object containing all game details, null if retrieval failed */
        private Game game;
        
        /**
         * Constructs a new GameInfoResponse with the specified parameters.
         * 
         * @param success true if the retrieval was successful, false otherwise
         * @param message descriptive message about the operation result
         * @param game complete game object, or null if retrieval failed
         */
        public GameInfoResponse(boolean success, String message, Game game) {
            this.success = success;
            this.message = message;
            this.game = game;
        }
        
        /**
         * Returns whether the game information retrieval was successful.
         * 
         * @return true if successful, false otherwise
         */
        public boolean isSuccess() { 
            return success; 
        }
        
        /**
         * Returns the descriptive message about the operation result.
         * 
         * @return the operation result message
         */
        public String getMessage() { 
            return message; 
        }
        
        /**
         * Returns the complete game object with all details.
         * 
         * @return the game object, or null if retrieval failed
         */
        public Game getGame() { 
            return game; 
        }
    }
}