package com.soliner.digitalcard.application.services.impl;

// KRİTİK: Gerekli tüm application.mapper importları
import com.soliner.digitalcard.application.mapper.ProjectMapper;
import com.soliner.digitalcard.application.mapper.SocialLinkMapper;
import com.soliner.digitalcard.application.mapper.UserMapper;

// KRİTİK: Gerekli tüm core.types.exceptions importları
import com.soliner.digitalcard.core.types.exceptions.InvalidInputException;
import com.soliner.digitalcard.core.types.exceptions.ResourceNotFoundException;

// KRİTİK: Gerekli tüm domain.model importları
import com.soliner.digitalcard.domain.model.ERole;
import com.soliner.digitalcard.domain.model.Role;
import com.soliner.digitalcard.domain.model.User;
import com.soliner.digitalcard.domain.model.SocialLink;
import com.soliner.digitalcard.domain.model.Project;

// KRİTİK: Gerekli tüm persistence.repository importları
import com.soliner.digitalcard.persistence.repository.RoleRepository;
import com.soliner.digitalcard.persistence.repository.UserRepository;

// KRİTİK: Gerekli tüm webApi.dto importları
import com.soliner.digitalcard.webApi.dto.auth.PasswordUpdateRequest;
import com.soliner.digitalcard.webApi.dto.user.UserRequest;
import com.soliner.digitalcard.webApi.dto.user.UserResponse;
import com.soliner.digitalcard.webApi.dto.sociallink.SocialLinkRequest;
import com.soliner.digitalcard.webApi.dto.project.ProjectRequest;


// JUnit ve Mockito importları
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

// Spring Security importları
import org.springframework.security.crypto.password.PasswordEncoder;

// Java util importları
import java.time.LocalDateTime; // LocalDateTime eklendi
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

// Static importlar (Mockito ve JUnit Assertions)
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * UserServiceImpl sınıfı için birim testleri.
 * Bu testler, UserServiceImpl'deki iş mantığını izole edilmiş bir ortamda doğrular.
 * Bağımlılıklar (UserRepository, UserMapper, PasswordEncoder, RoleRepository, SocialLinkMapper, ProjectMapper) Mockito kullanılarak mock'lanır.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SocialLinkMapper socialLinkMapper;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockUser;
    private UserRequest mockUserRequest;
    private UserResponse mockUserResponse; // mockUserResponse şimdi builder ile oluşturulacak
    private Role mockRole;

    @BeforeEach
    void setUp() {
        mockRole = Role.builder().id(1L).name(ERole.ROLE_USER).build();
        Set<Role> roles = new HashSet<>();
        roles.add(mockRole);

        mockUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .profilePhotoUrl("http://example.com/profile.jpg")
                .bio("A test user")
                .title("Developer")
                .address("Test City")
                .phoneNumber("1234567890")
                .website("http://test.com")
                .roles(roles)
                .socialLinks(new ArrayList<>())
                .projects(new ArrayList<>())
                .createdAt(LocalDateTime.now()) // createdAt eklendi
                .updatedAt(LocalDateTime.now()) // updatedAt eklendi
                .build();

        mockUserRequest = new UserRequest();
        mockUserRequest.setUsername("testuser");
        mockUserRequest.setEmail("test@example.com");
        mockUserRequest.setPassword("rawPassword123!"); // Şifre validasyonu için güncellendi
        mockUserRequest.setFirstName("Test");
        mockUserRequest.setLastName("User");
        mockUserRequest.setProfileImageUrl("http://example.com/profile.jpg"); // Frontend'den gelen isim
        mockUserRequest.setBio("A test user");
        mockUserRequest.setTitle("Developer");
        mockUserRequest.setLocation("Test City"); // Frontend'den gelen isim
        mockUserRequest.setPhone("1234567890"); // Frontend'den gelen isim
        mockUserRequest.setPortfolioUrl("http://test.com"); // Frontend'den gelen isim
        mockUserRequest.setSocialLinks(Collections.emptyList());
        mockUserRequest.setProjects(Collections.emptyList());

        // KRİTİK DÜZELTME: UserResponse builder ile oluşturuldu ve yeni alanlar eklendi
        mockUserResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .profileImageUrl("http://example.com/profile.jpg") // Response'taki field adı
                .bio("A test user")
                .title("Developer")
                .location("Test City") // Response'taki field adı
                .phone("1234567890") // Response'taki field adı
                .portfolioUrl("http://test.com") // Response'taki field adı
                .roles(Collections.singletonList("ROLE_USER"))
                .socialLinks(Collections.emptyList())
                .projects(Collections.emptyList())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("createUser - Başarılı Kullanıcı Oluşturma")
    void createUser_shouldReturnUserResponse() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(mockRole));
        // KRİTİK DÜZELTME: userMapper.toEntity yerine userMapper.toUser
        when(userMapper.toUser(any(UserRequest.class))).thenReturn(mockUser); 
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(userMapper.toResponse(any(User.class)))
                .thenReturn(mockUserResponse);
        
        UserResponse result = userService.createUser(mockUserRequest);

        assertNotNull(result);
        assertEquals(mockUserResponse.getUsername(), result.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
        verify(roleRepository, times(1)).findByName(ERole.ROLE_USER);
        // KRİTİK DÜZELTME: userMapper.toEntity yerine userMapper.toUser
        verify(userMapper, times(1)).toUser(any(UserRequest.class)); 
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userMapper, times(1)).toResponse(any(User.class));
        verify(socialLinkMapper, never()).toEntity(any(SocialLinkRequest.class)); 
        verify(projectMapper, never()).toEntity(any(ProjectRequest.class));
    }

    @Test
    @DisplayName("createUser - Kullanıcı Adı Zaten Mevcut Olduğunda InvalidInputException Fırlatma")
    void createUser_whenUsernameExists_shouldThrowInvalidInputException() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));

        assertThrows(InvalidInputException.class, () -> userService.createUser(mockUserRequest));
        verify(userRepository, never()).save(any(User.class));
        verify(userRepository, times(1)).findByUsername(anyString());
        verify(userRepository, never()).findByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(roleRepository, never()).findByName(any());
        // KRİTİK DÜZELTME: userMapper.toEntity yerine userMapper.toUser
        verify(userMapper, never()).toUser(any(UserRequest.class)); 
        verify(userMapper, never()).toResponse(any());
        verify(socialLinkMapper, never()).toEntity(any(SocialLinkRequest.class));
        verify(projectMapper, never()).toEntity(any(ProjectRequest.class));
    }

    @Test
    @DisplayName("createUser - E-posta Zaten Mevcut Olduğunda InvalidInputException Fırlatma")
    void createUser_whenEmailExists_shouldThrowInvalidInputException() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));

        assertThrows(InvalidInputException.class, () -> userService.createUser(mockUserRequest));
        verify(userRepository, never()).save(any(User.class));
        verify(userRepository, times(1)).findByUsername(anyString());
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(roleRepository, never()).findByName(any());
        // KRİTİK DÜZELTME: userMapper.toEntity yerine userMapper.toUser
        verify(userMapper, never()).toUser(any(UserRequest.class)); 
        verify(userMapper, never()).toResponse(any());
        verify(socialLinkMapper, never()).toEntity(any(SocialLinkRequest.class));
        verify(projectMapper, never()).toEntity(any(ProjectRequest.class));
    }

    @Test
    @DisplayName("createUser - ROLE_USER Bulunamadığında ResourceNotFoundException Fırlatma")
    void createUser_whenRoleUserNotFound_shouldThrowResourceNotFoundException() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        // KRİTİK DÜZELTME: userMapper.toEntity yerine userMapper.toUser
        when(userMapper.toUser(any(UserRequest.class))).thenReturn(mockUser); 
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.createUser(mockUserRequest));
        verify(userRepository, never()).save(any(User.class));
        verify(userRepository, times(1)).findByUsername(anyString());
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(passwordEncoder, times(1)).encode(anyString());
        // KRİTİK DÜZELTME: userMapper.toEntity yerine userMapper.toUser
        verify(userMapper, times(1)).toUser(any(UserRequest.class)); 
        verify(roleRepository, times(1)).findByName(ERole.ROLE_USER);
        verify(userMapper, never()).toResponse(any());
        verify(socialLinkMapper, never()).toEntity(any(SocialLinkRequest.class));
        verify(projectMapper, never()).toEntity(any(ProjectRequest.class));
    }

    @Test
    @DisplayName("updateUser - Başarılı Kullanıcı Güncelleme")
    void updateUser_shouldReturnUpdatedUserResponse() {
    	UserRequest updatedUserRequest = new UserRequest();
        updatedUserRequest.setUsername("updateduser");
        updatedUserRequest.setEmail("updated@example.com");
        updatedUserRequest.setPassword("newpassword123"); 
        updatedUserRequest.setFirstName("Updated");
        updatedUserRequest.setLastName("User");
        updatedUserRequest.setProfileImageUrl("http://example.com/updated.jpg");
        updatedUserRequest.setBio("An updated test user");
        updatedUserRequest.setTitle("Senior Developer");
        updatedUserRequest.setLocation("New City");
        updatedUserRequest.setPhone("0987654321");
        updatedUserRequest.setPortfolioUrl("http://updated.com");
        updatedUserRequest.setSocialLinks(Collections.singletonList(
                SocialLinkRequest.builder().platform("LinkedIn").url("http://linkedin.com/updated").build()
        ));
        updatedUserRequest.setProjects(Collections.singletonList(
                ProjectRequest.builder().title("New Project").description("New desc").build()
        ));


        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));
        // KRİTİK: updateEntityFromDto çağrısını doğrula
        doNothing().when(userMapper).updateEntityFromDto(any(UserRequest.class), any(User.class)); 
        
        when(userRepository.findByUsername(updatedUserRequest.getUsername())).thenReturn(Optional.empty()); 
        when(userRepository.findByEmail(updatedUserRequest.getEmail())).thenReturn(Optional.empty()); 
        
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(userMapper.toResponse(any(User.class)))
                .thenReturn(mockUserResponse);

        // KRİTİK: socialLinkMapper.toEntity ve projectMapper.toEntity doğrulamalarını SİL!
        // Çünkü bu çağrılar artık UserServiceImpl içinde değil, UserMapperImpl içinde yapılıyor.
        // when(socialLinkMapper.toEntity(any(SocialLinkRequest.class))).thenReturn(new SocialLink()); 
        // when(projectMapper.toEntity(any(ProjectRequest.class))).thenReturn(new Project());


        UserResponse result = userService.updateUser(1L, updatedUserRequest);

        assertNotNull(result);
        assertEquals(mockUserResponse.getUsername(), result.getUsername());
        verify(userRepository, times(1)).findById(eq(1L));
        // KRİTİK: Sadece updateEntityFromDto'nun çağrıldığını doğrula
        verify(userMapper, times(1)).updateEntityFromDto(eq(updatedUserRequest), any(User.class)); 
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toResponse(any(User.class));
        
        // KRİTİK: Aşağıdaki doğrulamaları SİL! Mapper'ın sorumluluğundadır.
        // verify(socialLinkMapper, times(updatedUserRequest.getSocialLinks().size())).toEntity(any(SocialLinkRequest.class));
        // verify(projectMapper, times(updatedUserRequest.getProjects().size())).toEntity(any(ProjectRequest.class));

        // KRİTİK: Mocked User objesi üzerinde addSocialLink ve addProject çağrılarının doğrulamalarını da SİL!
        // Çünkü bu çağrılar artık UserServiceImpl'den değil, MapStruct tarafından generate edilen mapper içinde yapılıyor.
        // verify(mockUser, times(updatedUserRequest.getSocialLinks().size())).addSocialLink(any(SocialLink.class));
        // verify(mockUser, times(updatedUserRequest.getProjects().size())).addProject(any(Project.class));
    }
    @Test
    @DisplayName("updateUser - Kullanıcı Bulunamadığında ResourceNotFoundException Fırlatma")
    void updateUser_whenUserNotFound_shouldThrowResourceNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(1L, mockUserRequest));
        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).updateEntityFromDto(any(), any());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userMapper, never()).toResponse(any());
        verify(socialLinkMapper, never()).toEntity(any(SocialLinkRequest.class));
        verify(projectMapper, never()).toEntity(any(ProjectRequest.class));
    }

    @Test
    @DisplayName("updateUser - Güncelleme Sırasında Kullanıcı Adı Çakışması")
    void updateUser_whenUsernameConflicts_shouldThrowInvalidInputException() {
        User existingUser = User.builder().id(1L).username("user1").email("user1@example.com").build();
        User conflictUser = User.builder().id(2L).username("testuser").email("test@example.com").build(); 
        UserRequest userRequestWithConflictingUsername = new UserRequest();
        userRequestWithConflictingUsername.setUsername("testuser"); 
        userRequestWithConflictingUsername.setEmail("user1@example.com"); 
        userRequestWithConflictingUsername.setPassword("newpassword123");
        userRequestWithConflictingUsername.setProfileImageUrl("http://example.com/img.jpg");
        userRequestWithConflictingUsername.setBio("bio");
        userRequestWithConflictingUsername.setTitle("title");
        userRequestWithConflictingUsername.setLocation("loc");
        userRequestWithConflictingUsername.setPhone("phone");
        userRequestWithConflictingUsername.setPortfolioUrl("url");


        when(userRepository.findById(anyLong())).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername(userRequestWithConflictingUsername.getUsername())).thenReturn(Optional.of(conflictUser));

        assertThrows(InvalidInputException.class, () -> userService.updateUser(1L, userRequestWithConflictingUsername));
        verify(userRepository, times(1)).findById(eq(1L));
        verify(userRepository, times(1)).findByUsername(userRequestWithConflictingUsername.getUsername());
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(userMapper, never()).toUser(any()); // KRİTİK DÜZELTME: toEntity yerine toUser
        verify(userMapper, never()).toResponse(any());
        verify(socialLinkMapper, never()).toEntity(any(SocialLinkRequest.class));
        verify(projectMapper, never()).toEntity(any(ProjectRequest.class));
    }

    @Test
    @DisplayName("getUserById - Başarılı Kullanıcı Getirme")
    void getUserById_shouldReturnUserResponse() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));
        when(userMapper.toResponse(any(User.class)))
                .thenReturn(mockUserResponse);

        UserResponse result = userService.getUserById(1L);
        assertNotNull(result);
        assertEquals(mockUserResponse.getUsername(), result.getUsername());
        verify(userRepository, times(1)).findById(anyLong());
        verify(userMapper, times(1)).toResponse(any(User.class));
    }

    @Test
    @DisplayName("getUserById - Kullanıcı Bulunamadığında ResourceNotFoundException Fırlatma")
    void getUserById_whenUserNotFound_shouldThrowResourceNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty()); 

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(1L));
        verify(userRepository, times(1)).findById(anyLong());
        verify(userMapper, never()).toResponse(any(User.class));
    }

    @Test
    @DisplayName("getUserByUsername - Başarılı Kullanıcı Getirme")
    void getUserByUsername_shouldReturnUserResponse() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));
        when(userMapper.toResponse(any(User.class)))
                .thenReturn(mockUserResponse);

        UserResponse result = userService.getUserByUsername("testuser");
        assertNotNull(result);
        assertEquals(mockUserResponse.getUsername(), result.getUsername());
        verify(userRepository, times(1)).findByUsername(anyString());
        verify(userMapper, times(1)).toResponse(any(User.class));
    }

    @Test
    @DisplayName("getUserByUsername - Kullanıcı Bulunamadığında ResourceNotFoundException Fırlatma")
    void getUserByUsername_whenUserNotFound_shouldThrowResourceNotFoundException() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserByUsername("nonexistent"));
        verify(userRepository, times(1)).findByUsername(anyString());
        verify(userMapper, never()).toResponse(any(User.class));
    }

    @Test
    @DisplayName("deleteUser - Başarılı Kullanıcı Silme")
    void deleteUser_shouldDeleteUser() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(userRepository).deleteById(anyLong());

        userService.deleteUser(1L);

        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteUser - Kullanıcı Bulunamadığında ResourceNotFoundException Fırlatma")
    void deleteUser_whenUserNotFound_shouldThrowResourceNotFoundException() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUser(1L);
        });

        assertEquals("User", exception.getResourceName());
        assertEquals("ID", exception.getFieldName());
        assertEquals(1L, exception.getFieldValue());

        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("updatePassword - Başarılı Şifre Güncelleme")
    void updatePassword_shouldUpdatePassword() {
    	// Test senaryosu için kullanıcı ve şifre bilgilerini hazırla
        Long userId = 1L;
        String oldPassword = "oldPass";
        String newPassword = "newPass123";
        String newEncodedPassword = "newEncodedPass"; // Mock olarak dönecek hash
        String originalEncodedPassword = "oldEncodedPass"; // Kullanıcının başlangıçtaki hashlenmiş şifresi

        // Mock User objesini oluştur ve eski şifre hash'ini ayarla
        User user = new User();
        user.setId(userId);
        user.setPasswordHash(originalEncodedPassword); // Başlangıçtaki hashlenmiş şifre
        
        // Mock davranışlarını tanımla
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        // passwordEncoder.matches çağrıldığında, doğru eski şifre için true dönsün
        when(passwordEncoder.matches(oldPassword, originalEncodedPassword)).thenReturn(true); // Buradaki originalEncodedPassword önemli
        // passwordEncoder.encode çağrıldığında, yeni hashlenmiş şifreyi dönsün
        when(passwordEncoder.encode(newPassword)).thenReturn(newEncodedPassword);
        // userRepository.save çağrıldığında, gelen user objesini geri dönsün
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Şifre güncelleme isteği DTO'sunu oluştur
        PasswordUpdateRequest request = new PasswordUpdateRequest(oldPassword, newPassword);

        // Metodu çağır (artık bir değer döndürmüyor)
        userService.updatePassword(userId, request);

        // Doğrulamalar: İlgili metotların çağrılıp çağrılmadığını kontrol et
        verify(userRepository, times(1)).findById(userId); // findById bir kez çağrıldı mı?
        // KRİTİK DÜZELTME: passwordEncoder.matches doğrulamasını düzelt
        // İkinci parametre olarak user objesinin orijinal hash'lenmiş şifresini beklemeli.
        verify(passwordEncoder, times(1)).matches(oldPassword, originalEncodedPassword); 
        verify(passwordEncoder, times(1)).encode(newPassword); // encode bir kez çağrıldı mı?
        verify(userRepository, times(1)).save(user); // save bir kez çağrıldı mı?

        // Şifrenin User objesi üzerinde gerçekten güncellendiğini doğrulama
        assertEquals(newEncodedPassword, user.getPasswordHash());
        assertNotNull(user.getUpdatedAt()); // updatedAt alanının null olmadığını doğrula (güncellendiğini varsayarak)
    }

    @Test
    @DisplayName("updatePassword - Kullanıcı Bulunamadığında ResourceNotFoundException Fırlatma")
    void updatePassword_whenUserNotFound_shouldThrowResourceNotFoundException() {
        PasswordUpdateRequest request = new PasswordUpdateRequest("oldPass", "newPass");
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updatePassword(1L, request));
        verify(userRepository, times(1)).findById(anyLong());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).toResponse(any(User.class));
    }

    @Test
    @DisplayName("updatePassword - Mevcut Şifre Yanlış Olduğunda InvalidInputException Fırlatma")
    void updatePassword_whenOldPasswordInvalid_shouldThrowInvalidInputException() {
        PasswordUpdateRequest request = new PasswordUpdateRequest("wrongPass", "newPass123");
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongPass", mockUser.getPasswordHash())).thenReturn(false);

        assertThrows(InvalidInputException.class, () -> userService.updatePassword(1L, request));
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).toResponse(any(User.class));
    }


    @Test
    @DisplayName("getAllUsers - Tüm Kullanıcıları Getirme")
    void getAllUsers_shouldReturnListOfUserResponses() {
        User user1 = User.builder().id(1L).username("user1").email("user1@example.com").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        User user2 = User.builder().id(2L).username("user2").email("user2@example.com").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        // KRİTİK DÜZELTME: UserResponse builder ile oluşturuldu ve tüm alanlar dahil edildi
        UserResponse response1 = UserResponse.builder()
            .id(1L).username("user1").email("user1@example.com").firstName("User").lastName("One")
            .profileImageUrl("url1").bio("bio1").title("title1").location("loc1").phone("phone1").portfolioUrl("port1")
            .roles(Collections.singletonList("ROLE_USER")).socialLinks(Collections.emptyList()).projects(Collections.emptyList())
            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        UserResponse response2 = UserResponse.builder()
            .id(2L).username("user2").email("user2@example.com").firstName("User").lastName("Two")
            .profileImageUrl("url2").bio("bio2").title("title2").location("loc2").phone("phone2").portfolioUrl("port2")
            .roles(Collections.singletonList("ROLE_USER")).socialLinks(Collections.emptyList()).projects(Collections.emptyList())
            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(userMapper.toResponseList(anyList())).thenReturn(Arrays.asList(response1, response2));

        List<UserResponse> result = userService.getAllUsers();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getUsername());
        assertEquals("user2", result.get(1).getUsername());
        verify(userRepository, times(1)).findAll();
        verify(userMapper, times(1)).toResponseList(anyList());
    }

    @Test
    @DisplayName("findByUsernameOrEmail - Kullanıcı Adı ile Başarılı Bulma")
    void findByUsernameOrEmail_shouldReturnUser_whenFoundByUsername() {
        String identifier = "testuser";
        User user = mockUser;
        when(userRepository.findByUsername(identifier)).thenReturn(Optional.of(user));

        Optional<User> foundUser = userService.findByUsernameOrEmail(identifier);

        assertTrue(foundUser.isPresent());
        assertEquals(identifier, foundUser.get().getUsername());
        verify(userRepository, times(1)).findByUsername(identifier);
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("findByUsernameOrEmail - E-posta ile Başarılı Bulma")
    void findByUsernameOrEmail_shouldReturnUser_whenFoundByEmail() {
        String identifier = "test@example.com";
        User user = mockUser;
        when(userRepository.findByUsername(identifier)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(identifier)).thenReturn(Optional.of(user));

        Optional<User> foundUser = userService.findByUsernameOrEmail(identifier);

        assertTrue(foundUser.isPresent());
        assertEquals(identifier, foundUser.get().getEmail());
        verify(userRepository, times(1)).findByUsername(identifier);
        verify(userRepository, times(1)).findByEmail(identifier);
    }

    @Test
    @DisplayName("findByUsernameOrEmail - Kullanıcı Adı veya E-posta ile Bulunamama")
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
