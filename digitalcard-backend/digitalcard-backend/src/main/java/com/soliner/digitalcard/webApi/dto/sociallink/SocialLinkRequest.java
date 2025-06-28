package com.soliner.digitalcard.webApi.dto.sociallink;

import lombok.AllArgsConstructor; // AllArgsConstructor eklendi
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;  // NoArgsConstructor eklendi
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
// import jakarta.validation.constraints.NotNull; // Eğer doğrudan kullanılmıyorsa bu import kaldırılabilir.

/**
 * Sosyal medya linki oluşturma veya güncelleme istekleri için kullanılan veri transfer nesnesi (DTO).
 * Platform adı, URL ve ilişkili kullanıcı ID'si bilgilerini içerir.
 */
@Builder
@Data
@NoArgsConstructor // Lombok: Boş bir yapıcı metot oluşturur.
@AllArgsConstructor // Lombok: Tüm alanları içeren bir yapıcı metot oluşturur.
public class SocialLinkRequest {
    @NotBlank(message = "Platform adı boş olamaz")
    @Size(max = 255, message = "Platform adı 255 karakterden uzun olamaz")
    private String platform;

    @NotBlank(message = "URL boş olamaz")
    @Size(max = 500, message = "URL 500 karakterden uzun olamaz")
    private String url;
}
