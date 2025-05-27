package com.soliner.digitalcard.persistence.repository;


import com.soliner.digitalcard.domain.model.User; // User Entity'sini import ediyoruz

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository; // Spring Data JPA'nın temel Repository arayüzünü import ediyoruz
import org.springframework.stereotype.Repository; // İsteğe bağlı ama iyi uygulama için eklenir

/**
 * Kullanıcı (User) Entity'si için veri erişim işlemlerini sağlayan Repository arayüzü.
 * JpaRepository'den miras alarak temel CRUD operasyonlarını otomatik olarak sağlar.
 * <User, Long>: İlk parametre Entity tipi, ikinci parametre Entity'nin ID'sinin tipi (User'da Long idi).
 * infrastructure katmanına aittir.
 */
@Repository // Bu arayüzün bir Spring bileşeni olduğunu ve veri erişim katmanına ait olduğunu belirtir
public interface UserRepository extends JpaRepository<User, Long> {
    // Özel sorgu metotları (örneğin kullanıcı adına göre arama) buraya eklenebilir.
    // Spring Data JPA, metot isimlerine göre otomatik sorgu oluşturabilir.
    // Örneğin:
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email); // Eğer UserServiceImpl'de kullanılıyorsa ekleyin

}