package com.ezequiel.reiunio.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ezequiel.reiunio.entity.Loan;
import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.ActionType;
import com.ezequiel.reiunio.enums.LoanStatus;
import com.ezequiel.reiunio.enums.Role;
import com.ezequiel.reiunio.service.AuditLogService;
import com.ezequiel.reiunio.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String listUsers(Model model, 
                           @RequestParam(required = false) String role,
                           @RequestParam(required = false) String search,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Listing users with filters - role: {}, search: {}, page: {}, size: {}", 
                 role, search, page, size);
        
        // Create pageable with sorting by registration date (newest first)
        Pageable pageable = PageRequest.of(page, size, Sort.by("registrationDate").descending());
        
        Page<User> userPage;
        
        // Apply filters and get paginated results
        if (role != null && !role.isEmpty()) {
            try {
                Role roleEnum = Role.valueOf(role.toUpperCase());
                userPage = userService.findByRolePaginated(roleEnum, pageable);
                model.addAttribute("filterRole", role);
                log.debug("Found {} users with role {}", userPage.getTotalElements(), role);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid role filter: {}", role);
                userPage = userService.findAllPaginated(pageable);
            }
        } else if (search != null && !search.isEmpty()) {
            userPage = userService.searchUsersPaginated(search, pageable);
            model.addAttribute("filterSearch", search);
            log.debug("Found {} users matching search '{}'", userPage.getTotalElements(), search);
        } else {
            userPage = userService.findAllPaginated(pageable);
            log.debug("Found {} total users", userPage.getTotalElements());
        }
        
        // Add pagination data to model
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", userPage.getNumber());
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalElements", userPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("roles", Role.values());
        
        // Calculate statistics for all users (not just current page)
        addUserStatistics(model);
        
        log.debug("Returning {} users on page {} of {}", userPage.getContent().size(), 
                 page + 1, userPage.getTotalPages());
        
        return "users/list";
    }
    
    /**
     * Add user statistics to the model
     */
    private void addUserStatistics(Model model) {
        try {
            long totalUsers = userService.count();
            long adminCount = userService.countByRole(Role.ADMIN);
            long extendedUserCount = userService.countByRole(Role.EXTENDED_USER);
            long basicUserCount = userService.countByRole(Role.BASIC_USER);
            
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("adminCount", adminCount);
            model.addAttribute("extendedUserCount", extendedUserCount);
            model.addAttribute("basicUserCount", basicUserCount);
            
            log.debug("User statistics - Total: {}, Admin: {}, Extended: {}, Basic: {}", 
                     totalUsers, adminCount, extendedUserCount, basicUserCount);
        } catch (Exception e) {
            log.error("Error calculating user statistics", e);
            // Set default values in case of error
            model.addAttribute("totalUsers", 0L);
            model.addAttribute("adminCount", 0L);
            model.addAttribute("extendedUserCount", 0L);
            model.addAttribute("basicUserCount", 0L);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isCurrentUser(#id, authentication.principal.username)")
    public String viewUser(@PathVariable Long id, Model model, Principal principal) {
        Optional<User> user = userService.findById(id);
        
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            
            // Get user's loans if current user is viewing their own profile or is admin
            Optional<User> currentUser = userService.findByUsername(principal.getName());
            if (currentUser.isPresent() && 
                (currentUser.get().getRole() == Role.ADMIN || 
                 currentUser.get().getId().equals(user.get().getId()))) {
                
                List<Loan> userLoans = user.get().getLoans();
                model.addAttribute("userLoans", userLoans);
                
                // Calculate loan statistics
                long activeLoans = userLoans.stream()
                    .filter(loan -> loan.getStatus() == LoanStatus.ACTIVE)
                    .count();
                model.addAttribute("activeLoansCount", activeLoans);
                
                model.addAttribute("userSessions", user.get().getCreatedGameSessions());
            }
            
            return "users/detail";
        } else {
            return "redirect:/users";
        }
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", Role.values());
        model.addAttribute("editing", false);
        return "users/form";
    }

    @PostMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String createUser(@Valid @ModelAttribute("user") User user, 
                            BindingResult result, 
                            Model model, 
                            RedirectAttributes redirectAttributes, 
                            Principal principal) {
        
        log.debug("Creating new user: {}", user.getUsername());
        
        // Validar duplicados ANTES de otras validaciones
        validateUserDuplicates(user, result, null);
        
        // Validaciones adicionales
        validateUserData(user, result);
        
        if (result.hasErrors()) {
            log.debug("Validation errors found for new user: {}", result.getAllErrors());
            model.addAttribute("roles", Role.values());
            model.addAttribute("editing", false);
            return "users/form";
        }
        
        // Preparar usuario para guardar
        user.setRegistrationDate(LocalDate.now());
        
        try {
            User savedUser = userService.save(user);
            
            // Log the action
            userService.findByUsername(principal.getName()).ifPresent(adminUser -> 
                auditLogService.logChange(adminUser, ActionType.CREATION, "User", savedUser.getId(), 
                    "User created: " + savedUser.getUsername() + " with role " + savedUser.getRole())
            );
            
            log.info("User created successfully: {} with role {}", savedUser.getUsername(), savedUser.getRole());
            redirectAttributes.addFlashAttribute("message", 
                "User '" + savedUser.getUsername() + "' created successfully");
            return "redirect:/users";
            
        } catch (Exception e) {
            log.error("Error creating user: {}", user.getUsername(), e);
            model.addAttribute("error", "Error creating user: " + e.getMessage());
            model.addAttribute("roles", Role.values());
            model.addAttribute("editing", false);
            return "users/form";
        }
    }

    @GetMapping("/register")
    public String registrationForm(Model model) {
        model.addAttribute("user", new User());
        return "users/register";
    }

    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("user") User user, 
                                     BindingResult result,
                                     Model model, 
                                     RedirectAttributes redirectAttributes) {
        
        log.debug("Processing registration for user: {}", user.getUsername());
        
        // Validar duplicados
        validateUserDuplicates(user, result, null);
        
        // Validaciones adicionales
        validateUserData(user, result);
        
        if (result.hasErrors()) {
            log.debug("Registration validation errors for user: {}", result.getAllErrors());
            return "users/register";
        }
        
        try {
            // Set default role for public registration
            user.setRole(Role.BASIC_USER);
            user.setRegistrationDate(LocalDate.now());
            
            User savedUser = userService.save(user);
            log.info("User registered successfully: {}", savedUser.getUsername());
            
            redirectAttributes.addFlashAttribute("message", 
                "Registration completed successfully. You can now log in.");
            return "redirect:/login";
            
        } catch (Exception e) {
            log.error("Error registering user: {}", user.getUsername(), e);
            model.addAttribute("error", "Error registering user: " + e.getMessage());
            return "users/register";
        }
    }

    @GetMapping("/profile")
    public String userProfile(Model model, Principal principal) {
        Optional<User> user = userService.findByUsername(principal.getName());
        
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "users/profile";
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editUserForm(@PathVariable Long id, Model model) {
        Optional<User> user = userService.findById(id);
        
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            model.addAttribute("roles", Role.values());
            model.addAttribute("editing", true);
            return "users/form";
        } else {
            return "redirect:/users";
        }
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateUser(@PathVariable Long id, 
                           @Valid @ModelAttribute("user") User user,
                           BindingResult result, 
                           Model model, 
                           RedirectAttributes redirectAttributes,
                           Principal principal,
                           @RequestParam(required = false) String password) {
        
        log.debug("Updating user with ID: {}", id);
        
        Optional<User> existingUser = userService.findById(id);
        
        if (!existingUser.isPresent()) {
            log.warn("Attempt to update non-existent user with ID: {}", id);
            redirectAttributes.addFlashAttribute("error", "User not found");
            return "redirect:/users";
        }
        
        // Validar duplicados para edición
        validateUserDuplicates(user, result, id);
        
        // Validaciones adicionales
        validateUserData(user, result);
        
        // Validar contraseña solo si se proporciona
        if (password != null && !password.trim().isEmpty()) {
            validatePassword(password, result);
        }
        
        if (result.hasErrors()) {
            log.debug("Validation errors found for user update: {}", result.getAllErrors());
            model.addAttribute("roles", Role.values());
            model.addAttribute("editing", true);
            return "users/form";
        }
        
        try {
            User currentUser = existingUser.get();
            
            // Keep original registration date and password if not changed
            user.setRegistrationDate(currentUser.getRegistrationDate());
            if (password == null || password.trim().isEmpty()) {
                user.setPassword(currentUser.getPassword());
            } else {
                // La contraseña será encriptada en el service
                user.setPassword(password);
            }
            
            User savedUser = userService.save(user);
            
            // Log the action
            userService.findByUsername(principal.getName()).ifPresent(adminUser -> 
                auditLogService.logChange(adminUser, ActionType.MODIFICATION, "User", savedUser.getId(), 
                    "User modified: " + savedUser.getUsername() + " - role: " + savedUser.getRole())
            );
            
            log.info("User updated successfully: {} with role {}", savedUser.getUsername(), savedUser.getRole());
            redirectAttributes.addFlashAttribute("message", 
                "User '" + savedUser.getUsername() + "' updated successfully");
            return "redirect:/users";
            
        } catch (Exception e) {
            log.error("Error updating user with ID: {}", id, e);
            model.addAttribute("error", "Error updating user: " + e.getMessage());
            model.addAttribute("roles", Role.values());
            model.addAttribute("editing", true);
            return "users/form";
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(@PathVariable Long id, 
                            RedirectAttributes redirectAttributes, 
                            Principal principal) {
        
        log.debug("Attempting to delete user with ID: {}", id);
        
        Optional<User> user = userService.findById(id);
        
        if (user.isPresent()) {
            User currentUser = user.get();
            
            // Prevent admin from deleting themselves
            Optional<User> adminUser = userService.findByUsername(principal.getName());
            if (adminUser.isPresent() && adminUser.get().getId().equals(id)) {
                log.warn("Admin {} attempted to delete their own account", principal.getName());
                redirectAttributes.addFlashAttribute("error", "You cannot delete your own account");
                return "redirect:/users";
            }
            
            try {
                String deletedUsername = currentUser.getUsername();
                userService.deleteById(id);
                
                // Log the action
                adminUser.ifPresent(admin -> 
                    auditLogService.logChange(admin, ActionType.DELETION, "User", id, 
                        "User deleted: " + deletedUsername)
                );
                
                log.info("User deleted successfully: {}", deletedUsername);
                redirectAttributes.addFlashAttribute("message", 
                    "User '" + deletedUsername + "' deleted successfully");
                    
            } catch (Exception e) {
                log.error("Error deleting user with ID: {}", id, e);
                redirectAttributes.addFlashAttribute("error", 
                    "Cannot delete user '" + currentUser.getUsername() + "'. " +
                    "User may have associated data that prevents deletion.");
            }
        } else {
            log.warn("Attempt to delete non-existent user with ID: {}", id);
            redirectAttributes.addFlashAttribute("error", "User not found");
        }
        
        return "redirect:/users";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@Valid @ModelAttribute("user") User user,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes,
                               Principal principal) {
        
        Optional<User> currentUser = userService.findByUsername(principal.getName());
        
        if (!currentUser.isPresent()) {
            return "redirect:/login";
        }
        
        User existingUser = currentUser.get();
        
        // Validar email duplicado (excluyendo el usuario actual)
        if (!existingUser.getEmail().equals(user.getEmail()) && 
            userService.existsByEmail(user.getEmail())) {
            result.rejectValue("email", "error.user", "Email already registered");
        }
        
        if (result.hasErrors()) {
            model.addAttribute("user", existingUser);
            return "users/profile";
        }
        
        try {
            // Update only allowed fields
            existingUser.setEmail(user.getEmail());
            existingUser.setFirstName(user.getFirstName());
            existingUser.setLastName(user.getLastName());
            
            // Update password only if provided
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                existingUser.setPassword(user.getPassword());
            }
            
            userService.save(existingUser);
            
            redirectAttributes.addFlashAttribute("message", "Profile updated successfully");
            return "redirect:/users/profile";
            
        } catch (Exception e) {
            log.error("Error updating profile for user: {}", principal.getName(), e);
            model.addAttribute("error", "Error updating profile: " + e.getMessage());
            model.addAttribute("user", existingUser);
            return "users/profile";
        }
    }

    /**
     * Check if username is available
     */
    @GetMapping("/api/users/check-username")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkUsernameAvailability(
            @RequestParam String username,
            @RequestParam(required = false) Long excludeId) {
        
        log.debug("Checking username availability: {} (excluding ID: {})", username, excludeId);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate username format first
            if (username == null || username.trim().isEmpty()) {
                response.put("available", false);
                response.put("message", "Username cannot be empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            String trimmedUsername = username.trim();
            
            // Check pattern
            if (!trimmedUsername.matches("^[a-zA-Z0-9_.-]+$")) {
                response.put("available", false);
                response.put("message", "Username can only contain letters, numbers, dots, hyphens and underscores");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check length
            if (trimmedUsername.length() < 3 || trimmedUsername.length() > 50) {
                response.put("available", false);
                response.put("message", "Username must be between 3 and 50 characters");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check if username exists
            Optional<User> existingUser = userService.findByUsername(trimmedUsername);
            
            boolean available = true;
            if (existingUser.isPresent()) {
                // If excludeId is provided (editing mode), check if it's the same user
                if (excludeId == null || !existingUser.get().getId().equals(excludeId)) {
                    available = false;
                }
            }
            
            response.put("available", available);
            response.put("username", trimmedUsername);
            
            if (available) {
                response.put("message", "Username is available");
            } else {
                response.put("message", "Username is already taken");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error checking username availability", e);
            response.put("available", false);
            response.put("message", "Error checking username availability");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Check if email is available
     */
    @GetMapping("/api/users/check-email")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkEmailAvailability(
            @RequestParam String email,
            @RequestParam(required = false) Long excludeId) {
        
        log.debug("Checking email availability: {} (excluding ID: {})", email, excludeId);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate email format first
            if (email == null || email.trim().isEmpty()) {
                response.put("available", false);
                response.put("message", "Email cannot be empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            String trimmedEmail = email.trim().toLowerCase();
            
            // Check email pattern
            if (!trimmedEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                response.put("available", false);
                response.put("message", "Please enter a valid email address");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check if email exists
            Optional<User> existingUser = userService.findByEmail(trimmedEmail);
            
            boolean available = true;
            if (existingUser.isPresent()) {
                // If excludeId is provided (editing mode), check if it's the same user
                if (excludeId == null || !existingUser.get().getId().equals(excludeId)) {
                    available = false;
                }
            }
            
            response.put("available", available);
            response.put("email", trimmedEmail);
            
            if (available) {
                response.put("message", "Email is available");
            } else {
                response.put("message", "Email is already registered");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error checking email availability", e);
            response.put("available", false);
            response.put("message", "Error checking email availability");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Valida duplicados de username y email
     * @param user Usuario a validar
     * @param result BindingResult para agregar errores
     * @param excludeUserId ID del usuario a excluir (para edición), null para nuevo usuario
     */
    private void validateUserDuplicates(User user, BindingResult result, Long excludeUserId) {
        // Validar username duplicado
        if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
            Optional<User> existingUserByUsername = userService.findByUsername(user.getUsername().trim());
            if (existingUserByUsername.isPresent()) {
                // Si es edición, verificar que no sea el mismo usuario
                if (excludeUserId == null || !existingUserByUsername.get().getId().equals(excludeUserId)) {
                    result.rejectValue("username", "error.user", 
                        "Username '" + user.getUsername() + "' is already taken");
                    log.warn("Attempt to create/update user with duplicate username: {}", user.getUsername());
                }
            }
        }
        
        // Validar email duplicado
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            Optional<User> existingUserByEmail = userService.findByEmail(user.getEmail().trim().toLowerCase());
            if (existingUserByEmail.isPresent()) {
                // Si es edición, verificar que no sea el mismo usuario
                if (excludeUserId == null || !existingUserByEmail.get().getId().equals(excludeUserId)) {
                    result.rejectValue("email", "error.user", 
                        "Email '" + user.getEmail() + "' is already registered");
                    log.warn("Attempt to create/update user with duplicate email: {}", user.getEmail());
                }
            }
        }
    }
    
    /**
     * Validaciones adicionales de datos del usuario
     */
    private void validateUserData(User user, BindingResult result) {
        // Validar username pattern
        if (user.getUsername() != null) {
            String username = user.getUsername().trim();
            if (!username.matches("^[a-zA-Z0-9_.-]+$")) {
                result.rejectValue("username", "error.user", 
                    "Username can only contain letters, numbers, dots, hyphens and underscores");
            }
            if (username.length() < 3 || username.length() > 50) {
                result.rejectValue("username", "error.user", 
                    "Username must be between 3 and 50 characters");
            }
        }
        
        // Validar email format
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            String email = user.getEmail().trim();
            if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                result.rejectValue("email", "error.user", "Please enter a valid email address");
            }
        }
        
        // Validar nombres (solo letras y espacios)
        if (user.getFirstName() != null && !user.getFirstName().trim().isEmpty()) {
            if (!user.getFirstName().matches("^[a-zA-ZÀ-ÿ\\s]+$")) {
                result.rejectValue("firstName", "error.user", 
                    "First name can only contain letters and spaces");
            }
        }
        
        if (user.getLastName() != null && !user.getLastName().trim().isEmpty()) {
            if (!user.getLastName().matches("^[a-zA-ZÀ-ÿ\\s]*$")) {
                result.rejectValue("lastName", "error.user", 
                    "Last name can only contain letters and spaces");
            }
        }
    }
    
    /**
     * Validar contraseña - REQUISITOS RELAJADOS
     */
    private void validatePassword(String password, BindingResult result) {
        if (password == null || password.trim().isEmpty()) {
            return; // Opcional en edición
        }
        
        String pwd = password.trim();
        
        // Validar longitud mínima
        if (pwd.length() < 8) {
            result.rejectValue("password", "error.user", 
                "Password must be at least 8 characters long");
            return;
        }
        
        // Validar que contenga letras
        boolean hasLetter = pwd.matches(".*[a-zA-Z].*");
        
        // Validar que contenga números O símbolos
        boolean hasNumberOrSymbol = pwd.matches(".*\\d.*") || pwd.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
        
        if (!hasLetter) {
            result.rejectValue("password", "error.user", 
                "Password must contain at least one letter");
            return;
        }
        
        if (!hasNumberOrSymbol) {
            result.rejectValue("password", "error.user", 
                "Password must contain at least one number or symbol");
        }
    }
}