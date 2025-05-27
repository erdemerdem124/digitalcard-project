package com.soliner.digitalcard.application.services.interfaces;

import java.util.Optional; // findByUsername metodu için
import java.util.List;
import com.soliner.digitalcard.domain.model.User;
import com.soliner.digitalcard.webApi.dto.user.UserRequest;
import com.soliner.digitalcard.webApi.dto.user.UserResponse;

/**
 * Kullanıcı iş mantığı operasyonları için arayüz.
 * Bu arayüz, webApi katmanındaki Controller'lar tarafından çağrılır.
 * application katmanına aittir.
 */
public interface UserService {

    UserResponse createUser(UserRequest userRequest);
    UserResponse updateUser(Long id, UserRequest userRequest);

    // *** BURADAKİ DEĞİŞİKLİK: ARTIK Optional<User> DÖNDÜRÜYORUZ ***
    Optional<User> getUserById(Long id);

    void deleteUser(Long id);
    void updatePassword(Long userId, String currentPassword, String newPassword);

    Optional<User> getUserByUsername(String username);
    List<UserResponse> getAllUsers(); // Tüm kullanıcıları döndüren metot (eğer API'de kullanılacaksa)
    Optional<User> findByUsernameOrEmail(String usernameOrEmail);
}