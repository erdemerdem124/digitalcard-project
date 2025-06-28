package com.soliner.digitalcard.webApi.dto.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * HTTP API'lerinde hata yanıtları için kullanılan standartlaştırılmış Problem Details DTO'su (RFC 7807).
 * Bu DTO, HTTP yanıt formatıyla ilgili olduğu için webApi katmanına aittir.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor // Bu anotasyon 6 argümanlı constructor'ı otomatik oluşturur.
public class ProblemDetails {
    private LocalDateTime timestamp;
    private Integer status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> validationErrors;

    // KRİTİK DÜZELTME: GlobalExceptionHandler'ın çağırdığı 5 argümanlı constructor eklendi.
    // Bu constructor, Lombok'un otomatik oluşturduğu 6 argümanlı constructor'ı çağırarak çalışır.
    public ProblemDetails(LocalDateTime timestamp, Integer status, String error, String message, String path) {
        this(timestamp, status, error, message, path, null); // 6 argümanlı constructor'ı çağırır
    }
}
