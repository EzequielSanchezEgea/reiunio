package com.ezequiel.reiunio.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ezequiel.reiunio.entity.Game;
import com.ezequiel.reiunio.enums.ActionType;
import com.ezequiel.reiunio.enums.GameState;
import com.ezequiel.reiunio.service.AuditLogService;
import com.ezequiel.reiunio.service.GameService;
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

    @GetMapping
    public String listGames(Model model, 
                           @RequestParam(required = false) String name,
                           @RequestParam(required = false) String category,
                           @RequestParam(required = false) Boolean available) {
        List<Game> games;
        
        if (name != null && !name.isEmpty()) {
            games = gameService.findByName(name);
            model.addAttribute("filterName", name);
        } else if (category != null && !category.isEmpty()) {
            games = gameService.findByCategory(category);
            model.addAttribute("filterCategory", category);
        } else if (available != null) {
            games = gameService.findByAvailable(available);
            model.addAttribute("filterAvailable", available);
        } else {
            games = gameService.findAll();
        }
        
        model.addAttribute("games", games);
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
                            Model model, 
                            RedirectAttributes redirectAttributes, 
                            Principal principal) {
        if (result.hasErrors()) {
            model.addAttribute("states", GameState.values());
            return "games/form";
        }
        
        game.setAcquisitionDate(LocalDate.now());
        game.setAvailable(true);
        
        Game savedGame = gameService.save(game);
        
        // Log the action - using savedGame (effectively final)
        userService.findByUsername(principal.getName()).ifPresent(user -> 
            auditLogService.logChange(user, ActionType.CREATION, "Game", savedGame.getId(), 
                "Game created: " + savedGame.getName())
        );
        
        redirectAttributes.addFlashAttribute("message", "Game created successfully");
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
            
            // Keep original acquisition date
            game.setAcquisitionDate(currentGame.getAcquisitionDate());
            
            Game savedGame = gameService.save(game);
            
            // Log the action - using savedGame (effectively final)
            userService.findByUsername(principal.getName()).ifPresent(user -> 
                auditLogService.logChange(user, ActionType.MODIFICATION, "Game", savedGame.getId(), 
                    "Game modified: " + savedGame.getName())
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
            // Store name before deletion
            String deletedName = game.get().getName();
            
            gameService.deleteById(id);
            
            // Log the action - using stored name (effectively final)
            userService.findByUsername(principal.getName()).ifPresent(user -> 
                auditLogService.logChange(user, ActionType.DELETION, "Game", id, 
                    "Game deleted: " + deletedName)
            );
            
            redirectAttributes.addFlashAttribute("message", "Game deleted successfully");
        }
        
        return "redirect:/games";
    }

    @GetMapping("/search")
    public String searchGames(@RequestParam(required = false) String name,
                             @RequestParam(required = false) String category,
                             @RequestParam(required = false) Boolean available,
                             @RequestParam(required = false) Integer playerCount,
                             Model model) {
        List<Game> games;
        
        if (name != null && !name.isEmpty()) {
            games = gameService.findByName(name);
            model.addAttribute("filterName", name);
        } else if (category != null && !category.isEmpty()) {
            games = gameService.findByCategory(category);
            model.addAttribute("filterCategory", category);
        } else if (available != null) {
            games = gameService.findByAvailable(available);
            model.addAttribute("filterAvailable", available);
        } else if (playerCount != null) {
            games = gameService.findByPlayerCount(playerCount);
            model.addAttribute("filterPlayerCount", playerCount);
        } else {
            games = gameService.findAll();
        }
        
        model.addAttribute("games", games);
        return "games/list";
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
            
            // Log the action - using saved game instance and local variables
            userService.findByUsername(principal.getName()).ifPresent(user -> {
                String action = available ? "Marked as available" : "Marked as unavailable";
                auditLogService.logChange(user, ActionType.MODIFICATION, "Game", savedGame.getId(),
                    action + ": " + savedGame.getName());
            });
            
            String message = available ? "Game marked as available" : "Game marked as unavailable";
            redirectAttributes.addFlashAttribute("message", message);
        }
        
        return "redirect:/games";
    }
}