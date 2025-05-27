package com.soliner.digitalcard.webApi.dto.error;

import java.util.Date;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * HTTP API'lerinde hata yanıtları için kullanılan standartlaştırılmış Problem Details DTO'su (RFC 7807).
 * Bu DTO, HTTP yanıt formatıyla ilgili olduğu için webApi katmanına aittir.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProblemDetails {
    private Date timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> details; // Ek hata detayları (örn: validasyon hataları için alan bazlı mesajlar)
}
