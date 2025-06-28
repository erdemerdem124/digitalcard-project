package com.soliner.digitalcard.webApi.dto.sociallink;

import lombok.AllArgsConstructor; // AllArgsConstructor eklendi
import lombok.Data;
import lombok.NoArgsConstructor;  // NoArgsConstructor eklendi

/**
 * Sosyal medya linki bilgilerini API üzerinden döndürmek için kullanılan veri transfer nesnesi (DTO).
 * Link ID'si, platform adı, URL ve ilişkili kullanıcı ID'si bilgilerini içerir.
 */
@Data // Lombok: Otomatik olarak getter, setter, equals, hashCode ve toString metodlarını oluşturur.
@NoArgsConstructor // Lombok: Boş bir yapıcı metot oluşturur.
@AllArgsConstructor // Lombok: Tüm alanları içeren bir yapıcı metot oluşturur.
public class SocialLinkResponse {
    private Long id;
    private String platform;
    private String url;
    private Long userId; // Hangi kullanıcıya ait olduğu.
}
