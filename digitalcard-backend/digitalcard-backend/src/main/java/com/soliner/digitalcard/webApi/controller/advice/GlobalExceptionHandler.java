package com.soliner.digitalcard.webApi.controller.advice;

import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;
//
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.soliner.digitalcard.core.types.exceptions.InvalidInputException;
import com.soliner.digitalcard.core.types.exceptions.ResourceNotFoundException;
import com.soliner.digitalcard.core.types.exceptions.UnauthorizedException;
import com.soliner.digitalcard.webApi.dto.error.ProblemDetails;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetails> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        ProblemDetails problemDetails = new ProblemDetails(
                new Date(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                null
        );
        return new ResponseEntity<>(problemDetails, HttpStatus.NOT_FOUND);
    }

    /**
     * Geçersiz giriş (InvalidInputException) yakalar ve HTTP 409 Conflict yanıtı döndürür.
     * Kullanıcı adı/e-posta zaten mevcut gibi durumlar için 409 daha uygun.
     * @param ex Yakalanan InvalidInputException nesnesi.
     * @param request İstek detaylarına erişim için WebRequest nesnesi.
     * @return Hata detaylarını içeren ProblemDetails nesnesi ve 409 Conflict durumu.
     */
    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ProblemDetails> handleInvalidInputException(InvalidInputException ex, WebRequest request) {
        ProblemDetails problemDetails = new ProblemDetails(
                new Date(),
                // BURAYI DEĞİŞTİRDİK: HttpStatus.BAD_REQUEST -> HttpStatus.CONFLICT
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                null
        );
        // BURAYI DEĞİŞTİRDİK: HttpStatus.BAD_REQUEST -> HttpStatus.CONFLICT
        return new ResponseEntity<>(problemDetails, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ProblemDetails> handleUnauthorizedException(UnauthorizedException ex, WebRequest request) {
        ProblemDetails problemDetails = new ProblemDetails(
                new Date(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                null
        );
        return new ResponseEntity<>(problemDetails, HttpStatus.UNAUTHORIZED);
    }

    /**
     * @Valid anotasyonu ile tetiklenen validasyon hatalarını (MethodArgumentNotValidException) yakalar
     * ve HTTP 400 Bad Request yanıtı döndürür. Hata detayları alan bazında gösterilir.
     * @param ex Yakalanan MethodArgumentNotValidException nesnesi.
     * @param request İstek detaylarına erişim için WebRequest nesnesi.
     * @return Hata detaylarını içeren ProblemDetails nesnesi ve 400 Bad Request durumu.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetails> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> fieldError.getDefaultMessage()
                ));

        ProblemDetails problemDetails = new ProblemDetails(
                new Date(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validasyon Hatası",
                request.getDescription(false).replace("uri=", ""),
                errors
        );
        return new ResponseEntity<>(problemDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetails> handleGlobalException(Exception ex, WebRequest request) {
        ProblemDetails problemDetails = new ProblemDetails(
                new Date(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Beklenmeyen bir hata oluştu: " + ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                null
        );
        ex.printStackTrace();
        return new ResponseEntity<>(problemDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}