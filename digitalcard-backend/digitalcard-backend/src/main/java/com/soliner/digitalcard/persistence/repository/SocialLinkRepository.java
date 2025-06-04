package com.soliner.digitalcard.persistence.repository;
import com.soliner.digitalcard.domain.model.SocialLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Sosyal Link verilerine erişim için Repository arayüzü.
 * Spring Data JPA tarafından otomatik olarak implemente edilir.
 * infrastructure katmanına aittir.
 */
@Repository
public interface SocialLinkRepository extends JpaRepository<SocialLink, Long> {

    /**
     * Belirli bir kullanıcıya ait tüm sosyal linkleri bulur.
     * Spring Data JPA, metod isminden otomatik olarak sorguyu oluşturur.
     * @param userId Sosyal linklerin ait olduğu kullanıcının ID'si.
     * @return Belirtilen kullanıcıya ait SocialLink listesi.
     */
    List<SocialLink> findByUser_Id(Long userId); // Bu metodu ekleyin
}
