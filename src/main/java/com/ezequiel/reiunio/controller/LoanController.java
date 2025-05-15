package com.ezequiel.reiunio.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ezequiel.reiunio.entity.Game;
import com.ezequiel.reiunio.entity.Loan;
import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.LoanStatus;
import com.ezequiel.reiunio.enums.ActionType;
import com.ezequiel.reiunio.service.AuditLogService;
import com.ezequiel.reiunio.service.GameService;
import com.ezequiel.reiunio.service.LoanService;
import com.ezequiel.reiunio.service.UserService;

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
    public String listLoans(Model model, @RequestParam(required = false) String status) {
        List<Loan> loans;
        
        if (status != null && !status.isEmpty()) {
            try {
                LoanStatus statusEnum = LoanStatus.valueOf(status.toUpperCase());
                loans = loanService.findByStatus(statusEnum);
                model.addAttribute("filterStatus", status);
            } catch (IllegalArgumentException e) {
                loans = loanService.findAll();
            }
        } else {
            loans = loanService.findAll();
        }
        
        model.addAttribute("loans", loans);
        model.addAttribute("statuses", LoanStatus.values());
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
    public String newLoan(Model model) {
        List<User> users = userService.findAll();
        List<Game> games = gameService.findByAvailable(true);
        
        model.addAttribute("users", users);
        model.addAttribute("games", games);
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
        
        if (!game.get().getAvailable()) {
            redirectAttributes.addFlashAttribute("error", "Game not available for loan");
            return "redirect:/loans/new";
        }
        
        try {
            Loan loan = loanService.createLoan(user.get(), game.get(), estimatedReturnDate);
            
            // Log the action
            userService.findByUsername(principal.getName()).ifPresent(adminUser -> 
                auditLogService.logChange(adminUser, ActionType.CREATION, "Loan", loan.getId(),
                    "Loan of game " + game.get().getName() + " to user " + user.get().getUsername())
            );
            
            redirectAttributes.addFlashAttribute("message", "Loan registered successfully");
            return "redirect:/loans";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/loans/new";
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
    public String viewOverdueLoans(Model model) {
        List<Loan> overdueLoans = loanService.findOverdueLoans();
        model.addAttribute("loans", overdueLoans);
        model.addAttribute("listTitle", "Overdue Loans");
        return "loans/list";
    }

    @GetMapping("/my-loans")
    public String viewMyLoans(Model model, Principal principal) {
        userService.findByUsername(principal.getName()).ifPresent(user -> {
            List<Loan> loans = loanService.findByUser(user);
            model.addAttribute("loans", loans);
            model.addAttribute("listTitle", "My Loans");
        });
        
        return "loans/list";
    }
}