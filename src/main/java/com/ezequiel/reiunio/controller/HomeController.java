package com.ezequiel.reiunio.controller;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ezequiel.reiunio.entity.Game;
import com.ezequiel.reiunio.entity.GameSession;
import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.GameSessionStatus;
import com.ezequiel.reiunio.service.GameService;
import com.ezequiel.reiunio.service.GameSessionService;
import com.ezequiel.reiunio.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final UserService userService;
    private final GameService gameService;
    private final GameSessionService gameSessionService;


    /**
     * Welcome page with login - for unauthenticated users
     */
    @GetMapping("/welcome")
    public String welcome(Principal principal) {
        log.debug("Processing welcome request");
        
        // If user is already authenticated, redirect to home
        if (principal != null) {
            return "redirect:/home";
        }
        
        return "welcome";
    }

    /**
     * Redirect root to welcome if not authenticated, or to home if authenticated
     */
    @GetMapping("/")
    public String root(Principal principal) {
        if (principal != null) {
            return "redirect:/home";
        }
        return "redirect:/welcome";
    }

    /**
     * Home page for authenticated users
     */
    @GetMapping("/home")
    public String home(Model model, Principal principal) {
        log.debug("Processing home request");
        
        // If not authenticated, redirect to welcome
        if (principal == null) {
            return "redirect:/welcome";
        }
        
        try {
            // Today's sessions (excluding finished ones)
            List<GameSession> todaySessions = gameSessionService.findTodaySessions().stream()
                    .filter(session -> session.getStatus() != GameSessionStatus.FINISHED)
                    .collect(Collectors.toList());
            model.addAttribute("todaySessions", todaySessions);
            log.debug("Found {} today's sessions (excluding finished)", todaySessions.size());
            
            // Upcoming sessions (scheduled only - no finished sessions)
            List<GameSession> upcomingSessions = gameSessionService.findByStatus(GameSessionStatus.SCHEDULED);
            model.addAttribute("upcomingSessions", upcomingSessions);
            log.debug("Found {} upcoming sessions", upcomingSessions.size());
            
            // Available games
            List<Game> availableGames = gameService.findByAvailable(true);
            model.addAttribute("availableGames", availableGames);
            log.debug("Found {} available games", availableGames.size());
            
            // User information
            Optional<User> user = userService.findByUsername(principal.getName());
            if (user.isPresent()) {
                model.addAttribute("user", user.get());
                log.debug("User authenticated: {}", user.get().getUsername());
                
                // Sessions where the user is registered (excluding finished ones)
                List<GameSession> mySessions = gameSessionService.findSessionsByPlayer(user.get().getId()).stream()
                        .filter(session -> session.getStatus() != GameSessionStatus.FINISHED)
                        .collect(Collectors.toList());
                model.addAttribute("mySessions", mySessions);
                log.debug("Found {} user sessions (excluding finished)", mySessions.size());
            }
            
        } catch (Exception e) {
            log.error("Error processing home request", e);
            // Add empty lists to avoid null pointer exceptions in the template
            model.addAttribute("todaySessions", List.of());
            model.addAttribute("upcomingSessions", List.of());
            model.addAttribute("availableGames", List.of());
            model.addAttribute("mySessions", List.of());
        }
        
        return "index";
    }

    @GetMapping("/login")
    public String login(Principal principal) {
        log.debug("Processing login request");
        
        // If already authenticated, redirect to home
        if (principal != null) {
            return "redirect:/home";
        }
        
        // Redirect to welcome page with login form
        return "redirect:/welcome";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        log.debug("Processing access denied request");
        return "error/access-denied";
    }
}