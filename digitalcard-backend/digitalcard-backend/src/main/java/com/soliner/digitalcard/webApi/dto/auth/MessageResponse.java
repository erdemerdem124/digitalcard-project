package com.soliner.digitalcard.webApi.dto.auth;

import lombok.Data;
import lombok.AllArgsConstructor; // AllArgsConstructor eklendi

/**
 * Genel amaçlı başarı veya hata mesajlarını döndürmek için kullanılan DTO.
 * webApi katmanına aittir.
 */
@Data // Lombok: Otomatik olarak getter, setter, equals, hashCode ve toString metodlarını oluşturur.
@AllArgsConstructor // Lombok: Tüm alanları içeren bir yapıcı metot oluşturur (String message için).
public class MessageResponse {
    private String message;
}
