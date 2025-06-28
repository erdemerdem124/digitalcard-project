package com.soliner.digitalcard.application.services.impl;

import com.soliner.digitalcard.application.services.interfaces.RoleService;
import com.soliner.digitalcard.domain.model.ERole;
import com.soliner.digitalcard.domain.model.Role;
import com.soliner.digitalcard.persistence.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * RoleService arayüzünün uygulama sınıfı.
 * Rollerle ilgili iş mantığını yönetir ve veritabanı işlemleri için RoleRepository'yi kullanır.
 */
@Service // Bu sınıfın bir Spring Service bean'i olduğunu belirtir
@RequiredArgsConstructor // Lombok: final alanlar için constructor oluşturur
@Slf4j // Lombok: Logger objesi oluşturur
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    /**
     * Verilen roldeki ada göre bir rolü bulur.
     *
     * @param name Aranacak rol adı (ERole enum'ından).
     * @return Bulunursa rolü içeren bir Optional, aksi takdirde boş bir Optional.
     */
    @Override
    @Transactional(readOnly = true) // Sadece okuma işlemleri için optimize eder
    public Optional<Role> findByName(ERole name) {
        log.debug("findByName metodu çağrıldı. Rol adı: {}", name);
        return roleRepository.findByName(name);
    }

    /**
     * Yeni bir rol oluşturur ve kaydeder.
     * Rol zaten mevcutsa, mevcut rolü döndürür (idempotent).
     *
     * @param name Oluşturulacak rol adı (ERole enum'ından).
     * @return Oluşturulan veya mevcut rol.
     */
    @Override
    @Transactional // Yazma işlemleri için transaction yönetimi
    public Role createRole(ERole name) {
        log.info("createRole metodu çağrıldı. Oluşturulacak rol adı: {}", name);
        // Eğer rol zaten varsa, onu döndür, yoksa yeni bir tane oluşturup kaydet
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(name);
                    log.info("Yeni rol kaydediliyor: {}", name);
                    return roleRepository.save(newRole);
                });
    }
}
