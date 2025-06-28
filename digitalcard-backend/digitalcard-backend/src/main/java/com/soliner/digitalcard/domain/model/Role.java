package com.soliner.digitalcard.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
// import lombok.ToString; // Kaldırıldı veya exclude olmadan kullanılacak

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
@Builder // Role.builder() metodunun oluşması için
// @ToString(exclude = {"users"}) // KRİTİK DÜZELTME: Bu satır kaldırıldı çünkü users alanı yok
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING) // Enum tipindeki rolleri String olarak sakla
    @Column(length = 20)
    private ERole name; // ERole enum'ı kullanılacak

    // Not: Eğer Role entity'nizde 'users' adında bir ManyToMany ilişkisi varsa ve toString'de sorun yaratıyorsa,
    // o zaman bu alanı buraya ekleyip @ToString(exclude = {"users"})'ı tekrar kullanabilirsiniz.
    // Sizin verdiğiniz güncel Role.java'da olmadığı için şimdilik kaldırıldı.
    // private java.util.Set<User> users;
}
