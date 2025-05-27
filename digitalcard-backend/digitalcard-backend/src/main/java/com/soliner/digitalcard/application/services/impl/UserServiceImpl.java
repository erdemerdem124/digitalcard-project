package com.soliner.digitalcard.application.services.impl;

import com.soliner.digitalcard.application.mapper.UserMapper;
import com.soliner.digitalcard.application.services.interfaces.UserService;
import com.soliner.digitalcard.core.types.exceptions.ResourceNotFoundException;
import com.soliner.digitalcard.core.types.exceptions.InvalidInputException;
import com.soliner.digitalcard.domain.model.User;
import com.soliner.digitalcard.persistence.repository.UserRepository;
import com.soliner.digitalcard.webApi.dto.user.UserRequest;
import com.soliner.digitalcard.webApi.dto.user.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * UserService arayüzünün implementasyonu.
 * Kullanıcı iş mantığı operasyonlarını gerçekleştirir ve UserRepository aracılığıyla veri erişimi sağlar.
 * application katmanına aittir.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        if (userRepository.findByUsername(userRequest.getUsername()).isPresent()) {
            throw new InvalidInputException("Kullanıcı adı '" + userRequest.getUsername() + "' zaten mevcut.");
        }
        if (userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
            throw new InvalidInputException("E-posta adresi '" + userRequest.getEmail() + "' zaten kayıtlı.");
        }

        User user = userMapper.toEntity(userRequest);
        user.setPasswordHash(passwordEncoder.encode(userRequest.getPassword()));

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "ID", id));

        if (!existingUser.getUsername().equals(userRequest.getUsername()) &&
            userRepository.findByUsername(userRequest.getUsername()).isPresent()) {
            throw new InvalidInputException("Kullanıcı adı '" + userRequest.getUsername() + "' zaten mevcut.");
        }
        if (!existingUser.getEmail().equals(userRequest.getEmail()) &&
            userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
            throw new InvalidInputException("E-posta adresi '" + userRequest.getEmail() + "' zaten kayıtlı.");
        }

        userMapper.updateEntityFromDto(userRequest, existingUser);
        
        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
            existingUser.setPasswordHash(passwordEncoder.encode(userRequest.getPassword()));
        }

        User updatedUser = userRepository.save(existingUser);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    // *** BURADAKİ DEĞİŞİKLİK: ARTIK Optional<User> DÖNDÜRÜYORUZ ***
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Kullanıcı", "ID", id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Kullanıcı adı veya e-posta ile kullanıcı bulur.
     * @param usernameOrEmail Kullanıcı adı veya e-posta adresi.
     * @return Kullanıcı bulunursa Optional<User>, aksi takdirde boş Optional.
     */
    @Override
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        // Önce kullanıcı adına göre ara
        Optional<User> userByUsername = userRepository.findByUsername(usernameOrEmail);
        if (userByUsername.isPresent()) {
            return userByUsername;
        }
        // Bulunamazsa e-posta adresine göre ara
        return userRepository.findByEmail(usernameOrEmail);
    }

	@Override
	public void updatePassword(Long userId, String currentPassword, String newPassword) {
		// TODO Auto-generated method stub
		 User user = userRepository.findById(userId)
		            .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "ID", userId));

		    // Mevcut şifreyi doğrula
		    if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
		        throw new IllegalArgumentException("Mevcut şifre yanlış.");
		    }

		    // Yeni şifreyi encode et ve kaydet
		    user.setPasswordHash(passwordEncoder.encode(newPassword));
		    userRepository.save(user);
	}
}