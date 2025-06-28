package com.soliner.digitalcard.application.services.interfaces;

import com.soliner.digitalcard.domain.model.User; // User entity import edildi
import com.soliner.digitalcard.webApi.dto.auth.PasswordUpdateRequest;
import com.soliner.digitalcard.webApi.dto.user.UserRequest;
import com.soliner.digitalcard.webApi.dto.user.UserResponse;

import java.util.List;
import java.util.Optional; // Optional import edildi

public interface UserService {
    UserResponse createUser(UserRequest userRequest);
    UserResponse getUserById(Long id);
    UserResponse getUserByUsername(String username);
    Optional<User> findByUsernameOrEmail(String identifier);
    UserResponse updateUser(Long id, UserRequest userRequest);
    void updatePassword(Long id, PasswordUpdateRequest request);
    void deleteUser(Long id);
    List<UserResponse> getAllUsers();

    // KRİTİK EKLENTİ: Doğrudan User entity'sini döndüren metot
    Optional<User> getUserEntityById(Long id);
}
