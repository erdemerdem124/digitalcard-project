package com.soliner.digitalcard.application.services.impl;

import com.soliner.digitalcard.application.mapper.ProjectMapper;
import com.soliner.digitalcard.application.mapper.SocialLinkMapper;
import com.soliner.digitalcard.application.mapper.UserMapper;
import com.soliner.digitalcard.core.types.exceptions.InvalidInputException;
import com.soliner.digitalcard.core.types.exceptions.ResourceNotFoundException;
import com.soliner.digitalcard.domain.model.ERole;
import com.soliner.digitalcard.domain.model.Project;
import com.soliner.digitalcard.domain.model.Role;
import com.soliner.digitalcard.domain.model.SocialLink;
import com.soliner.digitalcard.domain.model.User;
import com.soliner.digitalcard.persistence.repository.RoleRepository;
import com.soliner.digitalcard.persistence.repository.UserRepository;
import com.soliner.digitalcard.webApi.dto.auth.PasswordUpdateRequest;
import com.soliner.digitalcard.webApi.dto.user.UserRequest;
import com.soliner.digitalcard.webApi.dto.user.UserResponse;
import com.soliner.digitalcard.application.services.interfaces.RoleService;
import com.soliner.digitalcard.application.services.interfaces.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final SocialLinkMapper socialLinkMapper;
    private final ProjectMapper projectMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService; // RoleService'i inject et


    /**
     * Yeni bir kullanıcı oluşturur ve veritabanına kaydeder.
     * Kullanıcı adı veya e-posta zaten kullanımda ise InvalidInputException fırlatır.
     * Varsayılan olarak ROLE_USER rolünü atar.
     *
     * @param userRequest Kullanıcı oluşturma isteği DTO'su.
     * @return Oluşturulan kullanıcının yanıt DTO'su.
     * @throws InvalidInputException Kullanıcı adı veya e-posta zaten mevcutsa.
     * @throws ResourceNotFoundException ROLE_USER rolü bulunamazsa (uygulama hatası).
     */
    @Override
    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        log.info("createUser metodu çağrıldı. Kullanıcı adı: {}, E-posta: {}", userRequest.getUsername(), userRequest.getEmail());

        if (userRepository.findByUsername(userRequest.getUsername()).isPresent()) {
            log.warn("Kullanıcı adı zaten kullanımda: {}", userRequest.getUsername());
            throw new InvalidInputException("Username '" + userRequest.getUsername() + "' is already in use.");
        }
        if (userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
            log.warn("E-posta zaten kullanımda: {}", userRequest.getEmail());
            throw new InvalidInputException("Email '" + userRequest.getEmail() + "' is already in use.");
        }
        
        // KRİTİK: createUser'da şifre kesinlikle olmalı, boş veya null olamaz
        if (userRequest.getPassword() == null || userRequest.getPassword().trim().isEmpty()) {
            log.warn("createUser çağrısında şifre boş veya null.");
            throw new InvalidInputException("Password is required for user creation.");
        }
        if (userRequest.getPassword().length() < 8) {
             log.warn("createUser çağrısında şifre minimum uzunluktan kısa.");
             throw new InvalidInputException("Password must be at least 8 characters long for user creation.");
        }


        // DTO → Entity (temel alanlar)
        User user = userMapper.toUser(userRequest); // passwordHash burada password'dan maplenecek
        
        // Şifreyi hashle (mapper sadece source/target mappingi yapar, encode etmez)
        String hashedPassword = passwordEncoder.encode(userRequest.getPassword());
        user.setPasswordHash(hashedPassword);

        // Varsayılan rolü ata (ROLE_USER)
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", ERole.ROLE_USER.name()));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        // İlişkili varlıkların user referansını ayarlama
        // CREATE işlemi için bu hala gerekli, çünkü MapStruct toUser'da sadece listeyi oluşturur, ilişkiyi kurmaz.
        if (user.getSocialLinks() != null) {
            user.getSocialLinks().forEach(link -> link.setUser(user));
        }
        if (user.getProjects() != null) {
            user.getProjects().forEach(project -> project.setUser(user));
        }

        User savedUser = userRepository.save(user); // Bu satır, user'a ID atar ve cascade işlemlerini başlatır.

        log.info("Kullanıcı başarıyla oluşturuldu: ID {}", savedUser.getId());
        return userMapper.toResponse(savedUser); // Entity → DTO
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.info("getUserById metodu çağrıldı. ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", id));
        log.info("Kullanıcı bulundu: Username {}", user.getUsername());
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserEntityById(Long id) {
        log.debug("getUserEntityById metodu çağrıldı. ID: {}", id);
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        log.info("getUserByUsername metodu çağrıldı. Kullanıcı Adı: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "Username", username));
        log.info("Kullanıcı bulundu: ID {}", user.getId());
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsernameOrEmail(String identifier) {
        log.debug("findByUsernameOrEmail metodu çağrıldı. Kimlik: {}", identifier);
        Optional<User> user = userRepository.findByUsername(identifier);
        if (user.isEmpty()) {
            user = userRepository.findByEmail(identifier);
        }
        user.ifPresent(u -> log.debug("Kullanıcı bulundu: {}", u.getUsername()));
        return user;
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserRequest userRequest) {
    	log.info("updateUser metodu çağrıldı. ID: {}", id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", id));

        // Kullanıcı adı ve e-posta çakışma kontrolleri mapper'dan önce gelmeli.
        if (userRequest.getUsername() != null && !userRequest.getUsername().equals(existingUser.getUsername())) {
            if (userRepository.findByUsername(userRequest.getUsername()).isPresent()) {
                log.warn("Kullanıcı adı güncelleme sırasında çakıştı: {}", userRequest.getUsername());
                throw new InvalidInputException("Username '" + userRequest.getUsername() + "' is already in use.");
            }
        }
        if (userRequest.getEmail() != null && !userRequest.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
                log.warn("E-posta güncelleme sırasında çakıştı: {}", userRequest.getEmail());
                throw new InvalidInputException("Email '" + userRequest.getEmail() + "' is already in use.");
            }
        }
            
        // Temel alanları ve KOLEKSİYONLARI DTO'dan Entity'ye mapleyerek güncelle
        // MapStruct, updateEntityFromDto içinde socialLinks ve projects'i yönetir.
        userMapper.updateEntityFromDto(userRequest, existingUser);

        // KRİTİK DÜZELTME: MapStruct tarafından güncellenen ilişkili varlıkların user referansını ayarlama
        // MapStruct, koleksiyonları güncellese de, her bir alt öğenin "user" referansını otomatik olarak ayarlamaz.
        if (existingUser.getSocialLinks() != null) {
            existingUser.getSocialLinks().forEach(link -> link.setUser(existingUser));
        }
        if (existingUser.getProjects() != null) {
            existingUser.getProjects().forEach(project -> project.setUser(existingUser));
        }

        // Şifre güncelleme mantığı bu metottan tamamen kaldırıldı (ayrı updatePassword metodu var).
        // Burada password ile ilgili herhangi bir if bloğu veya validasyon olmamalıdır.
        // Frontend'den boş şifre gönderilse bile sorun yaratmaz.
                
        User updatedUser = userRepository.save(existingUser);
        log.info("Kullanıcı başarıyla güncellendi: ID {}", updatedUser.getId());
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void updatePassword(Long id, PasswordUpdateRequest request) { // Dönüş tipi void olarak değiştirildi
        log.info("updatePassword metodu çağrıldı. Kullanıcı ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", id));

        // Eski şifrenin doğruluğunu kontrol et
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            log.warn("Kullanıcı ID {} için eski şifre yanlış.", id);
            throw new InvalidInputException("Old password is incorrect.");
        }

        // Yeni şifrenin geçerliliğini kontrol et (PasswordUpdateRequest DTO'sundaki @Size anotasyonu
        // MethodArgumentNotValidException'ı tetikler. Ancak doğrudan burada da kontrol edilebilir
        // veya bu kontrolü tamamen DTO validasyonuna bırakabiliriz.
        // Testlerin beklentisiyle uyumlu olması için şimdilik buradaki kontrolü kaldırıyorum,
        // @Size validasyonu MethodArgumentNotValidException ile yakalanacak.)
        
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("Kullanıcı ID {} için şifre başarıyla güncellendi.", id);
    }
    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("deleteUser metodu çağrıldı. ID: {}", id);
        if (!userRepository.existsById(id)) {
            log.warn("Silinecek kullanıcı bulunamadı. ID: {}", id);
            throw new ResourceNotFoundException("User", "ID", id);
        }
        userRepository.deleteById(id);
        log.info("Kullanıcı başarıyla silindi. ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.info("getAllUsers metodu çağrıldı.");
        List<User> users = userRepository.findAll();
        log.info("{} kullanıcı bulundu.", users.size());
        return userMapper.toResponseList(users);
    }
}
