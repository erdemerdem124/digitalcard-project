package com.soliner.digitalcard.persistence.repository;
import com.soliner.digitalcard.domain.model.SocialLink;
import com.soliner.digitalcard.domain.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Sosyal Link verilerine erişim için Repository arayüzü.
 * Spring Data JPA tarafından otomatik olarak implemente edilir.
 * infrastructure katmanına aittir.
 */
@Repository
public interface SocialLinkRepository extends JpaRepository<SocialLink, Long> {
	  // Belirli bir kullanıcıya ait bir sosyal linki platformuna göre bulur
    Optional<SocialLink> findByUserAndPlatform(User user, String platform);
    /**
     * Belirli bir kullanıcıya ait tüm sosyal linkleri bulur.
     * Spring Data JPA, metod isminden otomatik olarak sorguyu oluşturur.
     * @param userId Sosyal linklerin ait olduğu kullanıcının ID'si.
     * @return Belirtilen kullanıcıya ait SocialLink listesi.
     */
    List<SocialLink> findByUserId(Long userId); // KRİTİK DÜZELTME: findByUser_Id yerine findByUserId kullanıldı
}
