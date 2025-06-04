package com.ezequiel.reiunio.controller;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import com.ezequiel.reiunio.entity.Loan;
import com.ezequiel.reiunio.enums.ActionType;
import com.ezequiel.reiunio.enums.GameState;
import com.ezequiel.reiunio.service.AuditLogService;
import com.ezequiel.reiunio.service.FileUploadService;
import com.ezequiel.reiunio.service.GameService;
import com.ezequiel.reiunio.service.GameSessionService;
import com.ezequiel.reiunio.service.LoanService;
import com.ezequiel.reiunio.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller responsible for managing game-related operations in the Reiunio application.
 * This controller handles the complete lifecycle of board games including creation, modification,
 * deletion, and viewing operations. It provides comprehensive game library management with
 * advanced filtering, pagination, and search capabilities.
 * 
 * <p>Key features include:</p>
 * <ul>
 *   <li>Paginated game listing with multiple filter options</li>
 *   <li>Game creation and editing with photo upload support</li>
 *   <li>Game deletion with referential integrity checks</li>
 *   <li>Availability management for game lending</li>
 *   <li>Comprehensive audit logging for all operations</li>
 *   <li>Role-based access control for administrative functions</li>
 * </ul>
 * 
 * <p>Security is enforced through Spring Security annotations, with most administrative
 * operations restricted to users with ADMIN role. All operations are logged through
 * the audit system for compliance and tracking purposes.</p>
 * 
 * @author Ezequiel
 * @version 1.0
 * @since 1.0
 */
@Controller
@RequestMapping("/games")
@RequiredArgsConstructor
@Slf4j
public class GameController {

    /** Service for game-related operations and data management */
    private final GameService gameService;
    
    /** Service for user-related operations and authentication */
    private final UserService userService;
    
    /** Service for logging audit events and user actions */
    private final AuditLogService auditLogService;
    
    /** Service for game session operations and management */
    private final GameSessionService gameSessionService;
    
    /** Service for loan operations and tracking */
    private final LoanService loanService;
    
    /** Service for handling file upload operations */
    private final FileUploadService fileUploadService;

    /**
     * Displays a paginated and filtered list of games in the library.
     * This endpoint provides comprehensive filtering and sorting capabilities to help users
     * find specific games based on various criteria.
     * 
     * <p>Available filter options include:</p>
     * <ul>
     *   <li>Name - partial text matching</li>
     *   <li>Category - exact category matching</li>
     *   <li>Availability - whether the game is available for loan</li>
     *   <li>State - physical condition of the game</li>
     *   <li>Player count - minimum and maximum number of players</li>
     *   <li>Duration - minimum and maximum game duration in minutes</li>
     * </ul>
     * 
     * <p>The results are paginated with configurable page size and support multiple
     * sorting options. Filter values are preserved in the model for form persistence
     * across requests.</p>
     * 
     * @param model the Spring MVC model for passing data to the view
     * @param name optional filter for game name (partial matching)
     * @param category optional filter for exact category matching
     * @param available optional filter for game availability status
     * @param state optional filter for game physical condition
     * @param minPlayers optional filter for minimum number of players
     * @param maxPlayers optional filter for maximum number of players
     * @param minDuration optional filter for minimum game duration in minutes
     * @param maxDuration optional filter for maximum game duration in minutes
     * @param page the page number for pagination (0-based, defaults to 0)
     * @param size the number of items per page (defaults to 20)
     * @param sortBy the field to sort by (defaults to "name")
     * @param sortDir the sort direction: "asc" or "desc" (defaults to "asc")
     * @return the name of the games list view template
     */
    @GetMapping
    public String listGames(Model model, 
                           @RequestParam(required = false) String name,
                           @RequestParam(required = false) String category,
                           @RequestParam(required = false) Boolean available,
                           @RequestParam(required = false) GameState state,
                           @RequestParam(required = false) Integer minPlayers,
                           @RequestParam(required = false) Integer maxPlayers,
                           @RequestParam(required = false) Integer minDuration,
                           @RequestParam(required = false) Integer maxDuration,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "20") int size,
                           @RequestParam(defaultValue = "name") String sortBy,
                           @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.debug("Listing games with filters - name: {}, category: {}, available: {}, state: {}, page: {}, size: {}", 
                 name, category, available, state, page, size);
        
        // Create pageable with sorting
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Get paginated games with filters
        Page<Game> gamesPage = gameService.findAllWithFilters(
            name, category, available, state, 
            minPlayers, maxPlayers, minDuration, maxDuration, 
            pageable);
        
        // Add pagination attributes
        model.addAttribute("games", gamesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", gamesPage.getTotalPages());
        model.addAttribute("totalElements", gamesPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        
        // Add filter attributes for form persistence
        model.addAttribute("filterName", name);
        model.addAttribute("filterCategory", category);
        model.addAttribute("filterAvailable", available);
        model.addAttribute("filterState", state);
        model.addAttribute("filterMinPlayers", minPlayers);
        model.addAttribute("filterMaxPlayers", maxPlayers);
        model.addAttribute("filterMinDuration", minDuration);
        model.addAttribute("filterMaxDuration", maxDuration);
        
        // Get distinct categories for dropdown
        List<String> categories = gameService.findDistinctCategories();
        model.addAttribute("categories", categories);
        
        // Add game states enum
        model.addAttribute("gameStates", GameState.values());
        
        return "games/list";
    }

    /**
     * Displays the detailed view of a specific game.
     * This endpoint shows comprehensive information about a single game including
     * all its properties, current status, and associated image if available.
     * 
     * @param id the unique identifier of the game to display
     * @param model the Spring MVC model for passing data to the view
     * @return the name of the game detail view template, or redirect to games list if not found
     */
    @GetMapping("/{id}")
    public String viewGame(@PathVariable Long id, Model model) {
        Optional<Game> game = gameService.findById(id);
        
        if (game.isPresent()) {
            model.addAttribute("game", game.get());
            return "games/detail";
        } else {
            return "redirect:/games";
        }
    }

    /**
     * Displays the form for creating a new game.
     * This endpoint is restricted to administrators and provides a form with all
     * necessary fields for game creation, including game state options.
     * 
     * @param model the Spring MVC model for passing data to the view
     * @return the name of the game form view template
     */
    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newGameForm(Model model) {
        model.addAttribute("game", new Game());
        model.addAttribute("states", GameState.values());
        return "games/form";
    }

    /**
     * Handles the creation of a new game with optional photo upload.
     * This endpoint processes the game creation form, validates the input data,
     * and optionally handles photo upload. The game is automatically marked as
     * available and assigned the current date as acquisition date.
     * 
     * <p>The creation process includes:</p>
     * <ul>
     *   <li>Form validation with error handling</li>
     *   <li>Game entity creation and initial save</li>
     *   <li>Optional photo upload and processing</li>
     *   <li>Audit logging of the creation action</li>
     *   <li>User feedback through flash messages</li>
     * </ul>
     * 
     * <p>If photo upload fails, the game is still created successfully, but a warning
     * message is displayed to inform the user about the photo upload issue.</p>
     * 
     * @param game the game entity to be created, validated through Spring Validation
     * @param result the binding result containing validation errors, if any
     * @param gamePhoto optional multipart file containing the game photo
     * @param model the Spring MVC model for passing data to the view
     * @param redirectAttributes Spring MVC attributes for passing messages between requests
     * @param principal the currently authenticated user's security principal
     * @return redirect to games list on success, or form view on validation errors
     * 
     * @throws IOException if there's an error during photo upload
     * @throws IllegalArgumentException if the uploaded photo file is invalid
     */
    @PostMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String createGame(@Valid @ModelAttribute("game") Game game, 
                            BindingResult result, 
                            @RequestParam(value = "gamePhoto", required = false) MultipartFile gamePhoto,
                            Model model, 
                            RedirectAttributes redirectAttributes, 
                            Principal principal) {
        if (result.hasErrors()) {
            model.addAttribute("states", GameState.values());
            return "games/form";
        }
        
        game.setAcquisitionDate(LocalDate.now());
        game.setAvailable(true);
        
        // Save the game first to get an ID
        Game savedGame = gameService.save(game);
        final Game finalSavedGame = savedGame; // Create a final copy for use in lambda
        
        // Handle photo upload if provided
        if (gamePhoto != null && !gamePhoto.isEmpty()) {
            try {
                String imagePath = fileUploadService.uploadGamePhoto(gamePhoto, finalSavedGame.getId());
                finalSavedGame.setImagePath(imagePath);
                gameService.save(finalSavedGame);
                
                log.info("Game photo uploaded successfully for game {}: {}", 
                        finalSavedGame.getId(), imagePath);
                        
            } catch (IOException e) {
                log.error("Error uploading game photo for game {}: {}", 
                         finalSavedGame.getId(), e.getMessage(), e);
                redirectAttributes.addFlashAttribute("warning", 
                    "Game created successfully, but there was an error uploading the photo: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                log.error("Invalid photo file for game {}: {}", 
                         finalSavedGame.getId(), e.getMessage());
                redirectAttributes.addFlashAttribute("warning", 
                    "Game created successfully, but the photo file was invalid: " + e.getMessage());
            }
        }
        
        // Log the action
        userService.findByUsername(principal.getName()).ifPresent(user -> 
            auditLogService.logChange(user, ActionType.CREATION, "Game", finalSavedGame.getId(), 
                "Game created: " + finalSavedGame.getName() + 
                (finalSavedGame.hasCustomImage() ? " (with photo)" : ""))
        );
        
        String successMessage = "Game created successfully";
        if (finalSavedGame.hasCustomImage()) {
            successMessage += " with photo";
        }
        redirectAttributes.addFlashAttribute("message", successMessage);
        
        return "redirect:/games";
    }

    /**
     * Displays the form for editing an existing game.
     * This endpoint is restricted to administrators and pre-populates the form
     * with the current game data for modification.
     * 
     * @param id the unique identifier of the game to edit
     * @param model the Spring MVC model for passing data to the view
     * @return the name of the game form view template, or redirect to games list if not found
     */
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editGameForm(@PathVariable Long id, Model model) {
        Optional<Game> game = gameService.findById(id);
        
        if (game.isPresent()) {
            model.addAttribute("game", game.get());
            model.addAttribute("states", GameState.values());
            model.addAttribute("editing", true);
            return "games/form";
        } else {
            return "redirect:/games";
        }
    }

    /**
     * Handles the update of an existing game.
     * This endpoint processes the game modification form while preserving certain
     * immutable properties like acquisition date and image path.
     * 
     * <p>The update process includes:</p>
     * <ul>
     *   <li>Form validation with error handling</li>
     *   <li>Preservation of original acquisition date and image path</li>
     *   <li>Game entity update and save</li>
     *   <li>Audit logging of the modification action</li>
     *   <li>User feedback through flash messages</li>
     * </ul>
     * 
     * <p>Note: Photo updates are handled through separate endpoints in the FileUploadController.</p>
     * 
     * @param id the unique identifier of the game to update
     * @param game the updated game entity with new values
     * @param result the binding result containing validation errors, if any
     * @param model the Spring MVC model for passing data to the view
     * @param redirectAttributes Spring MVC attributes for passing messages between requests
     * @param principal the currently authenticated user's security principal
     * @return redirect to games list on success, or form view on validation errors
     */
    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateGame(@PathVariable Long id, 
                            @Valid @ModelAttribute("game") Game game,
                            BindingResult result, 
                            Model model, 
                            RedirectAttributes redirectAttributes,
                            Principal principal) {
        if (result.hasErrors()) {
            model.addAttribute("states", GameState.values());
            model.addAttribute("editing", true);
            return "games/form";
        }
        
        Optional<Game> existingGame = gameService.findById(id);
        
        if (existingGame.isPresent()) {
            Game currentGame = existingGame.get();
            
            // Keep original acquisition date and image path
            game.setAcquisitionDate(currentGame.getAcquisitionDate());
            game.setImagePath(currentGame.getImagePath());
            
            Game savedGame = gameService.save(game);
            final Game finalSavedGame = savedGame; // Create a final copy for use in lambda
            
            // Log the action
            userService.findByUsername(principal.getName()).ifPresent(user -> 
                auditLogService.logChange(user, ActionType.MODIFICATION, "Game", finalSavedGame.getId(), 
                    "Game modified: " + finalSavedGame.getName())
            );
            
            redirectAttributes.addFlashAttribute("message", "Game updated successfully");
            return "redirect:/games";
        } else {
            return "redirect:/games";
        }
    }

    /**
     * Handles the deletion of a game with referential integrity checks.
     * This endpoint performs comprehensive validation to ensure that games with
     * associated sessions or loans cannot be deleted, maintaining data integrity.
     * 
     * <p>The deletion process includes:</p>
     * <ul>
     *   <li>Validation of game existence</li>
     *   <li>Referential integrity checks for game sessions and loans</li>
     *   <li>Photo file deletion if applicable</li>
     *   <li>Game entity deletion from database</li>
     *   <li>Audit logging of the deletion action</li>
     *   <li>Appropriate user feedback based on operation result</li>
     * </ul>
     * 
     * <p>If the game has associated sessions or loans, the deletion is prevented
     * and an informative error message is displayed to guide the user on how
     * to proceed with cleanup.</p>
     * 
     * @param id the unique identifier of the game to delete
     * @param redirectAttributes Spring MVC attributes for passing messages between requests
     * @param principal the currently authenticated user's security principal
     * @return redirect to games list with appropriate success or error message
     */
    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteGame(@PathVariable Long id, 
                            RedirectAttributes redirectAttributes, 
                            Principal principal) {
        Optional<Game> game = gameService.findById(id);
        
        if (game.isPresent()) {
            Game gameToDelete = game.get();
            String gameName = gameToDelete.getName();
            String imagePath = gameToDelete.getImagePath();
            final Long finalId = id; // Create a final copy for use in lambda
            
            try {
                // Check if game has related sessions or loans
                List<GameSession> relatedSessions = gameSessionService.findByGame(gameToDelete);
                List<Loan> relatedLoans = loanService.findByGame(gameToDelete);
                
                if (!relatedSessions.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", 
                        "Cannot delete game '" + gameName + "' because it has " + relatedSessions.size() + 
                        " game session(s) associated with it. Delete the sessions first.");
                    return "redirect:/games";
                }
                
                if (!relatedLoans.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", 
                        "Cannot delete game '" + gameName + "' because it has " + relatedLoans.size() + 
                        " loan(s) associated with it. Remove the loan history first.");
                    return "redirect:/games";
                }
                
                // Delete the game photo if it exists
                if (imagePath != null && !imagePath.isEmpty()) {
                    try {
                        fileUploadService.deleteFile(imagePath);
                        log.info("Game photo deleted for game {}: {}", finalId, imagePath);
                    } catch (Exception e) {
                        log.warn("Could not delete game photo for game {}: {}", finalId, e.getMessage());
                    }
                }
                
                // If no references, proceed with deletion
                gameService.deleteById(finalId);
                
                // Log the action
                userService.findByUsername(principal.getName()).ifPresent(user -> 
                    auditLogService.logChange(user, ActionType.DELETION, "Game", finalId, 
                        "Game deleted: " + gameName)
                );
                
                redirectAttributes.addFlashAttribute("message", "Game '" + gameName + "' deleted successfully");
                
            } catch (Exception e) {
                log.error("Error deleting game with ID: " + finalId, e);
                redirectAttributes.addFlashAttribute("error", 
                    "Cannot delete game '" + gameName + "' because it is referenced by other records. " +
                    "Please remove all associated sessions and loans first.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Game not found");
        }
        
        return "redirect:/games";
    }

    /**
     * Handles the availability status change of a game.
     * This endpoint allows administrators to mark games as available or unavailable
     * for lending, which affects the loan system functionality.
     * 
     * <p>The availability change process includes:</p>
     * <ul>
     *   <li>Game existence validation</li>
     *   <li>Availability status update</li>
     *   <li>Database persistence of the change</li>
     *   <li>Audit logging of the status modification</li>
     *   <li>User feedback through flash messages</li>
     * </ul>
     * 
     * <p>This operation is commonly used when games are temporarily unavailable
     * due to maintenance, damage, or other administrative reasons.</p>
     * 
     * @param id the unique identifier of the game whose availability is being changed
     * @param available the new availability status (true for available, false for unavailable)
     * @param redirectAttributes Spring MVC attributes for passing messages between requests
     * @param principal the currently authenticated user's security principal
     * @return redirect to games list with appropriate success message
     */
    @PostMapping("/{id}/availability")
    @PreAuthorize("hasRole('ADMIN')")
    public String changeAvailability(@PathVariable Long id, 
                                   @RequestParam Boolean available,
                                   RedirectAttributes redirectAttributes, 
                                   Principal principal) {
        Optional<Game> gameOpt = gameService.findById(id);
        
        if (gameOpt.isPresent()) {
            Game gameToUpdate = gameOpt.get();
            gameToUpdate.setAvailable(available);
            Game savedGame = gameService.save(gameToUpdate);
            final Game finalSavedGame = savedGame; // Create a final copy for use in lambda
            
            // Log the action
            userService.findByUsername(principal.getName()).ifPresent(user -> {
                String action = available ? "Marked as available" : "Marked as unavailable";
                auditLogService.logChange(user, ActionType.MODIFICATION, "Game", finalSavedGame.getId(),
                    action + ": " + finalSavedGame.getName());
            });
            
            String message = available ? "Game marked as available" : "Game marked as unavailable";
            redirectAttributes.addFlashAttribute("message", message);
        }
        
        return "redirect:/games";
    }
}