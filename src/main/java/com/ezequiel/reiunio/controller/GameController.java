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

@Controller
@RequestMapping("/games")
@RequiredArgsConstructor
@Slf4j
public class GameController {

    private final GameService gameService;
    private final UserService userService;
    private final AuditLogService auditLogService;
    private final GameSessionService gameSessionService;
    private final LoanService loanService;
    private final FileUploadService fileUploadService;

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

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newGameForm(Model model) {
        model.addAttribute("game", new Game());
        model.addAttribute("states", GameState.values());
        return "games/form";
    }

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