package com.ezequiel.reiunio.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

@Controller
@RequestMapping("/upload")
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {

    private final FileUploadService fileUploadService;
    private final UserService userService;
    private final GameService gameService;
    private final GameSessionService gameSessionService;
    private final AuditLogService auditLogService;

    /**
     * Upload user profile photo
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

            redirectAttributes.addFlashAttribute("message", "Profile photo updated successfully");
            
        } catch (IOException e) {
            log.error("Error uploading user photo", e);
            redirectAttributes.addFlashAttribute("error", "Error uploading photo: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Invalid file for user photo upload", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        // Determinar la redirección correcta
        String referer = determineRedirectForUser(userId, principal);
        return "redirect:" + referer;
    }

    /**
     * Upload user profile photo via AJAX
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
                return ResponseEntity.badRequest().body("User not found");
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
     * Upload game photo
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
     * Upload game photo via AJAX
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
     * Upload game session custom image
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
     * Upload game session custom image via AJAX
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
     * Delete user profile photo
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

            redirectAttributes.addFlashAttribute("message", "Profile photo deleted successfully");
        }

        // Determinar la redirección correcta
        String referer = determineRedirectForUser(userId, principal);
        return "redirect:" + referer;
    }

    /**
     * Delete game photo
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
     * Delete game session custom image
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
     * Determina la redirección correcta para operaciones de usuario
     */
    private String determineRedirectForUser(Long userId, Principal principal) {
        try {
            Optional<User> currentUserOpt = userService.findByUsername(principal.getName());
            if (currentUserOpt.isPresent()) {
                User currentUser = currentUserOpt.get();
                
                // Si es el mismo usuario, redirigir al perfil
                if (currentUser.getId().equals(userId)) {
                    return "/users/profile";
                }
                
                // Si es admin editando otro usuario, redirigir a la vista del usuario
                if (currentUser.getRole().name().equals("ADMIN")) {
                    return "/users/" + userId;
                }
            }
        } catch (Exception e) {
            log.warn("Error determining redirect for user {}: {}", userId, e.getMessage());
        }
        
        // Fallback por defecto
        return "/users/" + userId;
    }

    /**
     * Response class for AJAX photo uploads
     */
    public static class PhotoUploadResponse {
        private boolean success;
        private String message;
        private String photoUrl;

        public PhotoUploadResponse(boolean success, String message, String photoUrl) {
            this.success = success;
            this.message = message;
            this.photoUrl = photoUrl;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getPhotoUrl() { return photoUrl; }
    }
}
