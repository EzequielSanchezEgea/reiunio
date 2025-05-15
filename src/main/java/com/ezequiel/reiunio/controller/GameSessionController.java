package com.ezequiel.reiunio.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ezequiel.reiunio.entity.Game;
import com.ezequiel.reiunio.entity.GameSession;
import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.GameSessionStatus;
import com.ezequiel.reiunio.enums.ActionType;
import com.ezequiel.reiunio.service.AuditLogService;
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

    // List all game sessions
    @GetMapping
    public String listGameSessions(Model model, @RequestParam(required = false) String status) {
        List<GameSession> gameSessions;
        
        if (status != null && !status.isEmpty()) {
            try {
                GameSessionStatus statusEnum = GameSessionStatus.valueOf(status.toUpperCase());
                gameSessions = gameSessionService.findByStatus(statusEnum);
                model.addAttribute("filterStatus", status);
            } catch (IllegalArgumentException e) {
                gameSessions = gameSessionService.findAll();
            }
        } else {
            gameSessions = gameSessionService.findAll();
        }
        
        model.addAttribute("gameSessions", gameSessions);
        model.addAttribute("statuses", GameSessionStatus.values());
        return "game-sessions/list";
    }

    // View game session details
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
                    
                    // Check if user is confirmed
                    if (registered) {
                        boolean confirmed = session.getPlayers().stream()
                                .filter(gsp -> gsp.getUser().getId().equals(user.getId()))
                                .findFirst()
                                .map(gsp -> gsp.getConfirmed())
                                .orElse(false);
                        model.addAttribute("userConfirmed", confirmed);
                    }
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
        List<Game> games = gameService.findByAvailable(true); // Solo juegos disponibles
        
        model.addAttribute("gameSession", new GameSession());
        model.addAttribute("games", games);
        model.addAttribute("editing", false);
        return "game-sessions/form";
    }

    @PostMapping("/new")
    public String createGameSession(@Valid @ModelAttribute("gameSession") GameSession gameSession,
                                   BindingResult result,
                                   @RequestParam Long gameId,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
                                   Model model,
                                   RedirectAttributes redirectAttributes,
                                   Principal principal) {
        
        if (result.hasErrors()) {
            model.addAttribute("games", gameService.findByAvailable(true));
            model.addAttribute("editing", false);
            return "game-sessions/form";
        }
        
        Optional<Game> game = gameService.findById(gameId);
        Optional<User> user = userService.findByUsername(principal.getName());
        
        if (!game.isPresent()) {
            model.addAttribute("error", "Game not found");
            model.addAttribute("games", gameService.findByAvailable(true));
            model.addAttribute("editing", false);
            return "game-sessions/form";
        }
        
        if (!user.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "User not found");
            return "redirect:/game-sessions/new";
        }
        
        // Validate that the game is available
        if (!game.get().getAvailable()) {
            model.addAttribute("error", "Selected game is not available");
            model.addAttribute("games", gameService.findByAvailable(true));
            model.addAttribute("editing", false);
            return "game-sessions/form";
        }
        
        // Validate date is not in the past
        if (date.isBefore(LocalDate.now())) {
            model.addAttribute("error", "Session date cannot be in the past");
            model.addAttribute("games", gameService.findByAvailable(true));
            model.addAttribute("editing", false);
            return "game-sessions/form";
        }
        
        // Validate end time is after start time
        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            model.addAttribute("error", "End time must be after start time");
            model.addAttribute("games", gameService.findByAvailable(true));
            model.addAttribute("editing", false);
            return "game-sessions/form";
        }
        
        try {
            gameSession.setGame(game.get());
            gameSession.setCreator(user.get());
            gameSession.setDate(date);
            gameSession.setStartTime(startTime);
            gameSession.setEndTime(endTime);
            gameSession.setStatus(GameSessionStatus.SCHEDULED);
            
            gameSession = gameSessionService.save(gameSession);
            
            // Add creator as first player and confirm them
            gameSessionService.addPlayerToSession(gameSession.getId(), user.get().getId());
            gameSessionService.confirmPlayerInSession(gameSession.getId(), user.get().getId());
            
            // Log the action
            auditLogService.logChange(
                user.get(),
                ActionType.CREATION,
                "GameSession",
                gameSession.getId(),
                "Game session created: " + gameSession.getTitle()
            );
            
            redirectAttributes.addFlashAttribute("message", "Game session created successfully");
            return "redirect:/game-sessions/" + gameSession.getId();
        } catch (Exception e) {
            log.error("Error creating game session", e);
            model.addAttribute("error", "An error occurred while creating the session: " + e.getMessage());
            model.addAttribute("games", gameService.findByAvailable(true));
            model.addAttribute("editing", false);
            return "game-sessions/form";
        }
    }

    // Form for editing an existing game session
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXTENDED_USER') or @securityUtils.isGameSessionCreator(#id, principal.username)")
    public String editGameSession(@PathVariable Long id, Model model) {
        Optional<GameSession> gameSession = gameSessionService.findById(id);
        
        if (gameSession.isPresent()) {
            model.addAttribute("gameSession", gameSession.get());
            model.addAttribute("games", gameService.findAll());
            model.addAttribute("editing", true);
            return "game-sessions/form";
        } else {
            return "redirect:/game-sessions";
        }
    }

    // Process game session update
    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXTENDED_USER') or @securityUtils.isGameSessionCreator(#id, principal.username)")
    public String updateGameSession(@PathVariable Long id,
                                   @Valid @ModelAttribute("gameSession") GameSession gameSession,
                                   BindingResult result,
                                   @RequestParam Long gameId,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
                                   Model model,
                                   RedirectAttributes redirectAttributes,
                                   Principal principal) {
        
        if (result.hasErrors()) {
            model.addAttribute("games", gameService.findAll());
            model.addAttribute("editing", true);
            return "game-sessions/form";
        }
        
        Optional<GameSession> existingSession = gameSessionService.findById(id);
        Optional<Game> game = gameService.findById(gameId);
        Optional<User> user = userService.findByUsername(principal.getName());
        
        if (!existingSession.isPresent() || !game.isPresent() || !user.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Game session, game or user not found");
            return "redirect:/game-sessions";
        }
        
        GameSession currentSession = existingSession.get();
        
        // Keep original creator
        gameSession.setId(id);
        gameSession.setCreator(currentSession.getCreator());
        gameSession.setGame(game.get());
        gameSession.setDate(date);
        gameSession.setStartTime(startTime);
        gameSession.setEndTime(endTime);
        
        // Keep original players
        gameSession.setPlayers(currentSession.getPlayers());
        
        gameSession = gameSessionService.save(gameSession);
        
        // Log the action
        auditLogService.logChange(
            user.get(),
            ActionType.MODIFICATION,
            "GameSession",
            gameSession.getId(),
            "Game session modified: " + gameSession.getTitle()
        );
        
        redirectAttributes.addFlashAttribute("message", "Game session updated successfully");
        return "redirect:/game-sessions";
    }

    // Change game session status
    @PostMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXTENDED_USER') or @securityUtils.isGameSessionCreator(#id, principal.username)")
    public String changeSessionStatus(@PathVariable Long id,
                                     @RequestParam String status,
                                     RedirectAttributes redirectAttributes,
                                     Principal principal) {
        
        Optional<GameSession> sessionOpt = gameSessionService.findById(id);
        Optional<User> userOpt = userService.findByUsername(principal.getName());
        
        if (sessionOpt.isPresent() && userOpt.isPresent()) {
            GameSession gameSession = sessionOpt.get();
            User user = userOpt.get();
            
            try {
                GameSessionStatus newStatus = GameSessionStatus.valueOf(status.toUpperCase());
                gameSession.setStatus(newStatus);
                gameSessionService.save(gameSession);
                
                // Log the action
                auditLogService.logChange(
                    user,
                    ActionType.MODIFICATION,
                    "GameSession",
                    gameSession.getId(),
                    "Game session status changed to " + newStatus + ": " + gameSession.getTitle()
                );
                
                redirectAttributes.addFlashAttribute("message", "Game session status updated to " + newStatus);
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("error", "Invalid status");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Game session or user not found");
        }
        
        return "redirect:/game-sessions";
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

    // Confirm attendance
    @PostMapping("/{id}/confirm")
    public String confirmAttendance(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes,
                                   Principal principal) {
        
        userService.findByUsername(principal.getName()).ifPresent(user -> {
            boolean confirmed = gameSessionService.confirmPlayerInSession(id, user.getId());
            
            if (confirmed) {
                redirectAttributes.addFlashAttribute("message", "Attendance confirmed successfully");
                
                // Log the action
                gameSessionService.findById(id).ifPresent(session -> 
                    auditLogService.logChange(
                        user,
                        ActionType.MODIFICATION,
                        "GameSession",
                        id,
                        "Confirmed attendance for game session: " + session.getTitle()
                    )
                );
            } else {
                redirectAttributes.addFlashAttribute("error", "Could not confirm attendance. Session may be full or you're not registered.");
            }
        });
        
        return "redirect:/game-sessions/" + id;
    }

    // View today's sessions
    @GetMapping("/today")
    public String viewTodaySessions(Model model) {
        List<GameSession> sessions = gameSessionService.findTodaySessions();
        model.addAttribute("gameSessions", sessions);
        model.addAttribute("listTitle", "Today's Sessions");
        return "game-sessions/list";
    }
    
    // View upcoming sessions
    @GetMapping("/upcoming")
    public String viewUpcomingSessions(Model model) {
        List<GameSession> sessions = gameSessionService.findUpcomingSessions();
        model.addAttribute("gameSessions", sessions);
        model.addAttribute("listTitle", "Upcoming Sessions");
        return "game-sessions/list";
    }
    
    // View my created sessions
    @GetMapping("/my-sessions")
    public String viewMySessions(Model model, Principal principal) {
        userService.findByUsername(principal.getName()).ifPresent(user -> {
            List<GameSession> sessions = gameSessionService.findByCreator(user);
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
            model.addAttribute("gameSessions", sessions);
            model.addAttribute("listTitle", "My Participations");
        });
        
        return "game-sessions/list";
    }
}