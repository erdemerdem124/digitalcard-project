package com.soliner.digitalcard.application.services.impl;

import com.soliner.digitalcard.application.mapper.UserMapper;
import com.soliner.digitalcard.core.types.exceptions.InvalidInputException;
import com.soliner.digitalcard.core.types.exceptions.ResourceNotFoundException;
import com.soliner.digitalcard.domain.model.User;
import com.soliner.digitalcard.persistence.repository.UserRepository;
import com.soliner.digitalcard.webApi.dto.user.UserRequest;
import com.soliner.digitalcard.webApi.dto.user.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * UserServiceImpl sınıfı için birim testleri.
 * Bu testler, UserServiceImpl'deki iş mantığını izole edilmiş bir ortamda doğrular.
 * Bağımlılıklar (UserRepository, UserMapper, PasswordEncoder) Mockito kullanılarak mock'lanır.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // Gevşek mockito ayarı, testleri geliştirirken yardımcı olabilir
@DisplayName("UserServiceImpl Unit Tests")
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper; // UserMapper'ı mockluyoruz

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        // Her testten önce passwordEncoder'ın encode metodunun davranışını tanımla
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
    }

    @Test
    @DisplayName("should create a user successfully")
    void createUser_shouldCreateUserSuccessfully() {
        // Test için bir UserRequest DTO'su oluştur
        UserRequest request = UserRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();

        // Servis tarafından kaydedilecek User entity'si oluştur
        User userEntity = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .firstName("Test")
                .lastName("User")
                .build();

        // Servis tarafından döndürülecek beklenen UserResponse DTO'su oluştur
        UserResponse expectedResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        // Mock davranışlarını tanımla
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty()); // Kullanıcı adı boş dönsün
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());       // E-posta boş dönsün
        when(userMapper.toEntity(request)).thenReturn(userEntity);                              // Mapper, DTO'dan entity'ye dönüştürsün
        when(userRepository.save(any(User.class))).thenReturn(userEntity);                      // Repository, herhangi bir User'ı kaydettiğinde userEntity'yi dönsün
        when(userMapper.toResponse(userEntity)).thenReturn(expectedResponse);                   // Mapper, entity'den DTO'ya dönüştürdüğünde expectedResponse'u dönsün

        // Servis metodunu çağır
        UserResponse actualResponse = userService.createUser(request);

        // Doğrulamalar
        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getUsername(), actualResponse.getUsername());
        assertEquals(expectedResponse.getEmail(), actualResponse.getEmail());
        assertEquals(expectedResponse.getId(), actualResponse.getId());
        assertEquals(expectedResponse.getFirstName(), actualResponse.getFirstName());
        assertEquals(expectedResponse.getLastName(), actualResponse.getLastName());

        // Metod çağrılarının doğrulanması
        verify(userRepository, times(1)).findByUsername(request.getUsername());
        verify(userRepository, times(1)).findByEmail(request.getEmail());
        verify(userMapper, times(1)).toEntity(request);
        verify(passwordEncoder, times(1)).encode(request.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toResponse(userEntity);
    }

    @Test
    @DisplayName("should throw InvalidInputException when username already exists")
    void createUser_shouldThrowExceptionWhenUsernameExists() {
        UserRequest request = UserRequest.builder()
                .username("existinguser")
                .email("test@example.com")
                .password("password123")
                .build();

        // Kullanıcı adı zaten mevcut olduğunda repository'den dolu bir Optional dönsün
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(new User()));

        // InvalidInputException fırlatıldığını doğrula
        InvalidInputException thrown = assertThrows(InvalidInputException.class, () -> {
            userService.createUser(request);
        });
        assertEquals("Kullanıcı adı '" + request.getUsername() + "' zaten mevcut.", thrown.getMessage());

        // Metod çağrılarının doğrulanması
        verify(userRepository, times(1)).findByUsername(request.getUsername());
        verify(userRepository, never()).findByEmail(anyString()); // E-posta kontrolü yapılmamalı
        verify(userRepository, never()).save(any(User.class));    // Kaydetme yapılmamalı
        verify(userMapper, never()).toEntity(any(UserRequest.class)); // Dönüştürme yapılmamalı
    }

    @Test
    @DisplayName("should throw InvalidInputException when email already exists")
    void createUser_shouldThrowExceptionWhenEmailExists() {
        UserRequest request = UserRequest.builder()
                .username("newuser")
                .email("existing@example.com")
                .password("password123")
                .build();

        // Kullanıcı adı boş dönsün, ancak e-posta zaten mevcut olsun
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        // InvalidInputException fırlatıldığını doğrula
        InvalidInputException thrown = assertThrows(InvalidInputException.class, () -> {
            userService.createUser(request);
        });
        assertEquals("E-posta adresi '" + request.getEmail() + "' zaten kayıtlı.", thrown.getMessage());

        // Metod çağrılarının doğrulanması
        verify(userRepository, times(1)).findByUsername(request.getUsername());
        verify(userRepository, times(1)).findByEmail(request.getEmail());
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).toEntity(any(UserRequest.class));
    }

    @Test
    @DisplayName("should update a user successfully")
    void updateUser_shouldUpdateUserSuccessfully() {
        Long userId = 1L;
        UserRequest request = UserRequest.builder()
                .username("updateduser")
                .email("updated@example.com")
                .password("newpassword")
                .firstName("Updated")
                .lastName("User")
                .build();

        User existingUser = User.builder()
                .id(userId)
                .username("olduser")
                .email("old@example.com")
                .passwordHash("oldHashedPassword")
                .firstName("Old")
                .lastName("User")
                .build();

        // Güncellenmiş User entity'sinin beklenen hali
        User updatedUserEntity = User.builder()
                .id(userId)
                .username("updateduser")
                .email("updated@example.com")
                .passwordHash("hashedPassword") // passwordEncoder tarafından hash'lenmiş hali
                .firstName("Updated")
                .lastName("User")
                .build();

        // Güncelleme sonrası dönmesi beklenen UserResponse DTO'su
        UserResponse expectedResponse = UserResponse.builder()
                .id(userId)
                .username("updateduser")
                .email("updated@example.com")
                .firstName("Updated")
                .lastName("User")
                .build();

        // Mock davranışlarını tanımla
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser)); // Mevcut kullanıcıyı bul
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty()); // Yeni kullanıcı adı boş dönsün
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());       // Yeni e-posta boş dönsün

        // userMapper.updateEntityFromDto metodunun davranışını tanımla
        // Bu metod void olduğu için doAnswer kullanıyoruz. Gerçekte ne yapacağını simüle ediyoruz.
        doAnswer(invocation -> {
            UserRequest req = invocation.getArgument(0);
            User entity = invocation.getArgument(1);
            entity.setUsername(req.getUsername());
            entity.setEmail(req.getEmail());
            entity.setFirstName(req.getFirstName());
            entity.setLastName(req.getLastName());
            // Password hash'leme işlemi burada yapılmadığı için, testte passwordEncoder mock'unu kontrol edeceğiz.
            return null;
        }).when(userMapper).updateEntityFromDto(eq(request), any(User.class));

        when(userRepository.save(any(User.class))).thenReturn(updatedUserEntity); // Kaydetme işlemi sonrası güncellenmiş entity dönsün
        when(userMapper.toResponse(updatedUserEntity)).thenReturn(expectedResponse); // Mapper, güncellenmiş entity'den DTO'ya dönüştürsün

        // Servis metodunu çağır
        UserResponse actualResponse = userService.updateUser(userId, request);

        // Doğrulamalar
        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getUsername(), actualResponse.getUsername());
        assertEquals(expectedResponse.getEmail(), actualResponse.getEmail());
        assertEquals(expectedResponse.getId(), actualResponse.getId());
        assertEquals(expectedResponse.getFirstName(), actualResponse.getFirstName());
        assertEquals(expectedResponse.getLastName(), actualResponse.getLastName());

        // Metod çağrılarının doğrulanması
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).findByUsername(request.getUsername());
        verify(userRepository, times(1)).findByEmail(request.getEmail());
        verify(userMapper, times(1)).updateEntityFromDto(eq(request), any(User.class));
        verify(passwordEncoder, times(1)).encode(request.getPassword()); // Şifre güncellendiği için encode çağrılmalı
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toResponse(updatedUserEntity);
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when updating non-existing user")
    void updateUser_shouldThrowExceptionWhenUserNotFound() {
        Long userId = 99L;
        UserRequest request = UserRequest.builder()
                .username("nonexistent")
                .email("non@example.com")
                .build();

        // Kullanıcı bulunamadığında boş Optional dönsün
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // ResourceNotFoundException fırlatıldığını doğrula
        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(userId, request);
        });

        // Hata mesajını düzeltiyoruz
        assertEquals("Kullanıcı bulunamadı ID : '" + userId + "'", thrown.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).findByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("should throw InvalidInputException when updating with existing username")
    void updateUser_shouldThrowExceptionWhenUsernameAlreadyExists() {
        Long userId = 1L;
        User existingUser = User.builder().id(userId).username("user1").email("user1@example.com").build();
        UserRequest request = UserRequest.builder().username("user2").email("user1@example.com").build(); // Farklı bir kullanıcı adı, ama mevcut

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        // Yeni kullanıcı adı zaten mevcut olduğunda dolu bir Optional dönsün (farklı bir kullanıcıya ait)
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(new User()));

        InvalidInputException thrown = assertThrows(InvalidInputException.class, () -> {
            userService.updateUser(userId, request);
        });
        assertEquals("Kullanıcı adı 'user2' zaten mevcut.", thrown.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).findByUsername(request.getUsername());
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("should get user by id successfully")
    void getUserById_shouldReturnUserSuccessfully() {
        Long userId = 1L;
        User userEntity = User.builder().id(userId).username("testuser").email("test@example.com").build();
        UserResponse expectedResponse = UserResponse.builder().id(userId).username("testuser").email("test@example.com").build();

        // userRepository.findById çağrıldığında Optional.of(userEntity) dönmesini sağla
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        // userMapper.toResponse çağrıldığında userEntity'yi expectedResponse'a dönüştürmesini sağla
        when(userMapper.toResponse(userEntity)).thenReturn(expectedResponse);

        // Servis metodunu çağır
        // userService.getUserById(userId) metodu Optional<User> döndürdüğü için,
        // önce Optional'ı açıp ardından UserMapper ile UserResponse'a dönüştürüyoruz.
        Optional<User> actualUserOptional = userService.getUserById(userId);

        // Optional'ın dolu olduğunu ve içinde doğru User objesinin olduğunu doğrula
        assertTrue(actualUserOptional.isPresent(), "User should be found");
        User actualUser = actualUserOptional.get(); // Optional'ın içindeki User objesini al

        // User objesini UserResponse'a dönüştür
        UserResponse actualResponse = userMapper.toResponse(actualUser); // <-- Buradaki düzeltme!

        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getId(), actualResponse.getId());
        assertEquals(expectedResponse.getUsername(), actualResponse.getUsername());
        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, times(1)).toResponse(userEntity);
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException when user not found by id")
    void getUserById_shouldThrowExceptionWhenUserNotFound() {
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(userId);
        });
        // Hata mesajını düzeltiyoruz
        assertEquals("Kullanıcı bulunamadı ID : '" + userId + "'", thrown.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, never()).toResponse(any(User.class)); // Kullanıcı bulunamadığı için mapper çağrılmamalı
    }

    @Test
    @DisplayName("should return all users successfully")
    void getAllUsers_shouldReturnAllUsers() {
        User user1 = User.builder().id(1L).username("user1").email("user1@example.com").build();
        User user2 = User.builder().id(2L).username("user2").email("user2@example.com").build();
        List<User> userEntities = Arrays.asList(user1, user2);

        UserResponse response1 = UserResponse.builder().id(1L).username("user1").email("user1@example.com").build();
        UserResponse response2 = UserResponse.builder().id(2L).username("user2").email("user2@example.com").build();
        List<UserResponse> expectedResponses = Arrays.asList(response1, response2);

        when(userRepository.findAll()).thenReturn(userEntities);
        // Her bir User entity'si için mapper'ın dönüşünü mockla
        when(userMapper.toResponse(user1)).thenReturn(response1);
        when(userMapper.toResponse(user2)).thenReturn(response2);

        List<UserResponse> actualResponses = userService.getAllUsers();

        assertNotNull(actualResponses);
        assertEquals(2, actualResponses.size());
        assertEquals(expectedResponses.get(0).getUsername(), actualResponses.get(0).getUsername());
        assertEquals(expectedResponses.get(1).getUsername(), actualResponses.get(1).getUsername());
        verify(userRepository, times(1)).findAll();
        verify(userMapper, times(1)).toResponse(user1);
        verify(userMapper, times(1)).toResponse(user2);
    }

    @Test
    @DisplayName("should delete a user successfully")
    void deleteUser_shouldDeleteUserSuccessfully() {
        Long userId = 1L;
        // Kullanıcı var mı kontrolü için existsById mockla
        when(userRepository.existsById(userId)).thenReturn(true);
        // deleteById metodunun çağrıldığında hiçbir şey yapmamasını sağla (void metodlar için doNothing)
        doNothing().when(userRepository).deleteById(userId);

        userService.deleteUser(userId);

        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    @DisplayName("should throw exception when user not found during delete")
    void deleteUser_shouldThrowExceptionWhenUserNotFound() {
        Long userId = 99L;
        when(userRepository.existsById(userId)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUser(userId);
        });

        // Hata mesajını düzeltiyoruz
        assertEquals("Kullanıcı", exception.getResourceName()); // Kaynak adı
        assertEquals("ID", exception.getFieldName());       // Alan adı
        assertEquals(userId, exception.getFieldValue());    // Alan değeri

        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, never()).findById(anyLong());    // findById çağrılmamalı
        verify(userRepository, never()).deleteById(anyLong());  // deleteById çağrılmamalı
    }

    @Test
    @DisplayName("should find user by username successfully")
    void getUserByUsername_shouldReturnUser() {
        String username = "testuser";
        User user = User.builder().id(1L).username(username).build();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Optional<User> foundUser = userService.getUserByUsername(username);

        assertTrue(foundUser.isPresent());
        assertEquals(username, foundUser.get().getUsername());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("should return empty optional when user not found by username")
    void getUserByUsername_shouldReturnEmptyOptional_whenNotFound() {
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        Optional<User> foundUser = userService.getUserByUsername(username);

        assertFalse(foundUser.isPresent());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("should find user by username or email when found by username")
    void findByUsernameOrEmail_shouldReturnUser_whenFoundByUsername() {
        String identifier = "testuser";
        User user = User.builder().id(1L).username(identifier).email("test@example.com").build();
        when(userRepository.findByUsername(identifier)).thenReturn(Optional.of(user));

        Optional<User> foundUser = userService.findByUsernameOrEmail(identifier);

        assertTrue(foundUser.isPresent());
        assertEquals(identifier, foundUser.get().getUsername());
        verify(userRepository, times(1)).findByUsername(identifier);
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("should find user by username or email when found by email")
    void findByUsernameOrEmail_shouldReturnUser_whenFoundByEmail() {
        String identifier = "test@example.com";
        when(userRepository.findByUsername(identifier)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(identifier)).thenReturn(Optional.of(User.builder().id(1L).username("testuser").email(identifier).build()));

        Optional<User> foundUser = userService.findByUsernameOrEmail(identifier);

        assertTrue(foundUser.isPresent());
        assertEquals(identifier, foundUser.get().getEmail());
        verify(userRepository, times(1)).findByUsername(identifier);
        verify(userRepository, times(1)).findByEmail(identifier);
    }

    @Test
    @DisplayName("should return empty optional when user not found by username or email")
    void findByUsernameOrEmail_shouldReturnEmptyOptional_whenNotFound() {
        String identifier = "nonexistent";
        when(userRepository.findByUsername(identifier)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(identifier)).thenReturn(Optional.empty());

        Optional<User> foundUser = userService.findByUsernameOrEmail(identifier);

        assertFalse(foundUser.isPresent());
        verify(userRepository, times(1)).findByUsername(identifier);
        verify(userRepository, times(1)).findByEmail(identifier);
    }
}
