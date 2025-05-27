package com.soliner.digitalcard.application.services.impl;

import com.soliner.digitalcard.application.services.interfaces.UserService;
import com.soliner.digitalcard.domain.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Spring Security için kullanıcı detaylarını yükleyen servis.
 * Bu sınıf, UserService'i kullanarak domain katmanından kullanıcı bilgilerini çeker
 * ve Spring Security'nin anlayacağı UserDetails objesine dönüştürür.
 * application katmanına aittir çünkü iş mantığı (kullanıcı bilgileri) ile ilgilenir.
 */
@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    public UserDetailsServiceImpl(@Lazy UserService userService) {
        this.userService = userService;
    }

    /**
     * Kullanıcı adı veya e-posta ile kullanıcı detaylarını yükler.
     * Bu metot, Spring Security kimlik doğrulama sürecinde çağrılır.
     * @param username Yüklenecek kullanıcının adı (e-posta veya kullanıcı adı olabilir).
     * @return Yüklenen kullanıcı detayları (UserDetailsImpl).
     * @throws UsernameNotFoundException Kullanıcı bulunamazsa fırlatılır.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Kullanıcı detayları yükleniyor: {}", username);

        Optional<User> userOptional = userService.findByUsernameOrEmail(username);

        if (userOptional.isEmpty()) {
            log.warn("Kullanıcı bulunamadı: {}", username);
            throw new UsernameNotFoundException("Kullanıcı bulunamadı: " + username);
        }

        User user = userOptional.get();
        log.info("Kullanıcı yüklendi: {}", user.getUsername());

        // Kendi özel UserDetailsImpl objemizi döndürüyoruz
        return UserDetailsImpl.build(user); // <-- Buradaki değişiklik!
    }
}
