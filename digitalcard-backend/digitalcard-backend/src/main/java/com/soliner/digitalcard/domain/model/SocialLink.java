package com.soliner.digitalcard.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp; // Hibernate'in timestamp anotasyonları
import org.hibernate.annotations.UpdateTimestamp;   // Hibernate'in timestamp anotasyonları

import java.time.LocalDateTime; // Tarih ve saat için

/**
 * Sosyal medya link bilgilerini temsil eden JPA Entity sınıfı.
 * Veritabanındaki 'social_links' tablosuna maplenir.
 */
@Entity
@Table(name = "social_links", uniqueConstraints = { // KRİTİK DÜZELTME: Benzersiz kısıtlama eklendi
        @UniqueConstraint(columnNames = {"user_id", "platform"}) // Bir kullanıcının aynı platformda birden fazla linki olamaz
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String platform;

    @Column(nullable = false)
    private String url;

    // Many-to-One ilişkisi: Birçok sosyal link bir kullanıcıya ait olabilir.
    // nullable = false: user_id sütunu NULL olamaz. Bu bizim için kritik!
    // @JoinColumn: social_links tablosundaki foreign key sütununu belirtir.
    @ManyToOne(fetch = FetchType.LAZY) // Sosyal linki çekerken kullanıcıyı hemen çekme (performans)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp // Kayıt oluşturulduğunda otomatik olarak zamanı ayarlar
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // Kayıt güncellendiğinde otomatik olarak zamanı ayarlar
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
