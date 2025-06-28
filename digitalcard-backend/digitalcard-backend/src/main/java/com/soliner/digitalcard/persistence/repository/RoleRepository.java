package com.soliner.digitalcard.persistence.repository;

import com.soliner.digitalcard.domain.model.ERole;
import com.soliner.digitalcard.domain.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Role entity'si için veri erişim operasyonlarını tanımlayan JPA Repository arayüzü.
 * persistence katmanına aittir.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    // Rol adına göre rol bulma metodu
    Optional<Role> findByName(ERole name);
}
