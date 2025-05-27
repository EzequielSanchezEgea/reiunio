package com.ezequiel.reiunio.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class FileUploadExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Object handleMaxSizeException(MaxUploadSizeExceededException ex, 
                                        HttpServletRequest request,
                                        RedirectAttributes redirectAttributes) {
        log.error("File upload size exceeded: {}", ex.getMessage());
        
        String contentType = request.getHeader("Content-Type");
        String accept = request.getHeader("Accept");
        
        // Detectar si es una petición AJAX
        boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With")) ||
                        (accept != null && accept.contains("application/json")) ||
                        request.getRequestURI().contains("/ajax");
        
        if (isAjax) {
            // Respuesta para peticiones AJAX
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(false, "File size exceeds maximum allowed size (2MB)", null));
        } else {
            // Respuesta para peticiones normales
            redirectAttributes.addFlashAttribute("error", 
                "File size exceeds maximum allowed size (2MB). Please choose a smaller file.");
            
            String referer = request.getHeader("Referer");
            if (referer != null && !referer.isEmpty()) {
                return "redirect:" + referer;
            } else {
                return "redirect:/";
            }
        }
    }
    
    /**
     * Clase para respuestas de error en AJAX
     */
    public static class ErrorResponse {
        private boolean success;
        private String message;
        private String photoUrl;
        
        public ErrorResponse(boolean success, String message, String photoUrl) {
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
