package com.soliner.digitalcard.webApi.dto.sociallink;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

/**
 * Sosyal medya linki oluşturma veya güncelleme istekleri için kullanılan veri transfer nesnesi (DTO).
 * Platform adı, URL ve ilişkili kullanıcı ID'si bilgilerini içerir.
 */
@Data // Lombok: Otomatik olarak getter, setter, equals, hashCode ve toString metodlarını oluşturur.
public class SocialLinkRequest {
    @NotBlank(message = "Platform adı boş olamaz") // Platform adının boş veya sadece boşluklardan oluşmamasını sağlar.
    @Size(max = 255, message = "Platform adı 255 karakterden uzun olamaz") // Platform adının maksimum uzunluğu.
    private String platform;

    @NotBlank(message = "URL boş olamaz") // URL'nin boş veya sadece boşluklardan oluşmamasını sağlar.
    @Size(max = 500, message = "URL 500 karakterden uzun olamaz") // URL'nin maksimum uzunluğu.
    private String url;

    @NotNull(message = "Kullanıcı ID'si boş olamaz") // Bu sosyal linkin hangi kullanıcıya ait olduğunu belirten kullanıcı ID'si. Boş olamaz.
    private Long userId;
    }