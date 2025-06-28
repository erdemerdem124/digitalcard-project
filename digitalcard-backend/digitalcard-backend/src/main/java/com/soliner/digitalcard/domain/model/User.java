package com.soliner.digitalcard.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.hibernate.annotations.CreationTimestamp; // Hibernate'in timestamp anotasyonları
import org.hibernate.annotations.UpdateTimestamp;   // Hibernate'in timestamp anotasyonları

import java.time.LocalDateTime; // Tarih ve saat için
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Kullanıcı bilgilerini temsil eden JPA Entity sınıfı.
 * Veritabanındaki 'users' tablosuna maplenir.
 */
@Entity
@Table(name = "users", uniqueConstraints = { // Unique kısıtlamaları eklendi
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "profile_photo_url") // Profil fotoğrafı URL'si
    private String profilePhotoUrl;

    private String bio; // Biyografi

    private String title; // Ünvan, meslek vb.

    private String address; // Konum veya adres bilgisi

    @Column(name = "phone_number") // Telefon numarası
    private String phoneNumber;

    private String website; // Kişisel web sitesi veya portföy URL'si

    @CreationTimestamp // Kayıt oluşturulduğunda otomatik olarak zamanı ayarlar
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // Kayıt güncellendiğinde otomatik olarak zamanı ayarlar
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Sosyal link ilişkisi
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY) // FetchType.LAZY eklendi
    private List<SocialLink> socialLinks = new ArrayList<>();

    // Proje ilişkisi
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY) // FetchType.LAZY eklendi
    private List<Project> projects = new ArrayList<>();

    // Rol ilişkisi
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // Yardımcı metodlar (ilişkileri doğru kurmak için)
    public void addSocialLink(SocialLink socialLink) {
        if (socialLinks == null) {
            socialLinks = new ArrayList<>();
        }
        socialLinks.add(socialLink);
        socialLink.setUser(this);
    }

    public void removeSocialLink(SocialLink socialLink) {
        socialLinks.remove(socialLink);
        socialLink.setUser(null);
    }

    public void addProject(Project project) {
        if (projects == null) {
            projects = new ArrayList<>();
        }
        projects.add(project);
        project.setUser(this);
    }

    public void removeProject(Project project) {
        projects.remove(project);
        project.setUser(null);
    }

    public void addRole(Role role) {
        if (roles == null) {
            roles = new HashSet<>();
        }
        roles.add(role);
    }

    public void removeRole(Role role) {
        roles.remove(role);
    }
}
