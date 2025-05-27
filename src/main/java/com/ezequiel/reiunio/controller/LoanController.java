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

@Controller
@RequestMapping("/loans")
@RequiredArgsConstructor
@Slf4j
public class LoanController {

    private final LoanService loanService;
    private final GameService gameService;
    private final UserService userService;
    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXTENDED_USER')")
    public String listLoans(Model model, 
                           @RequestParam(required = false) String status,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "20") int size) {
        
        // Configurar paginación con ordenamiento por fecha de préstamo descendente
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
        model.addAttribute("currentUrl", "/loans"); // Para paginación
        
        // Agregar información de paginación
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", loanPage.getTotalPages());
        model.addAttribute("totalElements", loanPage.getTotalElements());
        model.addAttribute("pageSize", size);
        
        return "loans/list";
    }

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

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXTENDED_USER')")
    public String newLoan(Model model, @RequestParam(required = false) Long gameId) {
        List<User> users = userService.findAll();
        List<Game> availableGames = gameService.findByAvailable(true);
        
        model.addAttribute("users", users);
        model.addAttribute("games", availableGames);
        
        // Calcular fecha sugerida de devolución (una semana por defecto)
        LocalDate defaultReturnDate = LocalDate.now().plusWeeks(1);
        model.addAttribute("suggestedReturnDate", defaultReturnDate);
        
        // Si se especifica un juego, obtener información adicional
        if (gameId != null) {
            Optional<Game> selectedGameOpt = gameService.findById(gameId);
            if (selectedGameOpt.isPresent()) {
                Game selectedGame = selectedGameOpt.get();
                
                // Verificar que el juego esté disponible
                if (!selectedGame.getAvailable()) {
                    model.addAttribute("error", "The selected game '" + selectedGame.getName() + "' is not available for loans.");
                    return "loans/form";
                }
                
                // Obtener sesiones próximas
                List<GameSessionInfo> upcomingSessions = loanService.findUpcomingSessionsForGame(selectedGame);
                
                // Verificar conflictos con sesiones pero NO bloquear el préstamo
                LoanConflictInfo conflictInfo = loanService.checkLoanConflicts(selectedGame, defaultReturnDate);
                
                model.addAttribute("selectedGame", selectedGame);
                model.addAttribute("upcomingSessions", upcomingSessions);
                model.addAttribute("suggestedReturnDate", conflictInfo.getSuggestedReturnDate());
                model.addAttribute("hasSessionConflict", conflictInfo.hasConflicts());
                
                // Agregar mensaje informativo si hay conflictos (pero no bloquear)
                if (conflictInfo.hasConflicts()) {
                    model.addAttribute("warning", "Info: There are upcoming sessions for this game. " +
                        "Consider the suggested return date to avoid conflicts, but you can still proceed with your preferred date.");
                }
                
                log.debug("Game {} has {} upcoming sessions, conflicts: {}", 
                         selectedGame.getName(), upcomingSessions.size(), conflictInfo.hasConflicts());
            }
        }
        
        return "loans/form";
    }

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
            
            // Verificar disponibilidad del juego
            if (!selectedGame.getAvailable()) {
                redirectAttributes.addFlashAttribute("error", 
                    "Game '" + selectedGame.getName() + "' is not available for loans");
                return "redirect:/loans/new?gameId=" + gameId;
            }
            
            // Verificar que la fecha de devolución no sea en el pasado
            if (estimatedReturnDate.isBefore(LocalDate.now()) || estimatedReturnDate.isEqual(LocalDate.now())) {
                redirectAttributes.addFlashAttribute("error", 
                    "Return date must be after today");
                return "redirect:/loans/new?gameId=" + gameId;
            }
            
            // Obtener información sobre conflictos para mostrar advertencia (pero NO bloquear)
            LoanConflictInfo conflictInfo = loanService.checkLoanConflicts(selectedGame, estimatedReturnDate);
            
            // Crear el préstamo independientemente de los conflictos
            Loan loan = loanService.createLoan(user.get(), selectedGame, estimatedReturnDate);
            
            // Log the action
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
            
            // Log the action
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

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXTENDED_USER')")
    public String viewOverdueLoans(Model model,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        
        // Configurar paginación para préstamos vencidos
        Pageable pageable = PageRequest.of(page, size, Sort.by("loanDate").descending());
        Page<Loan> overdueLoanPage = loanService.findOverdueLoansPaginated(pageable);
        
        model.addAttribute("loanPage", overdueLoanPage);
        model.addAttribute("loans", overdueLoanPage.getContent());
        model.addAttribute("listTitle", "Overdue Loans");
        model.addAttribute("currentUrl", "/loans/overdue"); // Para paginación
        
        // Agregar información de paginación
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", overdueLoanPage.getTotalPages());
        model.addAttribute("totalElements", overdueLoanPage.getTotalElements());
        model.addAttribute("pageSize", size);
        
        return "loans/list";
    }

    @GetMapping("/my-loans")
    public String viewMyLoans(Model model, Principal principal,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int size) {
        
        userService.findByUsername(principal.getName()).ifPresent(user -> {
            // Configurar paginación para los préstamos del usuario
            Pageable pageable = PageRequest.of(page, size, Sort.by("loanDate").descending());
            Page<Loan> userLoanPage = loanService.findByUserPaginated(user, pageable);
            
            model.addAttribute("loanPage", userLoanPage);
            model.addAttribute("loans", userLoanPage.getContent());
            model.addAttribute("listTitle", "My Loans");
            model.addAttribute("currentUrl", "/loans/my-loans"); // Para paginación
            
            // Agregar información de paginación
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", userLoanPage.getTotalPages());
            model.addAttribute("totalElements", userLoanPage.getTotalElements());
            model.addAttribute("pageSize", size);
        });
        
        return "loans/list";
    }
    
    /**
     * AJAX endpoint para obtener información de juego y sesiones
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
            
            // Obtener sesiones próximas
            List<GameSessionInfo> upcomingSessions = loanService.findUpcomingSessionsForGame(game);
            
            // Calcular fecha sugerida de devolución (una semana por defecto)
            LocalDate defaultReturnDate = LocalDate.now().plusWeeks(1);
            
            // Verificar conflictos con sesiones
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
     * Response class for game info AJAX requests
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
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Game getGame() { return game; }
        public List<GameSessionInfo> getUpcomingSessions() { return upcomingSessions; }
        public LocalDate getSuggestedReturnDate() { return suggestedReturnDate; }
        public boolean isHasConflicts() { return hasConflicts; }
        public String getConflictMessage() { return conflictMessage; }
    }
}