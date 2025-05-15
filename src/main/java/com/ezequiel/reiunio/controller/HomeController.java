package com.ezequiel.reiunio.controller;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ezequiel.reiunio.entity.Game;
import com.ezequiel.reiunio.entity.GameSession;
import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.GameSessionStatus;
import com.ezequiel.reiunio.service.GameService;
import com.ezequiel.reiunio.service.GameSessionService;
import com.ezequiel.reiunio.service.LoanService;
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
    private final LoanService loanService;

    @GetMapping("/")
    public String home(Model model, Principal principal) {
        log.debug("Processing home request");
        
        try {
            // Today's sessions
            List<GameSession> todaySessions = gameSessionService.findTodaySessions();
            model.addAttribute("todaySessions", todaySessions);
            log.debug("Found {} today's sessions", todaySessions != null ? todaySessions.size() : 0);
            
            // Upcoming sessions (scheduled)
            List<GameSession> upcomingSessions = gameSessionService.findByStatus(GameSessionStatus.SCHEDULED);
            model.addAttribute("upcomingSessions", upcomingSessions);
            log.debug("Found {} upcoming sessions", upcomingSessions != null ? upcomingSessions.size() : 0);
            
            // Available games
            List<Game> availableGames = gameService.findByAvailable(true);
            model.addAttribute("availableGames", availableGames);
            log.debug("Found {} available games", availableGames != null ? availableGames.size() : 0);
            
            // If user is authenticated, show personalized information
            if (principal != null) {
                Optional<User> user = userService.findByUsername(principal.getName());
                if (user.isPresent()) {
                    model.addAttribute("user", user.get());
                    log.debug("User authenticated: {}", user.get().getUsername());
                    
                    // Sessions where the user is registered
                    List<GameSession> mySessions = gameSessionService.findSessionsByPlayer(user.get().getId());
                    model.addAttribute("mySessions", mySessions);
                    log.debug("Found {} user sessions", mySessions != null ? mySessions.size() : 0);
                }
            } else {
                log.debug("No authenticated user");
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
    public String login() {
        log.debug("Processing login request");
        return "login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        log.debug("Processing access denied request");
        return "error/access-denied";
    }
}