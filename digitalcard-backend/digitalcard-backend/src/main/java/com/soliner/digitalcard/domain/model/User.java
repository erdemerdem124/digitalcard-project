package com.soliner.digitalcard.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList; // ArrayList import edildi
import java.util.List;     // List import edildi

@Entity
@Table(name = "users",
        uniqueConstraints = {
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

    @NotBlank
    @Size(max = 20)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @NotBlank
    @Size(max = 120)
    private String passwordHash; // Şifre hash'i

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Size(max = 255)
    private String bio;

    @Size(max = 255)
    private String profilePhotoUrl;

    @ManyToMany(fetch = FetchType.EAGER) // Roller EAGER olarak yüklendi
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    // Sosyal bağlantılar ve projeler için OneToMany ilişkileri
    // Bu ilişkileri de EAGER yapıyoruz ki User çekildiğinde otomatik yüklensinler.
    // Ancak dikkat: Çok fazla ilişkili veri varsa performans sorunlarına yol açabilir.
    // Şimdilik sorun çözümü için EAGER yapıyoruz.
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER) // EAGER yapıldı
    @Builder.Default
    private List<SocialLink> socialLinks = new ArrayList<>(); // Set yerine List kullanıyorsanız

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER) // EAGER yapıldı
    @Builder.Default
    private List<Project> projects = new ArrayList<>(); // Set yerine List kullanıyorsanız

    // Constructor, getters, setters Lombok tarafından sağlanır.
    // Ancak ek yardımcı metotlar varsa onları da buraya ekleyebilirsiniz.
    // Örneğin, addSocialLink, removeSocialLink, addProject, removeProject gibi.
    // Eğer @Data kullanıyorsanız, getSocialLinks(), getProjects() otomatik oluşur.
}