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

/**
 * Controller responsible for handling the main navigation and landing pages of the Reiunio application.
 * This controller manages the user journey from initial access through authentication to the main
 * dashboard, providing appropriate content based on user authentication status and role.
 * 
 * <p>Key responsibilities include:</p>
 * <ul>
 *   <li>Managing the welcome page for unauthenticated users</li>
 *   <li>Providing the main dashboard for authenticated users</li>
 *   <li>Handling root path routing based on authentication status</li>
 *   <li>Managing login redirection logic</li>
 *   <li>Displaying access denied error pages</li>
 *   <li>Aggregating dashboard data from multiple services</li>
 * </ul>
 * 
 * <p>The controller implements intelligent routing that directs users to appropriate pages
 * based on their authentication status, ensuring a smooth user experience and proper
 * access control throughout the application.</p>
 * 
 * <p>Dashboard functionality includes:</p>
 * <ul>
 *   <li>Today's active game sessions overview</li>
 *   <li>Upcoming scheduled sessions</li>
 *   <li>Available games for borrowing</li>
 *   <li>User's personal session participations</li>
 *   <li>Role-specific content and navigation options</li>
 * </ul>
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    /** Service for user-related operations and authentication */
    private final UserService userService;
    
    /** Service for game-related operations and library management */
    private final GameService gameService;
    
    /** Service for game session operations and scheduling */
    private final GameSessionService gameSessionService;

    /**
     * Displays the welcome page for unauthenticated users.
     * This endpoint serves as the main landing page for visitors who haven't logged in yet,
     * providing information about the application and access to login functionality.
     * 
     * <p>The method implements intelligent redirection:</p>
     * <ul>
     *   <li>If the user is already authenticated, redirects to the main dashboard</li>
     *   <li>If the user is not authenticated, displays the welcome page with login options</li>
     * </ul>
     * 
     * <p>The welcome page typically includes:</p>
     * <ul>
     *   <li>Application overview and features</li>
     *   <li>Login form or login button</li>
     *   <li>Registration link for new users</li>
     *   <li>Public information about the gaming community</li>
     * </ul>
     * 
     * @param principal the currently authenticated user's security principal (null if not authenticated)
     * @return redirect to home dashboard if authenticated, or welcome page view name if not authenticated
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
     * Handles requests to the application root path with intelligent routing.
     * This endpoint serves as the main entry point for the application, automatically
     * directing users to the appropriate page based on their authentication status.
     * 
     * <p>Routing logic:</p>
     * <ul>
     *   <li><strong>Authenticated users:</strong> Redirected to the main dashboard (/home)</li>
     *   <li><strong>Unauthenticated users:</strong> Redirected to the welcome page (/welcome)</li>
     * </ul>
     * 
     * <p>This approach ensures that users always land on the most appropriate page
     * for their current authentication state, improving user experience and providing
     * a clear navigation flow.</p>
     * 
     * @param principal the currently authenticated user's security principal (null if not authenticated)
     * @return redirect to home dashboard if authenticated, or redirect to welcome page if not authenticated
     */
    @GetMapping("/")
    public String root(Principal principal) {
        if (principal != null) {
            return "redirect:/home";
        }
        return "redirect:/welcome";
    }

    /**
     * Displays the main dashboard for authenticated users with comprehensive gaming overview.
     * This endpoint serves as the central hub for authenticated users, providing a personalized
     * dashboard with relevant gaming information, upcoming sessions, and quick access to
     * key application features.
     * 
     * <p>Dashboard components include:</p>
     * <ul>
     *   <li><strong>Today's Sessions:</strong> Active sessions scheduled for today (excluding finished)</li>
     *   <li><strong>Upcoming Sessions:</strong> All scheduled sessions in the future</li>
     *   <li><strong>Available Games:</strong> Games currently available for borrowing</li>
     *   <li><strong>User's Sessions:</strong> Sessions where the current user is registered</li>
     *   <li><strong>User Information:</strong> Current user's profile and role information</li>
     * </ul>
     * 
     * <p>Data filtering and organization:</p>
     * <ul>
     *   <li>Finished sessions are excluded from today's and user's session lists</li>
     *   <li>Only scheduled sessions are shown in upcoming sessions</li>
     *   <li>Games are filtered to show only those available for borrowing</li>
     *   <li>User-specific data is personalized based on the authenticated user</li>
     * </ul>
     * 
     * <p>Error handling:</p>
     * <ul>
     *   <li>If data retrieval fails, empty lists are provided to prevent template errors</li>
     *   <li>Unauthenticated access is redirected to the welcome page</li>
     *   <li>All errors are logged for debugging purposes</li>
     * </ul>
     * 
     * @param model the Spring MVC model for passing data to the view
     * @param principal the currently authenticated user's security principal
     * @return the name of the main dashboard view template, or redirect to welcome if not authenticated
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

    /**
     * Handles login page requests with intelligent redirection for authenticated users.
     * This endpoint manages access to the login functionality while preventing unnecessary
     * login attempts by users who are already authenticated.
     * 
     * <p>Login handling logic:</p>
     * <ul>
     *   <li><strong>Already authenticated:</strong> Redirects to the main dashboard to prevent redundant login</li>
     *   <li><strong>Not authenticated:</strong> Redirects to the welcome page which includes login functionality</li>
     * </ul>
     * 
     * <p>This approach centralizes login functionality on the welcome page while ensuring
     * that authenticated users don't encounter unnecessary login prompts, improving the
     * overall user experience.</p>
     * 
     * @param principal the currently authenticated user's security principal (null if not authenticated)
     * @return redirect to home dashboard if authenticated, or redirect to welcome page for login access
     */
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

    /**
     * Displays the access denied error page for unauthorized access attempts.
     * This endpoint handles security violations when users attempt to access resources
     * or perform actions that exceed their authorization level.
     * 
     * <p>Common scenarios leading to access denied:</p>
     * <ul>
     *   <li>Users attempting to access admin-only functionality</li>
     *   <li>Basic users trying to access extended user features</li>
     *   <li>Users attempting to modify resources they don't own</li>
     *   <li>Attempts to access protected endpoints without proper role</li>
     * </ul>
     * 
     * <p>The access denied page typically includes:</p>
     * <ul>
     *   <li>Clear explanation of the access restriction</li>
     *   <li>Information about required permissions or roles</li>
     *   <li>Navigation options to return to authorized areas</li>
     *   <li>Contact information for requesting additional access</li>
     * </ul>
     * 
     * @return the name of the access denied error page view template
     */
    @GetMapping("/access-denied")
    public String accessDenied() {
        log.debug("Processing access denied request");
        return "error/access-denied";
    }
}