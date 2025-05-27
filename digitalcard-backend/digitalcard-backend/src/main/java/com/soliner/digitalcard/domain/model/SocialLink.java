package com.soliner.digitalcard.domain.model;


import jakarta.persistence.*; // JPA anotasyonları için
import lombok.AllArgsConstructor; // Lombok: Tüm argümanları içeren constructor oluşturur
import lombok.Builder; // Lombok: Builder deseni oluşturur
import lombok.Data; // Lombok: Getter, Setter, equals, hashCode, toString metodlarını otomatik oluşturur
import lombok.NoArgsConstructor; // Lombok: Argümansız constructor oluşturur

/**
 * Sosyal Link varlığını temsil eden domain katmanı Entity sınıfı.
 * Veritabanındaki 'social_links' tablosuna karşılık gelir.
 * Domain katmanına aittir.
 */
@Entity // Bu sınıfın bir JPA varlığı olduğunu belirtir
@Table(name = "social_links") // Veritabanındaki tablo adını belirtir
@Data // Lombok: Getter, Setter, equals, hashCode, toString metodlarını otomatik oluşturur
@NoArgsConstructor // Lombok: Argümansız constructor oluşturur
@AllArgsConstructor // Lombok: Tüm argümanları içeren constructor oluşturur
@Builder // Lombok: Builder deseni oluşturur
public class SocialLink {

    @Id // Primary key olduğunu belirtir
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID'nin otomatik artan olduğunu belirtir
    private Long id;

    @Column(nullable = false, length = 50) // Boş olamaz ve maksimum uzunluk
    private String platform; // Örneğin: "LinkedIn", "GitHub", "Twitter"

    @Column(nullable = false, length = 255) // Boş olamaz ve maksimum uzunluk
    private String url; // Sosyal linkin URL'si

    // Many-to-One ilişki: Birden fazla sosyal link bir kullanıcıya ait olabilir
    // optional = false: Bu sosyal linkin bir kullanıcıya sahip olması zorunludur
    @ManyToOne(fetch = FetchType.LAZY) // İlişkinin tembel yüklenmesini sağlar (performans için)
    @JoinColumn(name = "user_id", nullable = false) // Veritabanındaki yabancı anahtar sütununu belirtir
    private User user; // İlişkili User Entity'si
}
