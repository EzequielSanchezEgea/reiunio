package com.ezequiel.reiunio.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ezequiel.reiunio.entity.Game;
import com.ezequiel.reiunio.entity.GameSessionInfo;
import com.ezequiel.reiunio.entity.Loan;
import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.ActionType;
import com.ezequiel.reiunio.enums.LoanStatus;
import com.ezequiel.reiunio.service.AuditLogService;
import com.ezequiel.reiunio.service.GameService;
import com.ezequiel.reiunio.service.LoanService;
import com.ezequiel.reiunio.service.UserService;
import com.ezequiel.reiunio.service.impl.LoanServiceImpl.LoanConflictInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for managing all loan operations in the application.
 * 
 * <p>Handles the creation, viewing, and management of game loans, including
 * conflict detection with game sessions. Provides separate views for administrators
 * and regular users, with appropriate security checks for each operation.</p>
 */
@Controller
@RequestMapping("/loans")
@RequiredArgsConstructor
@Slf4j
public class LoanController {

    private final LoanService loanService;
    private final GameService gameService;
    private final UserService userService;
    private final AuditLogService auditLogService;

    /**
     * Displays a paginated list of loans with optional status filtering.
     * 
     * @param model the Spring MVC model to populate with loan data
     * @param status optional filter parameter for loan status
     * @param page the page number for pagination (0-based)
     * @param size the number of items per page
     * @return the view name for displaying the loan list
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXTENDED_USER')")
    public String listLoans(Model model, 
                           @RequestParam(required = false) String status,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("loanDate").descending());
        Page<Loan> loanPage;
        
        if (status != null && !status.isEmpty()) {
            try {
                LoanStatus statusEnum = LoanStatus.valueOf(status.toUpperCase());
                loanPage = loanService.findByStatusPaginated(statusEnum, pageable);
                model.addAttribute("filterStatus", status);
            } catch (IllegalArgumentException e) {
                loanPage = loanService.findAllPaginated(pageable);
            }
        } else {
            loanPage = loanService.findAllPaginated(pageable);
        }
        
        model.addAttribute("loanPage", loanPage);
        model.addAttribute("loans", loanPage.getContent());
        model.addAttribute("statuses", LoanStatus.values());
        model.addAttribute("currentUrl", "/loans");
        
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", loanPage.getTotalPages());
        model.addAttribute("totalElements", loanPage.getTotalElements());
        model.addAttribute("pageSize", size);
        
        return "loans/list";
    }

    /**
     * Displays detailed information about a specific loan.
     * 
     * @param id the ID of the loan to view
     * @param model the Spring MVC model to populate with loan details
     * @return the view name for loan details or redirect to loan list if not found
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXTENDED_USER') or @securityUtils.isUserLoan(#id, principal.username)")
    public String viewLoan(@PathVariable Long id, Model model) {
        Optional<Loan> loan = loanService.findById(id);
        
        if (loan.isPresent()) {
            model.addAttribute("loan", loan.get());
            return "loans/detail";
        } else {
            return "redirect:/loans";
        }
    }

    /**
     * Displays the form for creating a new loan.
     * 
     * @param model the Spring MVC model to populate with form data
     * @param gameId optional parameter to pre-select a specific game
     * @return the view name for the loan creation form
     */
    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXTENDED_USER')")
    public String newLoan(Model model, @RequestParam(required = false) Long gameId) {
        List<User> users = userService.findAll();
        List<Game> availableGames = gameService.findByAvailable(true);
        
        model.addAttribute("users", users);
        model.addAttribute("games", availableGames);
        
        LocalDate defaultReturnDate = LocalDate.now().plusWeeks(1);
        model.addAttribute("suggestedReturnDate", defaultReturnDate);
        
        if (gameId != null) {
            Optional<Game> selectedGameOpt = gameService.findById(gameId);
            if (selectedGameOpt.isPresent()) {
                Game selectedGame = selectedGameOpt.get();
                
                if (!selectedGame.getAvailable()) {
                    model.addAttribute("error", "The selected game '" + selectedGame.getName() + "' is not available for loans.");
                    return "loans/form";
                }
                
                List<GameSessionInfo> upcomingSessions = loanService.findUpcomingSessionsForGame(selectedGame);
                LoanConflictInfo conflictInfo = loanService.checkLoanConflicts(selectedGame, defaultReturnDate);
                
                model.addAttribute("selectedGame", selectedGame);
                model.addAttribute("upcomingSessions", upcomingSessions);
                model.addAttribute("suggestedReturnDate", conflictInfo.getSuggestedReturnDate());
                model.addAttribute("hasSessionConflict", conflictInfo.hasConflicts());
                
                if (conflictInfo.hasConflicts()) {
                    model.addAttribute("warning", "Scheduling conflict detected: " + conflictInfo.getWarningMessage());
                } else if (!upcomingSessions.isEmpty()) {
                    model.addAttribute("info", "Info: There are upcoming sessions for this game, but no conflicts detected with the proposed return date.");
                }
                
                log.debug("Game {} has {} upcoming sessions, conflicts: {}, suggested date: {}", 
                         selectedGame.getName(), upcomingSessions.size(), conflictInfo.hasConflicts(), 
                         conflictInfo.getSuggestedReturnDate());
            }
        }
        
        return "loans/form";
    }

    /**
     * Processes the creation of a new loan.
     * 
     * @param userId the ID of the user borrowing the game
     * @param gameId the ID of the game being loaned
     * @param estimatedReturnDate the expected return date for the loan
     * @param redirectAttributes for passing flash messages after redirect
     * @param principal the authenticated user creating the loan
     * @return redirect to loan list or back to form with errors
     */
    @PostMapping("/new")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXTENDED_USER')")
    public String createLoan(@RequestParam Long userId,
                            @RequestParam Long gameId,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate estimatedReturnDate,
                            RedirectAttributes redirectAttributes,
                            Principal principal) {
        
        Optional<User> user = userService.findById(userId);
        Optional<Game> game = gameService.findById(gameId);
        
        if (!user.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "User not found");
            return "redirect:/loans/new";
        }
        
        if (!game.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Game not found");
            return "redirect:/loans/new";
        }
        
        try {
            Game selectedGame = game.get();
            
            if (!selectedGame.getAvailable()) {
                redirectAttributes.addFlashAttribute("error", 
                    "Game '" + selectedGame.getName() + "' is not available for loans");
                return "redirect:/loans/new?gameId=" + gameId;
            }
            
            if (estimatedReturnDate.isBefore(LocalDate.now()) || estimatedReturnDate.isEqual(LocalDate.now())) {
                redirectAttributes.addFlashAttribute("error", 
                    "Return date must be after today");
                return "redirect:/loans/new?gameId=" + gameId;
            }
            
            LoanConflictInfo conflictInfo = loanService.checkLoanConflicts(selectedGame, estimatedReturnDate);
            Loan loan = loanService.createLoan(user.get(), selectedGame, estimatedReturnDate);
            
            userService.findByUsername(principal.getName()).ifPresent(adminUser -> 
                auditLogService.logChange(adminUser, ActionType.CREATION, "Loan", loan.getId(),
                    "Loan of game " + selectedGame.getName() + " to user " + user.get().getUsername() +
                    (conflictInfo.hasConflicts() ? " (with session conflicts)" : ""))
            );
            
            String successMessage = "Loan registered successfully";
            if (conflictInfo.hasConflicts()) {
                successMessage += ". Note: There are upcoming sessions for this game - consider the return timing";
            }
            
            redirectAttributes.addFlashAttribute("message", successMessage);
            return "redirect:/loans";
            
        } catch (IllegalStateException e) {
            log.error("Error creating loan: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/loans/new?gameId=" + gameId;
        } catch (Exception e) {
            log.error("Unexpected error creating loan", e);
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred while creating the loan");
            return "redirect:/loans/new?gameId=" + gameId;
        }
    }

    /**
     * Processes the return of a loaned game.
     * 
     * @param id the ID of the loan being returned
     * @param returnDate optional return date (defaults to current date)
     * @param redirectAttributes for passing flash messages after redirect
     * @param principal the authenticated user processing the return
     * @return redirect to loan list with success/error message
     */
    @PostMapping("/{id}/return")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXTENDED_USER')")
    public String registerReturn(@PathVariable Long id,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
                                RedirectAttributes redirectAttributes,
                                Principal principal) {
        
        if (returnDate == null) {
            returnDate = LocalDate.now();
        }
        
        try {
            Loan loan = loanService.registerReturn(id, returnDate);
            
            userService.findByUsername(principal.getName()).ifPresent(adminUser -> 
                auditLogService.logChange(adminUser, ActionType.MODIFICATION, "Loan", loan.getId(),
                    "Return of game " + loan.getGame().getName() + " by user " + loan.getUser().getUsername())
            );
            
            redirectAttributes.addFlashAttribute("message", "Return registered successfully");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/loans";
    }

    /**
     * Displays a paginated list of overdue loans.
     * 
     * @param model the Spring MVC model to populate with overdue loan data
     * @param page the page number for pagination (0-based)
     * @param size the number of items per page
     * @return the view name for displaying overdue loans
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXTENDED_USER')")
    public String viewOverdueLoans(Model model,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("loanDate").descending());
        Page<Loan> overdueLoanPage = loanService.findOverdueLoansPaginated(pageable);
        
        model.addAttribute("loanPage", overdueLoanPage);
        model.addAttribute("loans", overdueLoanPage.getContent());
        model.addAttribute("listTitle", "Overdue Loans");
        model.addAttribute("currentUrl", "/loans/overdue");
        
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", overdueLoanPage.getTotalPages());
        model.addAttribute("totalElements", overdueLoanPage.getTotalElements());
        model.addAttribute("pageSize", size);
        
        return "loans/list";
    }

    /**
     * Displays a paginated list of loans for the currently authenticated user.
     * 
     * @param model the Spring MVC model to populate with user's loan data
     * @param principal the authenticated user
     * @param page the page number for pagination (0-based)
     * @param size the number of items per page
     * @return the view name for displaying the user's loans
     */
    @GetMapping("/my-loans")
    public String viewMyLoans(Model model, Principal principal,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int size) {
        
        userService.findByUsername(principal.getName()).ifPresent(user -> {
            Pageable pageable = PageRequest.of(page, size, Sort.by("loanDate").descending());
            Page<Loan> userLoanPage = loanService.findByUserPaginated(user, pageable);
            
            model.addAttribute("loanPage", userLoanPage);
            model.addAttribute("loans", userLoanPage.getContent());
            model.addAttribute("listTitle", "My Loans");
            model.addAttribute("currentUrl", "/loans/my-loans");
            
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", userLoanPage.getTotalPages());
            model.addAttribute("totalElements", userLoanPage.getTotalElements());
            model.addAttribute("pageSize", size);
        });
        
        return "loans/list";
    }
    
    /**
     * AJAX endpoint to retrieve game information including availability and session conflicts.
     * 
     * @param gameId the ID of the game to get information for
     * @return ResponseEntity containing game information or error message
     */
    @GetMapping("/api/game-info/{gameId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXTENDED_USER')")
    @ResponseBody
    public ResponseEntity<GameInfoResponse> getGameInfo(@PathVariable Long gameId) {
        try {
            Optional<Game> gameOpt = gameService.findById(gameId);
            
            if (!gameOpt.isPresent()) {
                return ResponseEntity.badRequest().body(new GameInfoResponse(false, "Game not found", null, null, null, false, ""));
            }
            
            Game game = gameOpt.get();
            
            if (!game.getAvailable()) {
                return ResponseEntity.ok(new GameInfoResponse(false, "This game is currently on loan and not available", game, null, null, false, "Game is not available for loans"));
            }
            
            List<GameSessionInfo> upcomingSessions = loanService.findUpcomingSessionsForGame(game);
            LocalDate defaultReturnDate = LocalDate.now().plusWeeks(1);
            LoanConflictInfo conflictInfo = loanService.checkLoanConflicts(game, defaultReturnDate);
            
            return ResponseEntity.ok(new GameInfoResponse(
                true, 
                "Game information loaded successfully", 
                game, 
                upcomingSessions, 
                conflictInfo.getSuggestedReturnDate(), 
                conflictInfo.hasConflicts(),
                conflictInfo.hasConflicts() ? "There are session conflicts with the default return date" : "No conflicts detected"
            ));
            
        } catch (Exception e) {
            log.error("Error getting game info for gameId: " + gameId, e);
            return ResponseEntity.internalServerError().body(new GameInfoResponse(false, "Error loading game information", null, null, null, false, "Server error"));
        }
    }
    
    /**
     * Response class for game information AJAX requests.
     * 
     * <p>Contains all relevant information about a game's availability,
     * upcoming sessions, and potential loan conflicts.</p>
     */
    public static class GameInfoResponse {
        private boolean success;
        private String message;
        private Game game;
        private List<GameSessionInfo> upcomingSessions;
        private LocalDate suggestedReturnDate;
        private boolean hasConflicts;
        private String conflictMessage;
        
        public GameInfoResponse(boolean success, String message, Game game, List<GameSessionInfo> upcomingSessions, 
                               LocalDate suggestedReturnDate, boolean hasConflicts, String conflictMessage) {
            this.success = success;
            this.message = message;
            this.game = game;
            this.upcomingSessions = upcomingSessions;
            this.suggestedReturnDate = suggestedReturnDate;
            this.hasConflicts = hasConflicts;
            this.conflictMessage = conflictMessage;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Game getGame() { return game; }
        public List<GameSessionInfo> getUpcomingSessions() { return upcomingSessions; }
        public LocalDate getSuggestedReturnDate() { return suggestedReturnDate; }
        public boolean isHasConflicts() { return hasConflicts; }
        public String getConflictMessage() { return conflictMessage; }
    }
}