package com.ezequiel.reiunio.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ezequiel.reiunio.entity.Game;
import com.ezequiel.reiunio.entity.GameSession;
import com.ezequiel.reiunio.entity.User;
import com.ezequiel.reiunio.enums.ActionType;
import com.ezequiel.reiunio.service.AuditLogService;
import com.ezequiel.reiunio.service.FileUploadService;
import com.ezequiel.reiunio.service.GameService;
import com.ezequiel.reiunio.service.GameSessionService;
import com.ezequiel.reiunio.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller responsible for handling file upload operations in the Reiunio application.
 * This controller manages the upload, deletion, and management of photos for users, games, and game sessions.
 * It provides both traditional form-based endpoints and AJAX endpoints for enhanced user experience.
 * 
 * <p>The controller implements proper security measures using Spring Security annotations
 * to ensure users can only modify their own content or administrators can manage all content.</p>
 * 
 * <p>All file operations are logged through the audit system for compliance and tracking purposes.</p>
 */
@Controller
@RequestMapping("/upload")
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {

    /** Service for handling file upload operations and storage management */
    private final FileUploadService fileUploadService;
    
    /** Service for user-related operations and data management */
    private final UserService userService;
    
    /** Service for game-related operations and data management */
    private final GameService gameService;
    
    /** Service for game session operations and data management */
    private final GameSessionService gameSessionService;
    
    /** Service for logging audit events and user actions */
    private final AuditLogService auditLogService;
    
    /** Spring Security service for loading user details */
    private final UserDetailsService userDetailsService;

    /**
     * Handles the upload of a user profile photo through a traditional form submission.
     * This endpoint supports both administrators managing any user and users managing their own profiles.
     * 
     * <p>The method performs the following operations:</p>
     * <ul>
     *   <li>Validates the existence of the target user</li>
     *   <li>Removes any existing profile photo</li>
     *   <li>Uploads and stores the new photo</li>
     *   <li>Updates the user entity with the new photo path</li>
     *   <li>Logs the action for audit purposes</li>
     *   <li>Updates the security context if the user is updating their own photo</li>
     * </ul>
     * 
     * @param userId the unique identifier of the user whose photo is being uploaded
     * @param photo the multipart file containing the new profile photo
     * @param redirectAttributes Spring MVC attributes for passing messages between requests
     * @param principal the currently authenticated user's security principal
     * @return a redirect URL to the appropriate user page
     * 
     * @throws IOException if there's an error during file upload or deletion
     * @throws IllegalArgumentException if the uploaded file is invalid or doesn't meet requirements
     */
    @PostMapping("/user/{userId}/photo")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isCurrentUser(#userId, authentication.principal.username)")
    public String uploadUserPhoto(@PathVariable Long userId, 
                                 @RequestParam("photo") MultipartFile photo,
                                 RedirectAttributes redirectAttributes,
                                 Principal principal) {
        try {
            Optional<User> userOpt = userService.findById(userId);
            if (!userOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "User not found");
                return "redirect:/users/" + userId;
            }

            User user = userOpt.get();
            
            // Delete old photo if exists
            if (user.getProfilePhotoPath() != null) {
                fileUploadService.deleteFile(user.getProfilePhotoPath());
            }

            // Upload new photo
            String photoPath = fileUploadService.uploadUserPhoto(photo, userId);
            user.setProfilePhotoPath(photoPath);
            userService.save(user);

            // Log the action
            userService.findByUsername(principal.getName()).ifPresent(currentUser -> 
                auditLogService.logChange(currentUser, ActionType.MODIFICATION, "User", userId, 
                    "Profile photo updated for user: " + user.getUsername())
            );

            // Update security context if user is updating their own photo
            updateSecurityContextIfCurrentUser(user, principal);

            redirectAttributes.addFlashAttribute("message", "Profile photo updated successfully");
            
        } catch (IOException e) {
            log.error("Error uploading user photo", e);
            redirectAttributes.addFlashAttribute("error", "Error uploading photo: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Invalid file for user photo upload", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        // Determine the correct redirection
        String referer = determineRedirectForUser(userId, principal);
        return "redirect:" + referer;
    }

    /**
     * Handles the upload of a user profile photo through an AJAX request.
     * This endpoint provides the same functionality as the traditional upload but returns JSON responses
     * for better integration with dynamic web interfaces.
     * 
     * <p>This method is particularly useful for single-page applications or when updating
     * the UI without a full page refresh is desired.</p>
     * 
     * @param userId the unique identifier of the user whose photo is being uploaded
     * @param photo the multipart file containing the new profile photo
     * @param principal the currently authenticated user's security principal
     * @return a ResponseEntity containing a PhotoUploadResponse with operation status and details
     * 
     * @see PhotoUploadResponse
     */
    @PostMapping("/user/{userId}/photo/ajax")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isCurrentUser(#userId, authentication.principal.username)")
    @ResponseBody
    public ResponseEntity<?> uploadUserPhotoAjax(@PathVariable Long userId, 
                                               @RequestParam("photo") MultipartFile photo,
                                               Principal principal) {
        try {
            Optional<User> userOpt = userService.findById(userId);
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest().body(new PhotoUploadResponse(false, "User not found", null));
            }

            User user = userOpt.get();
            
            // Delete old photo if exists
            if (user.getProfilePhotoPath() != null) {
                fileUploadService.deleteFile(user.getProfilePhotoPath());
            }

            // Upload new photo
            String photoPath = fileUploadService.uploadUserPhoto(photo, userId);
            user.setProfilePhotoPath(photoPath);
            userService.save(user);

            // Log the action
            userService.findByUsername(principal.getName()).ifPresent(currentUser -> 
                auditLogService.logChange(currentUser, ActionType.MODIFICATION, "User", userId, 
                    "Profile photo updated for user: " + user.getUsername())
            );

            // Update security context if user is updating their own photo
            updateSecurityContextIfCurrentUser(user, principal);

            return ResponseEntity.ok().body(new PhotoUploadResponse(true, "Photo updated successfully", photoPath));
            
        } catch (IOException e) {
            log.error("Error uploading user photo", e);
            return ResponseEntity.badRequest().body(new PhotoUploadResponse(false, "Error uploading photo: " + e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            log.error("Invalid file for user photo upload", e);
            return ResponseEntity.badRequest().body(new PhotoUploadResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Handles the deletion of a user's profile photo.
     * This endpoint allows users to remove their profile photo entirely, reverting to the default avatar.
     * 
     * <p>The operation includes:</p>
     * <ul>
     *   <li>Validation of user existence</li>
     *   <li>Physical deletion of the photo file from storage</li>
     *   <li>Updating the user entity to remove the photo reference</li>
     *   <li>Audit logging of the deletion action</li>
     *   <li>Security context update if applicable</li>
     * </ul>
     * 
     * @param userId the unique identifier of the user whose photo is being deleted
     * @param redirectAttributes Spring MVC attributes for passing messages between requests
     * @param principal the currently authenticated user's security principal
     * @return a redirect URL to the appropriate user page
     */
    @PostMapping("/user/{userId}/photo/delete")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isCurrentUser(#userId, authentication.principal.username)")
    public String deleteUserPhoto(@PathVariable Long userId,
                                 RedirectAttributes redirectAttributes,
                                 Principal principal) {
        Optional<User> userOpt = userService.findById(userId);
        if (!userOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "User not found");
            return "redirect:/users";
        }

        User user = userOpt.get();
        
        if (user.getProfilePhotoPath() != null) {
            fileUploadService.deleteFile(user.getProfilePhotoPath());
            user.setProfilePhotoPath(null);
            userService.save(user);

            // Log the action
            userService.findByUsername(principal.getName()).ifPresent(currentUser -> 
                auditLogService.logChange(currentUser, ActionType.MODIFICATION, "User", userId, 
                    "Profile photo deleted for user: " + user.getUsername())
            );

            // Update security context if user is deleting their own photo
            updateSecurityContextIfCurrentUser(user, principal);

            redirectAttributes.addFlashAttribute("message", "Profile photo deleted successfully");
        }

        // Determine the correct redirection
        String referer = determineRedirectForUser(userId, principal);
        return "redirect:" + referer;
    }

    /**
     * Updates the Spring Security context when a user modifies their own profile information.
     * This method ensures that the current user's session reflects any changes made to their profile,
     * particularly useful for displaying updated profile photos without requiring a re-login.
     * 
     * <p>The method performs the following operations:</p>
     * <ul>
     *   <li>Checks if the updated user is the currently authenticated user</li>
     *   <li>Reloads the user details from the database</li>
     *   <li>Creates a new authentication token with updated information</li>
     *   <li>Updates the security context with the new authentication</li>
     * </ul>
     * 
     * <p>If any errors occur during the security context update, they are logged but don't
     * fail the entire operation, ensuring the primary functionality remains intact.</p>
     * 
     * @param updatedUser the user entity that has been updated
     * @param principal the current authentication principal
     */
    private void updateSecurityContextIfCurrentUser(User updatedUser, Principal principal) {
        try {
            // Check if the updated user is the currently logged-in user
            if (principal != null && principal.getName().equals(updatedUser.getUsername())) {
                log.debug("Updating security context for current user: {}", updatedUser.getUsername());
                
                // Get the current authentication
                Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
                
                if (currentAuth != null) {
                    // Reload the user details to get the updated profile photo path
                    UserDetails updatedUserDetails = userDetailsService.loadUserByUsername(updatedUser.getUsername());
                    
                    // Create new authentication with updated user details
                    Authentication newAuth = new UsernamePasswordAuthenticationToken(
                        updatedUserDetails,
                        currentAuth.getCredentials(),
                        currentAuth.getAuthorities()
                    );
                    
                    // Update the security context
                    SecurityContextHolder.getContext().setAuthentication(newAuth);
                    
                    log.debug("Security context updated successfully for user: {}", updatedUser.getUsername());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to update security context for user: {}, error: {}", 
                    updatedUser.getUsername(), e.getMessage());
            // Don't fail the entire operation if security context update fails
        }
    }

    /**
     * Handles the upload of a game photo through a traditional form submission.
     * This endpoint is restricted to administrators only, as game content management
     * is typically a privileged operation.
     * 
     * <p>The upload process includes:</p>
     * <ul>
     *   <li>Game existence validation</li>
     *   <li>Removal of any existing game image</li>
     *   <li>Upload and storage of the new image</li>
     *   <li>Database update with the new image path</li>
     *   <li>Audit logging of the modification</li>
     * </ul>
     * 
     * @param gameId the unique identifier of the game whose photo is being uploaded
     * @param photo the multipart file containing the new game photo
     * @param redirectAttributes Spring MVC attributes for passing messages between requests
     * @param principal the currently authenticated user's security principal
     * @return a redirect URL to the game details page
     */
    @PostMapping("/game/{gameId}/photo")
    @PreAuthorize("hasRole('ADMIN')")
    public String uploadGamePhoto(@PathVariable Long gameId, 
                                 @RequestParam("photo") MultipartFile photo,
                                 RedirectAttributes redirectAttributes,
                                 Principal principal) {
        try {
            Optional<Game> gameOpt = gameService.findById(gameId);
            if (!gameOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Game not found");
                return "redirect:/games";
            }

            Game game = gameOpt.get();
            
            // Delete old photo if exists
            if (game.getImagePath() != null) {
                fileUploadService.deleteFile(game.getImagePath());
            }

            // Upload new photo
            String photoPath = fileUploadService.uploadGamePhoto(photo, gameId);
            game.setImagePath(photoPath);
            gameService.save(game);

            // Log the action
            userService.findByUsername(principal.getName()).ifPresent(currentUser -> 
                auditLogService.logChange(currentUser, ActionType.MODIFICATION, "Game", gameId, 
                    "Photo updated for game: " + game.getName())
            );

            redirectAttributes.addFlashAttribute("message", "Game photo updated successfully");
            
        } catch (IOException e) {
            log.error("Error uploading game photo", e);
            redirectAttributes.addFlashAttribute("error", "Error uploading photo: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Invalid file for game photo upload", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/games/" + gameId;
    }

    /**
     * Handles the upload of a game photo through an AJAX request.
     * This endpoint provides JSON responses for dynamic UI updates and is restricted to administrators.
     * 
     * @param gameId the unique identifier of the game whose photo is being uploaded
     * @param photo the multipart file containing the new game photo
     * @param principal the currently authenticated user's security principal
     * @return a ResponseEntity containing a PhotoUploadResponse with operation status and details
     */
    @PostMapping("/game/{gameId}/photo/ajax")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> uploadGamePhotoAjax(@PathVariable Long gameId, 
                                               @RequestParam("photo") MultipartFile photo,
                                               Principal principal) {
        try {
            Optional<Game> gameOpt = gameService.findById(gameId);
            if (!gameOpt.isPresent()) {
                return ResponseEntity.badRequest().body("Game not found");
            }

            Game game = gameOpt.get();
            
            // Delete old photo if exists
            if (game.getImagePath() != null) {
                fileUploadService.deleteFile(game.getImagePath());
            }

            // Upload new photo
            String photoPath = fileUploadService.uploadGamePhoto(photo, gameId);
            game.setImagePath(photoPath);
            gameService.save(game);

            // Log the action
            userService.findByUsername(principal.getName()).ifPresent(currentUser -> 
                auditLogService.logChange(currentUser, ActionType.MODIFICATION, "Game", gameId, 
                    "Photo updated for game: " + game.getName())
            );

            return ResponseEntity.ok().body(new PhotoUploadResponse(true, "Photo updated successfully", photoPath));
            
        } catch (IOException e) {
            log.error("Error uploading game photo", e);
            return ResponseEntity.badRequest().body(new PhotoUploadResponse(false, "Error uploading photo: " + e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            log.error("Invalid file for game photo upload", e);
            return ResponseEntity.badRequest().body(new PhotoUploadResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Handles the upload of a custom image for a game session.
     * This endpoint allows session creators, extended users, and administrators to upload custom images
     * for personal game sessions (not library games).
     * 
     * <p>Important restrictions:</p>
     * <ul>
     *   <li>Only personal games can have custom images uploaded</li>
     *   <li>Library games cannot be modified with custom images</li>
     *   <li>Access is restricted based on user roles and session ownership</li>
     * </ul>
     * 
     * @param sessionId the unique identifier of the game session
     * @param photo the multipart file containing the custom game image
     * @param redirectAttributes Spring MVC attributes for passing messages between requests
     * @param principal the currently authenticated user's security principal
     * @return a redirect URL to the game session details page
     */
    @PostMapping("/game-session/{sessionId}/photo")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXTENDED_USER') or @securityUtils.isGameSessionCreator(#sessionId, principal.username)")
    public String uploadGameSessionPhoto(@PathVariable Long sessionId, 
                                        @RequestParam("photo") MultipartFile photo,
                                        RedirectAttributes redirectAttributes,
                                        Principal principal) {
        try {
            Optional<GameSession> sessionOpt = gameSessionService.findById(sessionId);
            if (!sessionOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Game session not found");
                return "redirect:/game-sessions";
            }

            GameSession gameSession = sessionOpt.get();
            
            // Only allow for personal games (not library games)
            if (gameSession.isLibraryGame()) {
                redirectAttributes.addFlashAttribute("error", "Cannot upload image for library games");
                return "redirect:/game-sessions/" + sessionId;
            }
            
            // Delete old photo if exists
            if (gameSession.getCustomGameImagePath() != null) {
                fileUploadService.deleteFile(gameSession.getCustomGameImagePath());
            }

            // Upload new photo
            String photoPath = fileUploadService.uploadGameSessionPhoto(photo, sessionId);
            gameSession.setCustomGameImagePath(photoPath);
            gameSessionService.save(gameSession);

            // Log the action
            userService.findByUsername(principal.getName()).ifPresent(currentUser -> 
                auditLogService.logChange(currentUser, ActionType.MODIFICATION, "GameSession", sessionId, 
                    "Custom image uploaded for session: " + gameSession.getTitle())
            );

            redirectAttributes.addFlashAttribute("message", "Game image updated successfully");
            
        } catch (IOException e) {
            log.error("Error uploading game session photo", e);
            redirectAttributes.addFlashAttribute("error", "Error uploading photo: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Invalid file for game session photo upload", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/game-sessions/" + sessionId;
    }

    /**
     * Handles the upload of a custom image for a game session through an AJAX request.
     * This endpoint provides the same functionality as the traditional upload but with JSON responses
     * for better integration with dynamic interfaces.
     * 
     * @param sessionId the unique identifier of the game session
     * @param photo the multipart file containing the custom game image
     * @param principal the currently authenticated user's security principal
     * @return a ResponseEntity containing a PhotoUploadResponse with operation status and details
     */
    @PostMapping("/game-session/{sessionId}/photo/ajax")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXTENDED_USER') or @securityUtils.isGameSessionCreator(#sessionId, principal.username)")
    @ResponseBody
    public ResponseEntity<?> uploadGameSessionPhotoAjax(@PathVariable Long sessionId, 
                                                       @RequestParam("photo") MultipartFile photo,
                                                       Principal principal) {
        try {
            Optional<GameSession> sessionOpt = gameSessionService.findById(sessionId);
            if (!sessionOpt.isPresent()) {
                return ResponseEntity.badRequest().body("Game session not found");
            }

            GameSession gameSession = sessionOpt.get();
            
            // Only allow for personal games (not library games)
            if (gameSession.isLibraryGame()) {
                return ResponseEntity.badRequest().body("Cannot upload image for library games");
            }
            
            // Delete old photo if exists
            if (gameSession.getCustomGameImagePath() != null) {
                fileUploadService.deleteFile(gameSession.getCustomGameImagePath());
            }

            // Upload new photo
            String photoPath = fileUploadService.uploadGameSessionPhoto(photo, sessionId);
            gameSession.setCustomGameImagePath(photoPath);
            gameSessionService.save(gameSession);

            // Log the action
            userService.findByUsername(principal.getName()).ifPresent(currentUser -> 
                auditLogService.logChange(currentUser, ActionType.MODIFICATION, "GameSession", sessionId, 
                    "Custom image uploaded for session: " + gameSession.getTitle())
            );

            return ResponseEntity.ok().body(new PhotoUploadResponse(true, "Game image updated successfully", photoPath));
            
        } catch (IOException e) {
            log.error("Error uploading game session photo", e);
            return ResponseEntity.badRequest().body(new PhotoUploadResponse(false, "Error uploading photo: " + e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            log.error("Invalid file for game session photo upload", e);
            return ResponseEntity.badRequest().body(new PhotoUploadResponse(false, e.getMessage(), null));
        }
    }

    /**
     * Handles the deletion of a game photo.
     * This endpoint is restricted to administrators and removes both the file from storage
     * and the reference from the database.
     * 
     * @param gameId the unique identifier of the game whose photo is being deleted
     * @param redirectAttributes Spring MVC attributes for passing messages between requests
     * @param principal the currently authenticated user's security principal
     * @return a redirect URL to the game details page
     */
    @PostMapping("/game/{gameId}/photo/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteGamePhoto(@PathVariable Long gameId,
                                 RedirectAttributes redirectAttributes,
                                 Principal principal) {
        Optional<Game> gameOpt = gameService.findById(gameId);
        if (!gameOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Game not found");
            return "redirect:/games";
        }

        Game game = gameOpt.get();
        
        if (game.getImagePath() != null) {
            fileUploadService.deleteFile(game.getImagePath());
            game.setImagePath(null);
            gameService.save(game);

            // Log the action
            userService.findByUsername(principal.getName()).ifPresent(currentUser -> 
                auditLogService.logChange(currentUser, ActionType.MODIFICATION, "Game", gameId, 
                    "Photo deleted for game: " + game.getName())
            );

            redirectAttributes.addFlashAttribute("message", "Game photo deleted successfully");
        }

        return "redirect:/games/" + gameId;
    }

    /**
     * Handles the deletion of a custom image from a game session.
     * This endpoint allows authorized users to remove custom images from personal game sessions.
     * 
     * @param sessionId the unique identifier of the game session
     * @param redirectAttributes Spring MVC attributes for passing messages between requests
     * @param principal the currently authenticated user's security principal
     * @return a redirect URL to the game session details page
     */
    @PostMapping("/game-session/{sessionId}/photo/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXTENDED_USER') or @securityUtils.isGameSessionCreator(#sessionId, principal.username)")
    public String deleteGameSessionPhoto(@PathVariable Long sessionId,
                                        RedirectAttributes redirectAttributes,
                                        Principal principal) {
        Optional<GameSession> sessionOpt = gameSessionService.findById(sessionId);
        if (!sessionOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Game session not found");
            return "redirect:/game-sessions";
        }

        GameSession gameSession = sessionOpt.get();
        
        if (gameSession.getCustomGameImagePath() != null) {
            fileUploadService.deleteFile(gameSession.getCustomGameImagePath());
            gameSession.setCustomGameImagePath(null);
            gameSessionService.save(gameSession);

            // Log the action
            userService.findByUsername(principal.getName()).ifPresent(currentUser -> 
                auditLogService.logChange(currentUser, ActionType.MODIFICATION, "GameSession", sessionId, 
                    "Custom image deleted for session: " + gameSession.getTitle())
            );

            redirectAttributes.addFlashAttribute("message", "Game image deleted successfully");
        }

        return "redirect:/game-sessions/" + sessionId;
    }

    /**
     * Determines the appropriate redirect URL for user-related operations.
     * This method analyzes the current user's role and relationship to the target user
     * to provide the most appropriate redirect destination.
     * 
     * <p>Redirect logic:</p>
     * <ul>
     *   <li>If a user is editing their own profile, redirect to the profile page</li>
     *   <li>If an administrator is editing another user, redirect to that user's detail page</li>
     *   <li>Default fallback to the user's detail page</li>
     * </ul>
     * 
     * @param userId the identifier of the user being modified
     * @param principal the current authentication principal
     * @return the appropriate redirect path as a string
     */
    private String determineRedirectForUser(Long userId, Principal principal) {
        try {
            Optional<User> currentUserOpt = userService.findByUsername(principal.getName());
            if (currentUserOpt.isPresent()) {
                User currentUser = currentUserOpt.get();
                
                // If it's the same user, redirect to profile
                if (currentUser.getId().equals(userId)) {
                    return "/users/profile";
                }
                
                // If it's an admin editing another user, redirect to user view
                if (currentUser.getRole().name().equals("ADMIN")) {
                    return "/users/" + userId;
                }
            }
        } catch (Exception e) {
            log.warn("Error determining redirect for user {}: {}", userId, e.getMessage());
        }
        
        // Default fallback
        return "/users/" + userId;
    }

    /**
     * Data Transfer Object representing the response for AJAX photo upload operations.
     * This class encapsulates the result of photo upload attempts, providing both
     * success status and relevant details for client-side processing.
     * 
     * <p>The response includes:</p>
     * <ul>
     *   <li>Success flag indicating if the operation completed successfully</li>
     *   <li>Message providing details about the operation result</li>
     *   <li>Photo URL for successful uploads (null for failures)</li>
     * </ul>
     * 
     * @since 1.0
     */
    public static class PhotoUploadResponse {
        /** Indicates whether the photo upload operation was successful */
        private boolean success;
        
        /** Human-readable message describing the operation result */
        private String message;
        
        /** URL path to the uploaded photo, null if upload failed */
        private String photoUrl;

        /**
         * Constructs a new PhotoUploadResponse with the specified parameters.
         * 
         * @param success true if the upload was successful, false otherwise
         * @param message descriptive message about the operation result
         * @param photoUrl URL path to the uploaded photo, or null if upload failed
         */
        public PhotoUploadResponse(boolean success, String message, String photoUrl) {
            this.success = success;
            this.message = message;
            this.photoUrl = photoUrl;
        }

        /**
         * Returns whether the photo upload operation was successful.
         * 
         * @return true if successful, false otherwise
         */
        public boolean isSuccess() { 
            return success; 
        }
        
        /**
         * Returns the descriptive message about the operation result.
         * 
         * @return the operation result message
         */
        public String getMessage() { 
            return message; 
        }
        
        /**
         * Returns the URL path to the uploaded photo.
         * 
         * @return the photo URL, or null if upload failed
         */
        public String getPhotoUrl() { 
            return photoUrl; 
        }
    }
}