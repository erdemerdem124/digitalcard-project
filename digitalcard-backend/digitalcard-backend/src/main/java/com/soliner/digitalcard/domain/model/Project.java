package com.soliner.digitalcard.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;           // Bu import olmalı
import lombok.Setter;           // Bu import olmalı
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode; // Bu import olmalı
import lombok.ToString;         // Bu import olmalı
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Proje bilgilerini temsil eden JPA Entity sınıfı.
 * Veritabanındaki 'projects' tablosuna maplenir.
 */
@Entity
@Table(name = "projects", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "title"})
})
@Getter // Tüm alanlar için getter metotları üretir
@Setter // Tüm alanlar için setter metotları üretir
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"user"}) // KRİTİK: user ilişkisini equals/hashCode'dan hariç tut
@ToString(exclude = {"user"})         // KRİTİK: user ilişkisini toString'den hariç tut
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many-to-One ilişkisi: Birçok proje bir kullanıcıya ait olabilir.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Projenin sahibi olan kullanıcı

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "project_url")
    private String projectUrl;

    private String technologies;

    @Column(name = "project_image_url")
    private String projectImageUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
