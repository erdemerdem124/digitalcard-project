package com.soliner.digitalcard.application.services.interfaces;
import java.util.Optional;

import com.soliner.digitalcard.domain.model.ERole;
import com.soliner.digitalcard.domain.model.Role;

public interface RoleService {

/**
 * Verilen roldeki ada göre bir rolü bulur.
 *
 * @param name Aranacak rol adı (ERole enum'ından).
 * @return Bulunursa rolü içeren bir Optional, aksi takdirde boş bir Optional.
 */
Optional<Role> findByName(ERole name);

/**
 * Yeni bir rol oluşturur ve kaydeder.
 *
 * @param name Oluşturulacak rol adı (ERole enum'ından).
 * @return Oluşturulan rol.
 */
Role createRole(ERole name);
}
