package com.ezequiel.reiunio.exception;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class FileUploadExceptionHandler {

    /**
     * Handle file size exceeded exception for regular form submissions
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc, 
                                       RedirectAttributes redirectAttributes) {
        log.error("File upload size exceeded", exc);
        redirectAttributes.addFlashAttribute("error", 
            "File size exceeds maximum allowed size of 5MB. Please choose a smaller file.");
        return "redirect:/";
    }

    /**
     * Handle invalid file exceptions for regular form submissions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleInvalidFileException(IllegalArgumentException exc, 
                                           RedirectAttributes redirectAttributes) {
        log.error("Invalid file upload", exc);
        redirectAttributes.addFlashAttribute("error", exc.getMessage());
        return "redirect:/";
    }

    /**
     * Handle IO exceptions during file upload
     */
    @ExceptionHandler(IOException.class)
    public String handleIOException(IOException exc, 
                                   RedirectAttributes redirectAttributes) {
        log.error("IO error during file upload", exc);
        redirectAttributes.addFlashAttribute("error", 
            "An error occurred while uploading the file. Please try again.");
        return "redirect:/";
    }

    /**
     * Handle file size exceeded exception for AJAX requests
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ResponseEntity<ErrorResponse> handleMaxSizeExceptionAjax(MaxUploadSizeExceededException exc) {
        log.error("File upload size exceeded (AJAX)", exc);
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ErrorResponse(false, "File size exceeds maximum allowed size of 5MB"));
    }

    /**
     * Handle invalid file exceptions for AJAX requests
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleInvalidFileExceptionAjax(IllegalArgumentException exc) {
        log.error("Invalid file upload (AJAX)", exc);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(false, exc.getMessage()));
    }

    /**
     * Handle IO exceptions for AJAX requests
     */
    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleIOExceptionAjax(IOException exc) {
        log.error("IO error during file upload (AJAX)", exc);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(false, "An error occurred while uploading the file"));
    }

    /**
     * Error response class for AJAX requests
     */
    public static class ErrorResponse {
        private boolean success;
        private String message;

        public ErrorResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}