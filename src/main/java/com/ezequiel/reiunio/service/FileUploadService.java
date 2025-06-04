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

/**
 * Service for handling file uploads and file-related operations such as validation, saving, and deletion.
 */
@Service
@Slf4j
public class FileUploadService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.max-file-size:2MB}")
    private String maxFileSize;

    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    /**
     * Uploads a profile photo for a specific user.
     *
     * @param file   the uploaded file
     * @param userId the ID of the user
     * @return the relative path to the uploaded photo
     * @throws IOException if an I/O error occurs during upload
     */
    public String uploadUserPhoto(MultipartFile file, Long userId) throws IOException {
        validateFile(file);
        String uploadPath = uploadDir + "/users";
        createDirectoryIfNotExists(uploadPath);
        String fileName = "user_" + userId + "_" + UUID.randomUUID() + getFileExtension(file.getOriginalFilename());
        Path filePath = Paths.get(uploadPath, fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("User photo uploaded successfully: {}", fileName);
        return "/uploads/users/" + fileName;
    }

    /**
     * Uploads a photo for a specific game.
     *
     * @param file   the uploaded file
     * @param gameId the ID of the game
     * @return the relative path to the uploaded photo
     * @throws IOException if an I/O error occurs during upload
     */
    public String uploadGamePhoto(MultipartFile file, Long gameId) throws IOException {
        validateFile(file);
        String uploadPath = uploadDir + "/games";
        createDirectoryIfNotExists(uploadPath);
        String fileName = "game_" + gameId + "_" + UUID.randomUUID() + getFileExtension(file.getOriginalFilename());
        Path filePath = Paths.get(uploadPath, fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Game photo uploaded successfully: {}", fileName);
        return "/uploads/games/" + fileName;
    }

    /**
     * Uploads a custom image for a game session.
     *
     * @param file      the uploaded file
     * @param sessionId the ID of the game session
     * @return the relative path to the uploaded image
     * @throws IOException if an I/O error occurs during upload
     */
    public String uploadGameSessionPhoto(MultipartFile file, Long sessionId) throws IOException {
        validateFile(file);
        String uploadPath = uploadDir + "/game-sessions";
        createDirectoryIfNotExists(uploadPath);
        String fileName = "session_" + sessionId + "_" + UUID.randomUUID() + getFileExtension(file.getOriginalFilename());
        Path filePath = Paths.get(uploadPath, fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Game session photo uploaded successfully: {}", fileName);
        return "/uploads/game-sessions/" + fileName;
    }

    /**
     * Deletes a file at the given path.
     *
     * @param filePath the path to the file to delete
     * @return true if the file was successfully deleted, false otherwise
     */
    public boolean deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) return false;
        try {
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
     * Deletes a custom image for a game session.
     *
     * @param imagePath the path to the game session image
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteGameSessionImage(String imagePath) {
        return deleteFile(imagePath);
    }

    /**
     * Validates the uploaded file against size and type restrictions.
     *
     * @param file the uploaded file
     * @throws IOException if the file is invalid
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
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }
    }

    /**
     * Validates a game session image upload.
     *
     * @param file the uploaded file
     * @throws IOException if the file is invalid
     */
    public void validateGameSessionImage(MultipartFile file) throws IOException {
        validateFile(file);
        log.debug("Game session image validation passed for file: {}", file.getOriginalFilename());
    }

    /**
     * Creates a directory if it does not already exist.
     *
     * @param path the directory path
     * @throws IOException if the directory cannot be created
     */
    private void createDirectoryIfNotExists(String path) throws IOException {
        Path directory = Paths.get(path);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
            log.info("Created directory: {}", path);
        }
    }

    /**
     * Extracts the file extension from the file name.
     *
     * @param filename the original file name
     * @return the file extension (including the dot), or an empty string if none
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }

    /**
     * Generates a unique image name for a game session image.
     *
     * @param sessionId        the session ID
     * @param originalFilename the original file name
     * @return the generated file name
     */
    public String generateGameSessionImageName(Long sessionId, String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return "session_" + sessionId + "_" + UUID.randomUUID() + extension;
    }

    /**
     * Constructs the full upload path for a game session image.
     *
     * @param sessionId the session ID
     * @param filename  the image file name
     * @return the relative path to the image
     */
    public String getGameSessionImagePath(Long sessionId, String filename) {
        return uploadDir + "/game-sessions/" + filename;
    }

    /**
     * Checks if a file exists at the given path.
     *
     * @param filePath the path to the file
     * @return true if the file exists, false otherwise
     */
    public boolean fileExists(String filePath) {
        if (filePath == null || filePath.isEmpty()) return false;
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
     * Generates a default avatar URL using the user's initials.
     *
     * @param firstName the user's first name
     * @param lastName  the user's last name
     * @return the URL for the default avatar
     */
    public String generateDefaultAvatar(String firstName, String lastName) {
        String initials = "";
        if (firstName != null && !firstName.isEmpty()) {
            initials += firstName.charAt(0);
        }
        if (lastName != null && !lastName.isEmpty()) {
            initials += lastName.charAt(0);
        }
        return "https://ui-avatars.com/api/?name=" + initials + "&background=007bff&color=fff&size=200";
    }

    /**
     * Validates a game photo upload.
     *
     * @param file the uploaded file
     * @throws IOException if the file is invalid
     */
    public void validateGamePhoto(MultipartFile file) throws IOException {
        validateFile(file);
        log.debug("Game photo validation passed for file: {}", file.getOriginalFilename());
    }

    /**
     * Returns the configured upload directory path.
     *
     * @return the upload directory path
     */
    public String getUploadDirectory() {
        return uploadDir;
    }

    /**
     * Returns the allowed file extensions.
     *
     * @return an array of allowed extensions
     */
    public String[] getAllowedExtensions() {
        return ALLOWED_EXTENSIONS.clone();
    }

    /**
     * Returns the maximum allowed file size in bytes.
     *
     * @return the maximum file size
     */
    public long getMaxFileSize() {
        return MAX_FILE_SIZE;
    }
}
