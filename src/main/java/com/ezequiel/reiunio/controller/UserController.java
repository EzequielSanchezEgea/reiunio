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

import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.ActionType;
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
                           @RequestParam(required = false) String search) {
        List<User> users;
        
        if (role != null && !role.isEmpty()) {
            try {
                Role roleEnum = Role.valueOf(role.toUpperCase());
                users = userService.findByRole(roleEnum);
                model.addAttribute("filterRole", role);
            } catch (IllegalArgumentException e) {
                users = userService.findAll();
            }
        } else if (search != null && !search.isEmpty()) {
            users = userService.searchUsers(search);
            model.addAttribute("filterSearch", search);
        } else {
            users = userService.findAll();
        }
        
        model.addAttribute("users", users);
        model.addAttribute("roles", Role.values());
        
        // Add statistics
        long totalUsers = users.size();
        long adminCount = users.stream().filter(u -> u.getRole() == Role.ADMIN).count();
        long extendedUserCount = users.stream().filter(u -> u.getRole() == Role.EXTENDED_USER).count();
        long basicUserCount = users.stream().filter(u -> u.getRole() == Role.BASIC_USER).count();
        
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("extendedUserCount", extendedUserCount);
        model.addAttribute("basicUserCount", basicUserCount);
        
        return "users/list";
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
                model.addAttribute("userLoans", user.get().getLoans());
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
        return "users/form";
    }

    @PostMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String createUser(@Valid @ModelAttribute("user") User user, 
                            BindingResult result, 
                            Model model, 
                            RedirectAttributes redirectAttributes, 
                            Principal principal) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "users/form";
        }
        
        // Check if username or email already exist
        if (userService.existsByUsername(user.getUsername())) {
            result.rejectValue("username", "error.user", "Username already exists");
            model.addAttribute("roles", Role.values());
            return "users/form";
        }
        
        if (userService.existsByEmail(user.getEmail())) {
            result.rejectValue("email", "error.user", "Email already registered");
            model.addAttribute("roles", Role.values());
            return "users/form";
        }
        
        user.setRegistrationDate(LocalDate.now());
        User savedUser = userService.save(user);
        
        // Log the action
        userService.findByUsername(principal.getName()).ifPresent(adminUser -> 
            auditLogService.logChange(adminUser, ActionType.CREATION, "User", savedUser.getId(), 
                "User created: " + savedUser.getUsername() + " with role " + savedUser.getRole())
        );
        
        redirectAttributes.addFlashAttribute("message", "User created successfully");
        return "redirect:/users";
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
        if (result.hasErrors()) {
            return "users/register";
        }
        
        // Check if username or email already exist
        if (userService.existsByUsername(user.getUsername())) {
            result.rejectValue("username", "error.user", "Username already exists");
            return "users/register";
        }
        
        if (userService.existsByEmail(user.getEmail())) {
            result.rejectValue("email", "error.user", "Email already registered");
            return "users/register";
        }
        
        // Set default role for public registration
        user.setRole(Role.BASIC_USER);
        user.setRegistrationDate(LocalDate.now());
        
        userService.save(user);
        
        redirectAttributes.addFlashAttribute("message", "Registration completed successfully. You can now log in.");
        return "redirect:/login";
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
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            model.addAttribute("editing", true);
            return "users/form";
        }
        
        Optional<User> existingUser = userService.findById(id);
        
        if (existingUser.isPresent()) {
            User currentUser = existingUser.get();
            
            // Check if username or email already exist for other users
            if (!currentUser.getUsername().equals(user.getUsername()) && 
                userService.existsByUsername(user.getUsername())) {
                result.rejectValue("username", "error.user", "Username already exists");
                model.addAttribute("roles", Role.values());
                model.addAttribute("editing", true);
                return "users/form";
            }
            
            if (!currentUser.getEmail().equals(user.getEmail()) && 
                userService.existsByEmail(user.getEmail())) {
                result.rejectValue("email", "error.user", "Email already registered");
                model.addAttribute("roles", Role.values());
                model.addAttribute("editing", true);
                return "users/form";
            }
            
            // Keep original registration date and password if not changed
            user.setRegistrationDate(currentUser.getRegistrationDate());
            if (password == null || password.isEmpty()) {
                user.setPassword(currentUser.getPassword());
            } else {
                user.setPassword(password);
            }
            
            User savedUser = userService.save(user);
            
            // Log the action
            userService.findByUsername(principal.getName()).ifPresent(adminUser -> 
                auditLogService.logChange(adminUser, ActionType.MODIFICATION, "User", savedUser.getId(), 
                    "User modified: " + savedUser.getUsername() + " - role: " + savedUser.getRole())
            );
            
            redirectAttributes.addFlashAttribute("message", "User updated successfully");
            return "redirect:/users";
        } else {
            return "redirect:/users";
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(@PathVariable Long id, 
                            RedirectAttributes redirectAttributes, 
                            Principal principal) {
        Optional<User> user = userService.findById(id);
        
        if (user.isPresent()) {
            User currentUser = user.get();
            
            // Prevent admin from deleting themselves
            Optional<User> adminUser = userService.findByUsername(principal.getName());
            if (adminUser.isPresent() && adminUser.get().getId().equals(id)) {
                redirectAttributes.addFlashAttribute("error", "You cannot delete your own account");
                return "redirect:/users";
            }
            
            String deletedUsername = currentUser.getUsername();
            userService.deleteById(id);
            
            // Log the action
            adminUser.ifPresent(admin -> 
                auditLogService.logChange(admin, ActionType.DELETION, "User", id, 
                    "User deleted: " + deletedUsername)
            );
            
            redirectAttributes.addFlashAttribute("message", "User deleted successfully");
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
        
        // Check if email already exists for other users
        if (!existingUser.getEmail().equals(user.getEmail()) && 
            userService.existsByEmail(user.getEmail())) {
            result.rejectValue("email", "error.user", "Email already registered");
            model.addAttribute("user", existingUser);
            return "users/profile";
        }
        
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
    }
}