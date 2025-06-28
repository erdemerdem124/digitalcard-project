package com.soliner.digitalcard.webApi.dto.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiExceptionResponse {
    private String message;
    private int status;
    private String error;
    private LocalDateTime timestamp;
    private String path; // Hatanın meydana geldiği endpoint yolu

    public ApiExceptionResponse(String message, HttpStatus status, String path) {
        this.message = message;
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.timestamp = LocalDateTime.now();
        this.path = path;
    }
}
