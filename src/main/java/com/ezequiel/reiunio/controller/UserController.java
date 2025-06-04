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

/**
 * Controller responsible for managing user operations in the Reiunio application.
 * This controller handles the complete user lifecycle including registration, authentication,
 * profile management, and administrative user operations. It provides comprehensive user
 * management capabilities with role-based access control and detailed audit logging.
 * 
 * <p>Key features include:</p>
 * <ul>
 *   <li>User registration and self-service profile management</li>
 *   <li>Administrative user management with full CRUD operations</li>
 *   <li>Paginated user listing with filtering and search capabilities</li>
 *   <li>User statistics and reporting functionality</li>
 *   <li>Role-based access control with multiple user roles</li>
 *   <li>Real-time username and email availability checking</li>
 *   <li>Comprehensive validation for user data integrity</li>
 *   <li>Audit logging for all user-related operations</li>
 *   <li>Profile photo management integration</li>
 *   <li>User loan and game session tracking</li>
 * </ul>
 * 
 * <p>The controller supports three user roles:</p>
 * <ul>
 *   <li><strong>ADMIN:</strong> Full system access and user management</li>
 *   <li><strong>EXTENDED_USER:</strong> Enhanced privileges for advanced features</li>
 *   <li><strong>BASIC_USER:</strong> Standard user access for regular features</li>
 * </ul>
 * 
 * <p>Security is enforced through Spring Security annotations ensuring that users can only
 * access and modify data appropriate to their role and ownership permissions.</p>
 */
@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    /** Service for user-related operations and data management */
    private final UserService userService;
    
    /** Service for logging audit events and user actions */
    private final AuditLogService auditLogService;

    /**
     * Displays a paginated and filtered list of users with comprehensive statistics.
     * This administrative endpoint provides filtering capabilities by role and search terms,
     * along with user statistics for system overview. Results are paginated for performance
     * and include detailed user information.
     * 
     * <p>Available filtering options:</p>
     * <ul>
     *   <li><strong>Role filtering:</strong> Show users by specific role (ADMIN, EXTENDED_USER, BASIC_USER)</li>
     *   <li><strong>Text search:</strong> Search across usernames, emails, and names</li>
     *   <li><strong>Pagination:</strong> Configurable page size with default sorting by registration date</li>
     * </ul>
     * 
     * <p>The endpoint also provides comprehensive user statistics including total counts
     * by role, which are useful for system administration and reporting.</p>
     * 
     * @param model the Spring MVC model for passing data to the view
     * @param role optional filter to show users with specific role
     * @param search optional search term to filter users by username, email, or name
     * @param page the page number for pagination (0-based, defaults to 0)
     * @param size the number of items per page (defaults to 20)
     * @return the name of the users list view template
     */
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
        Sort sort = Sort.by("registrationDate").descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<User> userPage;
        
        // Normalizar parámetros
        String roleFilter = (role != null && !role.trim().isEmpty()) ? role.trim() : null;
        String searchFilter = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        
        // NUEVA LÓGICA: Combinar filtros según lo que esté presente
        if (roleFilter != null && searchFilter != null) {
            // AMBOS filtros: buscar por término dentro del rol específico
            try {
                Role roleEnum = Role.valueOf(roleFilter.toUpperCase());
                userPage = userService.searchUsersByRoleAndTerm(roleEnum, searchFilter, pageable);
                model.addAttribute("filterRole", role);
                model.addAttribute("filterSearch", search);
                log.debug("Found {} users with role {} and search term '{}'", 
                         userPage.getTotalElements(), roleFilter, searchFilter);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid role filter: {}, falling back to search only", roleFilter);
                userPage = userService.searchUsersPaginated(searchFilter, pageable);
                model.addAttribute("filterSearch", search);
            }
        } else if (roleFilter != null) {
            // Solo filtro por rol
            try {
                Role roleEnum = Role.valueOf(roleFilter.toUpperCase());
                userPage = userService.findByRolePaginated(roleEnum, pageable);
                model.addAttribute("filterRole", role);
                log.debug("Found {} users with role {}", userPage.getTotalElements(), roleFilter);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid role filter: {}", roleFilter);
                userPage = userService.findAllPaginated(pageable);
            }
        } else if (searchFilter != null) {
            // Solo búsqueda por término
            userPage = userService.searchUsersPaginated(searchFilter, pageable);
            model.addAttribute("filterSearch", search);
            log.debug("Found {} users matching search '{}'", userPage.getTotalElements(), searchFilter);
        } else {
            // Sin filtros: todos los usuarios
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
     * Calculates and adds comprehensive user statistics to the model.
     * This private helper method computes various user metrics including total counts
     * by role, which are displayed in the administrative interface for system overview.
     * 
     * <p>Statistics included:</p>
     * <ul>
     *   <li>Total number of users across all roles</li>
     *   <li>Count of administrators</li>
     *   <li>Count of extended users</li>
     *   <li>Count of basic users</li>
     * </ul>
     * 
     * <p>In case of errors during calculation, default values of 0 are set to prevent
     * view rendering issues.</p>
     * 
     * @param model the Spring MVC model to add statistics attributes to
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

    /**
     * Displays the detailed view of a specific user with associated data.
     * This endpoint shows comprehensive user information including personal details,
     * role information, and related data such as loans and game sessions. Access is
     * restricted to administrators and the user themselves.
     * 
     * <p>The view includes:</p>
     * <ul>
     *   <li>Complete user profile information</li>
     *   <li>User's loan history and statistics (if authorized)</li>
     *   <li>User's created game sessions (if authorized)</li>
     *   <li>Active loan count for quick overview</li>
     *   <li>Profile photo if available</li>
     * </ul>
     * 
     * <p>Data visibility is controlled based on the viewer's role and relationship
     * to the target user, ensuring privacy and appropriate access control.</p>
     * 
     * @param id the unique identifier of the user to display
     * @param model the Spring MVC model for passing data to the view
     * @param principal the currently authenticated user's security principal
     * @return the name of the user detail view template, or redirect to users list if not found
     */
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

    /**
     * Displays the form for creating a new user by an administrator.
     * This endpoint is restricted to administrators and provides a form with all
     * necessary fields for user creation, including role selection.
     * 
     * @param model the Spring MVC model for passing data to the view
     * @return the name of the user form view template
     */
    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", Role.values());
        model.addAttribute("editing", false);
        return "users/form";
    }

    /**
     * Handles the creation of a new user by an administrator with comprehensive validation.
     * This endpoint processes user creation forms with extensive validation including
     * duplicate checking, password strength requirements, and data format validation.
     * 
     * <p>The creation process includes:</p>
     * <ul>
     *   <li>Comprehensive form validation with custom error messages</li>
     *   <li>Duplicate username and email prevention</li>
     *   <li>Password strength validation with specific requirements</li>
     *   <li>User data format validation (names, email format)</li>
     *   <li>Automatic registration date assignment</li>
     *   <li>Audit logging of the creation action</li>
     * </ul>
     * 
     * <p>Password requirements:</p>
     * <ul>
     *   <li>Minimum 8 characters length</li>
     *   <li>Must contain at least one letter</li>
     *   <li>Must contain at least one number or symbol</li>
     * </ul>
     * 
     * @param user the user entity to be created, validated through Spring Validation
     * @param result the binding result containing validation errors, if any
     * @param model the Spring MVC model for passing data to the view
     * @param redirectAttributes Spring MVC attributes for passing messages between requests
     * @param principal the currently authenticated user's security principal
     * @return redirect to users list on success, or form view on validation errors
     */
    @PostMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String createUser(@Valid @ModelAttribute("user") User user, 
                            BindingResult result, 
                            Model model, 
                            RedirectAttributes redirectAttributes, 
                            Principal principal) {
        
        log.debug("Creating new user: {}", user.getUsername());
        
        // Manual password validation for new users
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            result.rejectValue("password", "error.user", "Password is required for new users");
        } else {
            validatePassword(user.getPassword(), result);
        }
        
        // Validate duplicates BEFORE other validations
        validateUserDuplicates(user, result, null);
        
        // Additional validations
        validateUserData(user, result);
        
        if (result.hasErrors()) {
            log.debug("Validation errors found for new user: {}", result.getAllErrors());
            model.addAttribute("roles", Role.values());
            model.addAttribute("editing", false);
            return "users/form";
        }
        
        // Prepare user for saving
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

    /**
     * Displays the public user registration form.
     * This endpoint provides a self-service registration form for new users to create
     * their own accounts with basic user privileges.
     * 
     * @param model the Spring MVC model for passing data to the view
     * @return the name of the user registration view template
     */
    @GetMapping("/register")
    public String registrationForm(Model model) {
        model.addAttribute("user", new User());
        return "users/register";
    }

    /**
     * Handles public user registration with automatic basic user role assignment.
     * This endpoint processes self-service user registration with the same validation
     * as administrative user creation, but automatically assigns basic user role and
     * redirects to the login page upon successful registration.
     * 
     * <p>The registration process includes:</p>
     * <ul>
     *   <li>Same comprehensive validation as administrative user creation</li>
     *   <li>Automatic assignment of BASIC_USER role</li>
     *   <li>Automatic registration date assignment</li>
     *   <li>Redirect to login page for immediate access</li>
     * </ul>
     * 
     * @param user the user entity to be registered, validated through Spring Validation
     * @param result the binding result containing validation errors, if any
     * @param model the Spring MVC model for passing data to the view
     * @param redirectAttributes Spring MVC attributes for passing messages between requests
     * @return redirect to login on success, or registration form on validation errors
     */
    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("user") User user, 
                                     BindingResult result,
                                     Model model, 
                                     RedirectAttributes redirectAttributes) {
        
        log.debug("Processing registration for user: {}", user.getUsername());
        
        // Manual password validation for registration
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            result.rejectValue("password", "error.user", "Password is required");
        } else {
            validatePassword(user.getPassword(), result);
        }
        
        // Validate duplicates
        validateUserDuplicates(user, result, null);
        
        // Additional validations
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

    /**
     * Displays the current user's profile page.
     * This endpoint shows the authenticated user's profile information in a read-only
     * format with options to edit specific fields.
     * 
     * @param model the Spring MVC model for passing data to the view
     * @param principal the currently authenticated user's security principal
     * @return the name of the user profile view template, or redirect to home if user not found
     */
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

    /**
     * Displays the form for editing an existing user by an administrator.
     * This endpoint is restricted to administrators and pre-populates the form with
     * current user data for modification.
     * 
     * @param id the unique identifier of the user to edit
     * @param model the Spring MVC model for passing data to the view
     * @return the name of the user form view template, or redirect to users list if not found
     */
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

    /**
     * Handles the update of an existing user by an administrator with comprehensive validation.
     * This endpoint processes user modification forms with special handling for password updates
     * and preservation of certain immutable fields like username and registration date.
     * 
     * <p>The update process includes:</p>
     * <ul>
     *   <li>Validation excluding the current user from duplicate checks</li>
     *   <li>Optional password update (only if provided)</li>
     *   <li>Preservation of username and registration date</li>
     *   <li>Profile photo path preservation</li>
     *   <li>Role modification capabilities</li>
     *   <li>Audit logging of modifications</li>
     * </ul>
     * 
     * <p>Special considerations:</p>
     * <ul>
     *   <li>Username cannot be changed during administrative edit</li>
     *   <li>Password is optional - if not provided, current password is retained</li>
     *   <li>Registration date and profile photo are preserved</li>
     * </ul>
     * 
     * @param id the unique identifier of the user to update
     * @param user the updated user entity with new values
     * @param result the binding result containing validation errors, if any
     * @param model the Spring MVC model for passing data to the view
     * @param redirectAttributes Spring MVC attributes for passing messages between requests
     * @param principal the currently authenticated user's security principal
     * @param password optional new password for the user
     * @return redirect to users list on success, or form view on validation errors
     */
    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateUser(@PathVariable Long id, 
                           @Valid @ModelAttribute("user") User user,
                           BindingResult result, 
                           Model model, 
                           RedirectAttributes redirectAttributes,
                           Principal principal,
                           @RequestParam(required = false) String password) {
        
        log.debug("Admin updating user with ID: {}", id);
        
        Optional<User> existingUserOpt = userService.findById(id);
        
        if (!existingUserOpt.isPresent()) {
            log.warn("Attempt to update non-existent user with ID: {}", id);
            redirectAttributes.addFlashAttribute("error", "User not found");
            return "redirect:/users";
        }
        
        User existingUser = existingUserOpt.get();
        log.debug("Updating user: {} ({})", existingUser.getUsername(), existingUser.getEmail());
        
        // Validate duplicates for editing (excluding current user)
        validateUserDuplicates(user, result, id);
        
        // More permissive data validations
        validateUserDataPermissive(user, result);
        
        // Validate password only if provided
        if (password != null && !password.trim().isEmpty()) {
            validatePassword(password, result);
        }
        
        if (result.hasErrors()) {
            log.debug("Validation errors found for user update: {}", result.getAllErrors());
            model.addAttribute("roles", Role.values());
            model.addAttribute("editing", true);
            // Ensure the user in model has correct ID
            user.setId(id);
            model.addAttribute("user", user);
            return "users/form";
        }
        
        try {
            // Copy data from existing user and update only necessary fields
            User updatedUser = new User();
            updatedUser.setId(id);
            updatedUser.setUsername(existingUser.getUsername()); // Username doesn't change in admin edit
            updatedUser.setEmail(user.getEmail().trim());
            updatedUser.setFirstName(user.getFirstName().trim());
            updatedUser.setLastName(user.getLastName() != null ? user.getLastName().trim() : null);
            updatedUser.setRole(user.getRole());
            updatedUser.setRegistrationDate(existingUser.getRegistrationDate());
            updatedUser.setProfilePhotoPath(existingUser.getProfilePhotoPath());
            
            // Handle password correctly
            if (password != null && !password.trim().isEmpty()) {
                updatedUser.setPassword(password.trim()); // Will be encrypted in service
                log.debug("Password will be updated for user: {}", existingUser.getUsername());
            } else {
                updatedUser.setPassword(existingUser.getPassword()); // Keep current password
                log.debug("Password kept unchanged for user: {}", existingUser.getUsername());
            }
            
            User savedUser = userService.save(updatedUser);
            
            // Log the action
            userService.findByUsername(principal.getName()).ifPresent(adminUser -> 
                auditLogService.logChange(adminUser, ActionType.MODIFICATION, "User", savedUser.getId(), 
                    "User modified: " + savedUser.getUsername() + " - role: " + savedUser.getRole())
            );
            
            log.info("User updated successfully by admin: {} - Target: {}", principal.getName(), savedUser.getUsername());
            redirectAttributes.addFlashAttribute("message", 
                "User '" + savedUser.getUsername() + "' updated successfully");
            return "redirect:/users";
            
        } catch (Exception e) {
            log.error("Error updating user with ID: {}", id, e);
            model.addAttribute("error", "Error updating user: " + e.getMessage());
            model.addAttribute("roles", Role.values());
            model.addAttribute("editing", true);
            user.setId(id); // Ensure ID is present
            model.addAttribute("user", user);
            return "users/form";
        }
    }

    /**
     * Handles the deletion of a user by an administrator with safety checks.
     * This endpoint performs user deletion with comprehensive validation to prevent
     * accidental deletion of critical accounts and handles referential integrity issues.
     * 
     * <p>Safety measures include:</p>
     * <ul>
     *   <li>Prevention of administrators deleting their own accounts</li>
     *   <li>Referential integrity checks for associated data</li>
     *   <li>Comprehensive error handling for constraint violations</li>
     *   <li>Audit logging of deletion actions</li>
     *   <li>Informative error messages for failed deletions</li>
     * </ul>
     * 
     * <p>If the user has associated data (loans, game sessions, etc.), the deletion
     * will fail with an informative error message guiding the administrator on
     * necessary cleanup steps.</p>
     * 
     * @param id the unique identifier of the user to delete
     * @param redirectAttributes Spring MVC attributes for passing messages between requests
     * @param principal the currently authenticated user's security principal
     * @return redirect to users list with appropriate success or error message
     */
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

    /**
     * Handles user profile updates by the authenticated user with limited field access.
     * This endpoint allows users to update their own profile information including
     * first name, last name, and password, while preserving username and email integrity.
     * 
     * <p>Editable fields for profile updates:</p>
     * <ul>
     *   <li><strong>First Name:</strong> Required field with character validation</li>
     *   <li><strong>Last Name:</strong> Optional field with character validation</li>
     *   <li><strong>Password:</strong> Optional update with strength requirements</li>
     * </ul>
     * 
     * <p>Non-editable fields (preserved automatically):</p>
     * <ul>
     *   <li>Username - cannot be changed by users</li>
     *   <li>Email - requires administrative action to change</li>
     *   <li>Role - determined by administrators</li>
     *   <li>Registration date - historical data</li>
     * </ul>
     * 
     * <p>The validation is more permissive than administrative updates, focusing only
     * on the fields that users can actually modify.</p>
     * 
     * @param user the user entity with updated profile information
     * @param result the binding result containing validation errors, if any
     * @param model the Spring MVC model for passing data to the view
     * @param redirectAttributes Spring MVC attributes for passing messages between requests
     * @param principal the currently authenticated user's security principal
     * @param password optional new password for the user
     * @return redirect to profile page on success, or profile view on validation errors
     */
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute("user") User user,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes,
                               Principal principal,
                               @RequestParam(required = false) String password) {
        
        log.debug("Updating profile for user: {}", principal.getName());
        log.debug("Received user data - firstName: {}, lastName: {}, password provided: {}", 
                 user.getFirstName(), user.getLastName(), 
                 (password != null && !password.trim().isEmpty()));
        
        Optional<User> currentUserOpt = userService.findByUsername(principal.getName());
        
        if (!currentUserOpt.isPresent()) {
            log.error("Current user not found: {}", principal.getName());
            return "redirect:/login";
        }
        
        User existingUser = currentUserOpt.get();
        log.debug("Current user found: {} (ID: {})", existingUser.getUsername(), existingUser.getId());
        
        // Manual validation for profile update (only for editable fields)
        boolean hasErrors = false;
        
        // Validate firstName (required)
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            result.rejectValue("firstName", "error.user", "First name is required");
            hasErrors = true;
            log.debug("FirstName validation failed: empty");
        } else {
            String firstName = user.getFirstName().trim();
            if (!firstName.matches("^[a-zA-ZÀ-ÿÑñÇç\\s'-]+$")) {
                result.rejectValue("firstName", "error.user", 
                    "First name can only contain letters, spaces, hyphens and apostrophes");
                hasErrors = true;
                log.debug("FirstName validation failed: invalid characters");
            }
        }
        
        // Validate lastName (optional, but if provided must be valid)
        if (user.getLastName() != null && !user.getLastName().trim().isEmpty()) {
            String lastName = user.getLastName().trim();
            if (!lastName.matches("^[a-zA-ZÀ-ÿÑñÇç\\s'-]*$")) {
                result.rejectValue("lastName", "error.user", 
                    "Last name can only contain letters, spaces, hyphens and apostrophes");
                hasErrors = true;
                log.debug("LastName validation failed: invalid characters");
            }
        }
        
        // Validate password (only if provided)
        if (password != null && !password.trim().isEmpty()) {
            String pwd = password.trim();
            
            if (pwd.length() < 8) {
                result.rejectValue("password", "error.user", 
                    "Password must be at least 8 characters long");
                hasErrors = true;
                log.debug("Password validation failed: too short");
            } else {
                // Check for letters
                boolean hasLetter = pwd.matches(".*[a-zA-Z].*");
                // Check for numbers or symbols
                boolean hasNumberOrSymbol = pwd.matches(".*\\d.*") || pwd.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
                
                if (!hasLetter) {
                    result.rejectValue("password", "error.user", 
                        "Password must contain at least one letter");
                    hasErrors = true;
                    log.debug("Password validation failed: no letters");
                } else if (!hasNumberOrSymbol) {
                    result.rejectValue("password", "error.user", 
                        "Password must contain at least one number or symbol");
                    hasErrors = true;
                    log.debug("Password validation failed: no numbers or symbols");
                }
            }
        }
        
        if (hasErrors) {
            log.debug("Profile validation errors found");
            // Return the existing user data to maintain the form state
            model.addAttribute("user", existingUser);
            return "users/profile";
        }
        
        try {
            // Update only the editable fields (firstName, lastName, password)
            // Keep username and email unchanged
            
            if (user.getFirstName() != null && !user.getFirstName().trim().isEmpty()) {
                existingUser.setFirstName(user.getFirstName().trim());
                log.debug("Updated firstName: {}", user.getFirstName().trim());
            }
            
            // LastName can be null or empty
            if (user.getLastName() != null && !user.getLastName().trim().isEmpty()) {
                existingUser.setLastName(user.getLastName().trim());
                log.debug("Updated lastName: {}", user.getLastName().trim());
            } else {
                existingUser.setLastName(null);
                log.debug("Cleared lastName");
            }
            
            // Update password only if provided
            if (password != null && !password.trim().isEmpty()) {
                existingUser.setPassword(password.trim()); // Will be encrypted in service
                log.debug("Password updated for user: {}", existingUser.getUsername());
            }
            
            User savedUser = userService.save(existingUser);
            log.info("Profile updated successfully for user: {}", savedUser.getUsername());
            
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
     * AJAX endpoint for checking username availability in real-time.
     * This endpoint provides immediate feedback on username availability during form entry,
     * improving user experience by preventing submission of forms with unavailable usernames.
     * 
     * <p>The endpoint performs comprehensive validation including:</p>
     * <ul>
     *   <li>Username format validation (letters, numbers, dots, hyphens, underscores)</li>
     *   <li>Length validation (3-50 characters)</li>
     *   <li>Duplicate checking with optional exclusion for editing scenarios</li>
     *   <li>Real-time feedback with descriptive messages</li>
     * </ul>
     * 
     * <p>The excludeId parameter allows the same username to be considered available
     * when editing an existing user, preventing false positives during updates.</p>
     * 
     * @param username the username to check for availability
     * @param excludeId optional user ID to exclude from duplicate checking (for edit scenarios)
     * @return ResponseEntity containing availability status and descriptive message
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
     * AJAX endpoint for checking email availability in real-time.
     * This endpoint provides immediate feedback on email availability during form entry,
     * with special handling for different contexts (registration, profile edit, admin edit).
     * 
     * <p>The endpoint performs comprehensive validation including:</p>
     * <ul>
     *   <li>Email format validation using standard email pattern</li>
     *   <li>Case-insensitive duplicate checking</li>
     *   <li>Context-aware availability checking for different scenarios</li>
     *   <li>Real-time feedback with descriptive messages</li>
     * </ul>
     * 
     * <p>Context handling:</p>
     * <ul>
     *   <li><strong>New registration:</strong> Email must be completely unique</li>
     *   <li><strong>Profile edit:</strong> User can keep their current email</li>
     *   <li><strong>Admin edit:</strong> Uses excludeId to allow same user's email</li>
     * </ul>
     * 
     * @param email the email address to check for availability
     * @param excludeId optional user ID to exclude from duplicate checking (for edit scenarios)
     * @param principal the currently authenticated user's security principal (for context)
     * @return ResponseEntity containing availability status and descriptive message
     */
    @GetMapping("/api/users/check-email")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkEmailAvailability(
            @RequestParam String email,
            @RequestParam(required = false) Long excludeId,
            Principal principal) {
        
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
                if (excludeId != null) {
                    // If excludeId is provided (editing mode), check if it's the same user
                    available = existingUser.get().getId().equals(excludeId);
                } else if (principal != null) {
                    // If no excludeId but we have a principal (profile edit), 
                    // check if it's the current user's email
                    available = existingUser.get().getUsername().equals(principal.getName());
                } else {
                    // New user registration
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
     * Validates for duplicate usernames and emails with context-aware exclusion.
     * This private helper method performs comprehensive duplicate checking while allowing
     * exclusion of a specific user ID for editing scenarios.
     * 
     * <p>The method checks:</p>
     * <ul>
     *   <li>Username uniqueness (if username is provided and not readonly)</li>
     *   <li>Email uniqueness with case-insensitive comparison</li>
     *   <li>Proper exclusion handling for user editing scenarios</li>
     * </ul>
     * 
     * @param user the user entity to validate for duplicates
     * @param result the BindingResult to add validation errors to
     * @param excludeUserId the ID of the user to exclude from duplicate checking (null for new users)
     */
    private void validateUserDuplicates(User user, BindingResult result, Long excludeUserId) {
        // Validate username duplicate only if not readonly
        if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
            Optional<User> existingUserByUsername = userService.findByUsername(user.getUsername().trim());
            if (existingUserByUsername.isPresent()) {
                // If editing, verify it's not the same user
                if (excludeUserId == null || !existingUserByUsername.get().getId().equals(excludeUserId)) {
                    result.rejectValue("username", "error.user", 
                        "Username '" + user.getUsername() + "' is already taken");
                    log.warn("Attempt to create/update user with duplicate username: {}", user.getUsername());
                }
            }
        }
        
        // Validate email duplicate
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            Optional<User> existingUserByEmail = userService.findByEmail(user.getEmail().trim().toLowerCase());
            if (existingUserByEmail.isPresent()) {
                // If editing, verify it's not the same user
                if (excludeUserId == null || !existingUserByEmail.get().getId().equals(excludeUserId)) {
                    result.rejectValue("email", "error.user", 
                        "Email '" + user.getEmail() + "' is already registered");
                    log.warn("Attempt to create/update user with duplicate email: {}", user.getEmail());
                }
            }
        }
    }
    
    /**
     * Performs comprehensive validation of user data with strict requirements.
     * This private helper method validates all user data fields according to strict
     * business rules for new user creation and comprehensive administrative updates.
     * 
     * <p>Validation rules include:</p>
     * <ul>
     *   <li><strong>Username:</strong> 3-50 characters, letters/numbers/dots/hyphens/underscores only</li>
     *   <li><strong>Email:</strong> Standard email format validation</li>
     *   <li><strong>First Name:</strong> Required, letters/spaces/hyphens/apostrophes only</li>
     *   <li><strong>Last Name:</strong> Optional, same character restrictions as first name</li>
     * </ul>
     * 
     * @param user the user entity to validate
     * @param result the BindingResult to add validation errors to
     */
    private void validateUserData(User user, BindingResult result) {
        // Validate username pattern
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
        
        // Validate email format
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            String email = user.getEmail().trim();
            if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                result.rejectValue("email", "error.user", "Please enter a valid email address");
            }
        }
        
        // Validate names with permissive pattern
        if (user.getFirstName() != null && !user.getFirstName().trim().isEmpty()) {
            // Pattern that accepts letters, spaces, accents, hyphens and apostrophes
            if (!user.getFirstName().matches("^[a-zA-ZÀ-ÿÑñÇç\\s'-]+$")) {
                result.rejectValue("firstName", "error.user", 
                    "First name can only contain letters, spaces, hyphens and apostrophes");
            }
        }
        
        // Last name is optional but if provided must be valid
        if (user.getLastName() != null && !user.getLastName().trim().isEmpty()) {
            // Same permissive pattern for last name
            if (!user.getLastName().matches("^[a-zA-ZÀ-ÿÑñÇç\\s'-]*$")) {
                result.rejectValue("lastName", "error.user", 
                    "Last name can only contain letters, spaces, hyphens and apostrophes");
            }
        }
    }

    /**
     * Performs validation specific to user profile updates with more permissive rules.
     * This private helper method validates user data for profile updates, focusing only
     * on fields that users can modify and using more lenient validation rules.
     * 
     * <p>Profile-specific validation includes:</p>
     * <ul>
     *   <li><strong>Email:</strong> Format validation if provided (optional in profiles)</li>
     *   <li><strong>First Name:</strong> Required with character validation</li>
     *   <li><strong>Last Name:</strong> Optional with character validation</li>
     * </ul>
     * 
     * @param user the user entity to validate for profile updates
     * @param result the BindingResult to add validation errors to
     */
    private void validateProfileData(User user, BindingResult result) {
        // Validate email format if provided
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            String email = user.getEmail().trim();
            if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                result.rejectValue("email", "error.user", "Please enter a valid email address");
            }
        }
        
        // Validate that firstName is not empty
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            result.rejectValue("firstName", "error.user", "First name is required");
        } else {
            // Validate firstName format
            String firstName = user.getFirstName().trim();
            if (!firstName.matches("^[a-zA-ZÀ-ÿÑñÇç\\s'-]+$")) {
                result.rejectValue("firstName", "error.user", 
                    "First name can only contain letters, spaces, hyphens and apostrophes");
            }
        }
    }

    /**
     * Performs more permissive validation of user data for administrative editing.
     * This private helper method provides relaxed validation rules suitable for
     * administrative user updates where certain constraints may be more flexible.
     * 
     * <p>Permissive validation includes:</p>
     * <ul>
     *   <li><strong>Email:</strong> Standard format validation</li>
     *   <li><strong>First Name:</strong> Required with character validation</li>
     *   <li><strong>Last Name:</strong> Optional with character validation</li>
     * </ul>
     * 
     * @param user the user entity to validate with permissive rules
     * @param result the BindingResult to add validation errors to
     */
    private void validateUserDataPermissive(User user, BindingResult result) {
        // Validate email format
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            String email = user.getEmail().trim();
            if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                result.rejectValue("email", "error.user", "Please enter a valid email address");
            }
        }
        
        // Validate firstName
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            result.rejectValue("firstName", "error.user", "First name is required");
        } else {
            String firstName = user.getFirstName().trim();
            if (!firstName.matches("^[a-zA-ZÀ-ÿÑñÇç\\s'-]+$")) {
                result.rejectValue("firstName", "error.user", 
                    "First name can only contain letters, spaces, hyphens and apostrophes");
            }
        }
        
        // Validate lastName only if provided
        if (user.getLastName() != null && !user.getLastName().trim().isEmpty()) {
            String lastName = user.getLastName().trim();
            if (!lastName.matches("^[a-zA-ZÀ-ÿÑñÇç\\s'-]*$")) {
                result.rejectValue("lastName", "error.user", 
                    "Last name can only contain letters, spaces, hyphens and apostrophes");
            }
        }
    }
    
    /**
     * Validates password strength according to security requirements.
     * This private helper method enforces password strength rules to ensure
     * account security while maintaining usability with reasonable requirements.
     * 
     * <p>Password requirements include:</p>
     * <ul>
     *   <li><strong>Length:</strong> Minimum 8 characters</li>
     *   <li><strong>Characters:</strong> Must contain at least one letter</li>
     *   <li><strong>Complexity:</strong> Must contain at least one number or symbol</li>
     * </ul>
     * 
     * <p>The validation is optional during editing - if no password is provided,
     * the current password is retained unchanged.</p>
     * 
     * @param password the password string to validate
     * @param result the BindingResult to add validation errors to
     */
    private void validatePassword(String password, BindingResult result) {
        if (password == null || password.trim().isEmpty()) {
            return; // Optional in editing
        }
        
        String pwd = password.trim();
        
        // Validate minimum length
        if (pwd.length() < 8) {
            result.rejectValue("password", "error.user", 
                "Password must be at least 8 characters long");
            return;
        }
        
        // Validate that it contains letters
        boolean hasLetter = pwd.matches(".*[a-zA-Z].*");
        
        // Validate that it contains numbers OR symbols
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