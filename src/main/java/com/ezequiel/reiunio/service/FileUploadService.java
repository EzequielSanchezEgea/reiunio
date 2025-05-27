package com.ezequiel.reiunio.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileUploadService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.max-file-size:2MB}")
    private String maxFileSize;

    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB in bytes

    /**
     * Uploads a user profile photo
     */
    public String uploadUserPhoto(MultipartFile file, Long userId) throws IOException {
        validateFile(file);
        
        String uploadPath = uploadDir + "/users";
        createDirectoryIfNotExists(uploadPath);
        
        String fileName = "user_" + userId + "_" + UUID.randomUUID().toString() + getFileExtension(file.getOriginalFilename());
        Path filePath = Paths.get(uploadPath, fileName);
        
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("User photo uploaded successfully: {}", fileName);
        return "/uploads/users/" + fileName;
    }

    /**
     * Uploads a game photo
     */
    public String uploadGamePhoto(MultipartFile file, Long gameId) throws IOException {
        validateFile(file);
        
        String uploadPath = uploadDir + "/games";
        createDirectoryIfNotExists(uploadPath);
        
        String fileName = "game_" + gameId + "_" + UUID.randomUUID().toString() + getFileExtension(file.getOriginalFilename());
        Path filePath = Paths.get(uploadPath, fileName);
        
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("Game photo uploaded successfully: {}", fileName);
        return "/uploads/games/" + fileName;
    }

    /**
     * Uploads a game session custom photo
     */
    public String uploadGameSessionPhoto(MultipartFile file, Long sessionId) throws IOException {
        validateFile(file);
        
        String uploadPath = uploadDir + "/game-sessions";
        createDirectoryIfNotExists(uploadPath);
        
        String fileName = "session_" + sessionId + "_" + UUID.randomUUID().toString() + getFileExtension(file.getOriginalFilename());
        Path filePath = Paths.get(uploadPath, fileName);
        
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("Game session photo uploaded successfully: {}", fileName);
        return "/uploads/game-sessions/" + fileName;
    }

    /**
     * Deletes a file by its path
     */
    public boolean deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        
        try {
            // Remove the leading slash and prepend the upload directory
            String relativePath = filePath.startsWith("/") ? filePath.substring(1) : filePath;
            Path path = Paths.get(relativePath);
            
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("File deleted successfully: {}", filePath);
                return true;
            } else {
                log.warn("File not found for deletion: {}", filePath);
                return false;
            }
        } catch (IOException e) {
            log.error("Error deleting file: {}", filePath, e);
            return false;
        }
    }

    /**
     * Deletes a game session custom image
     */
    public boolean deleteGameSessionImage(String imagePath) {
        return deleteFile(imagePath);
    }

    /**
     * Validates the uploaded file
     */
    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 2MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Invalid file name");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        boolean isValidExtension = false;
        for (String allowedExtension : ALLOWED_EXTENSIONS) {
            if (allowedExtension.equals(extension)) {
                isValidExtension = true;
                break;
            }
        }

        if (!isValidExtension) {
            throw new IllegalArgumentException("File type not allowed. Please upload JPG, PNG, GIF, or WebP images only");
        }

        // Validate MIME type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }
    }

    /**
     * Validates file specifically for game session images
     */
    public void validateGameSessionImage(MultipartFile file) throws IOException {
        validateFile(file); // Use the same validation as other images
        
        // Additional validation specific to game session images could go here
        log.debug("Game session image validation passed for file: {}", file.getOriginalFilename());
    }

    /**
     * Creates directory if it doesn't exist
     */
    private void createDirectoryIfNotExists(String path) throws IOException {
        Path directory = Paths.get(path);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
            log.info("Created directory: {}", path);
        }
    }

    /**
     * Gets file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }

    /**
     * Generates a unique filename for game session images
     */
    public String generateGameSessionImageName(Long sessionId, String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return "session_" + sessionId + "_" + UUID.randomUUID().toString() + extension;
    }

    /**
     * Gets the full path for a game session image
     */
    public String getGameSessionImagePath(Long sessionId, String filename) {
        return uploadDir + "/game-sessions/" + filename;
    }

    /**
     * Checks if a file exists
     */
    public boolean fileExists(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        
        try {
            String relativePath = filePath.startsWith("/") ? filePath.substring(1) : filePath;
            Path path = Paths.get(relativePath);
            return Files.exists(path);
        } catch (Exception e) {
            log.error("Error checking if file exists: {}", filePath, e);
            return false;
        }
    }

    /**
     * Generates a default avatar URL based on user initials
     */
    public String generateDefaultAvatar(String firstName, String lastName) {
        String initials = "";
        if (firstName != null && !firstName.isEmpty()) {
            initials += firstName.charAt(0);
        }
        if (lastName != null && !lastName.isEmpty()) {
            initials += lastName.charAt(0);
        }
        
        // Using a service like UI Avatars or similar
        return "https://ui-avatars.com/api/?name=" + initials + "&background=007bff&color=fff&size=200";
    }
    
    /**
     * Validates a game photo specifically
     */
    public void validateGamePhoto(MultipartFile file) throws IOException {
        validateFile(file); // Use the same validation as other images
        
        log.debug("Game photo validation passed for file: {}", file.getOriginalFilename());
    }
    
    /**
     * Gets upload directory path
     */
    public String getUploadDirectory() {
        return uploadDir;
    }
    
    /**
     * Gets allowed file extensions
     */
    public String[] getAllowedExtensions() {
        return ALLOWED_EXTENSIONS.clone();
    }
    
    /**
     * Gets maximum file size in bytes
     */
    public long getMaxFileSize() {
        return MAX_FILE_SIZE;
    }
}