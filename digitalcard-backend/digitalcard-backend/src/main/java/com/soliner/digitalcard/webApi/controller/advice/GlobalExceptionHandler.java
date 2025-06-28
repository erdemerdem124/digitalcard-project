package com.soliner.digitalcard.webApi.controller.advice;

import com.soliner.digitalcard.core.types.exceptions.InvalidInputException;
import com.soliner.digitalcard.core.types.exceptions.ResourceNotFoundException;
import com.soliner.digitalcard.core.types.exceptions.UnauthorizedException;
import com.soliner.digitalcard.webApi.dto.error.ProblemDetails; // DTO'nuz ProblemDetails ise bu import doğru
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ProblemDetails> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        ProblemDetails problemDetails = new ProblemDetails(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(problemDetails, HttpStatus.NOT_FOUND);
    }

    // KRİTİK DÜZELTME: InvalidInputException'ı artık 400 Bad Request olarak ele alıyoruz.
    // Bu, şifre boş/kısa gibi doğrudan kullanıcı giriş hatalarını doğru şekilde yansıtır.
    @ExceptionHandler(InvalidInputException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // Burayı CONFLICT yerine BAD_REQUEST olarak değiştirin
    public ResponseEntity<ProblemDetails> handleInvalidInputException(InvalidInputException ex, WebRequest request) {
        ProblemDetails problemDetails = new ProblemDetails(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(), // Burayı CONFLICT yerine BAD_REQUEST olarak değiştirin
                "Bad Request", // Burayı "Conflict" yerine "Bad Request" olarak değiştirin
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(problemDetails, HttpStatus.BAD_REQUEST); // Burayı CONFLICT yerine BAD_REQUEST olarak değiştirin
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ProblemDetails> handleUnauthorizedException(UnauthorizedException ex, WebRequest request) {
        ProblemDetails problemDetails = new ProblemDetails(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(problemDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ProblemDetails> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage
                ));

        ProblemDetails problemDetails = new ProblemDetails(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Doğrulama hatası oluştu.",
                request.getDescription(false).replace("uri=", ""),
                errors
        );
        return new ResponseEntity<>(problemDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ProblemDetails> handleGlobalException(Exception ex, WebRequest request) {
        ProblemDetails problemDetails = new ProblemDetails(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Beklenmeyen bir hata oluştu: " + ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        ex.printStackTrace(); // Hata yığın izini konsola yazdır
        return new ResponseEntity<>(problemDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
	