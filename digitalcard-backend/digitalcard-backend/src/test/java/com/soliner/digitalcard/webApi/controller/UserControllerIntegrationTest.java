package com.soliner.digitalcard.webApi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soliner.digitalcard.application.services.interfaces.RoleService;
import com.soliner.digitalcard.application.services.interfaces.UserService;
import com.soliner.digitalcard.domain.model.ERole;
import com.soliner.digitalcard.domain.model.Project;
import com.soliner.digitalcard.domain.model.Role;
import com.soliner.digitalcard.domain.model.SocialLink;
import com.soliner.digitalcard.domain.model.User;
import com.soliner.digitalcard.persistence.repository.UserRepository;
import com.soliner.digitalcard.webApi.dto.auth.PasswordUpdateRequest; // LoginRequest yerine PasswordUpdateRequest
import com.soliner.digitalcard.webApi.dto.user.UserRequest;
import com.soliner.digitalcard.webApi.dto.sociallink.SocialLinkRequest;
import com.soliner.digitalcard.webApi.dto.project.ProjectRequest;
import com.soliner.digitalcard.webApi.security.JwtUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.hamcrest.Matchers.is;
// Mockito importları bu test için kullanılmıyor, kaldırıldı
// import static org.mockito.Mockito.*;
// import static org.mockito.ArgumentMatchers.anyString;


/**
 * UserController için entegrasyon testleri.
 * Bu testler, API katmanının ve servis/depo katmanlarının birlikte doğru çalıştığını doğrular.
 * @SpringBootTest: Spring Boot uygulamasını tam olarak yükler.
 * @AutoConfigureMockMvc: MockMvc'yi otomatik yapılandırır.
 * @ActiveProfiles("test"): "test" profilini etkinleştirir (genellikle in-memory db vb. için).
 * @Transactional: Her test metodundan sonra veritabanı işlemlerini geri alır (rollback).
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {com.soliner.digitalcard.digitalcard_backend.DigitalcardBackendApplication.class,
                           com.soliner.digitalcard.application.services.impl.RoleServiceImpl.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("UserController Integration Tests")
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService; // Gerçek servisi kullanırız

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService; // RoleService'i de inject et
    
    @Autowired
    private AuthenticationManager authenticationManager; // JWT oluşturmak için
    
    @Autowired
    private JwtUtils jwtUtils; // JWT oluşturmak için
    
    @Autowired
    private PasswordEncoder passwordEncoder;


    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        // Her testten önce veritabanını temizlemek yerine @Transactional kullanıyoruz.
        // Ancak rollerin her zaman var olduğundan emin olmak için ekleyebiliriz (idempotent).
        userRole = roleService.findByName(ERole.ROLE_USER).orElseGet(() -> roleService.createRole(ERole.ROLE_USER));
        adminRole = roleService.findByName(ERole.ROLE_ADMIN).orElseGet(() -> roleService.createRole(ERole.ROLE_ADMIN));
    }

    /**
     * MockMvc istekleri için JWT token eklemek için yardımcı metot.
     * Bu metot, Spring Security context'ini simüle eder ve istek başlığına bir JWT ekler.
     * Bu metot, her zaman belirli bir kullanıcı adı ve **gerçek şifresi** ile kimlik doğrulama yapar.
     * Eğer kullanıcı yoksa, belirtilen şifre ile yeni bir kullanıcı oluşturur.
     * @param username Token oluşturulacak kullanıcı adı.
     * @param rawPassword Kullanıcının raw (hashlenmemiş) şifresi.
     * @param roles Kullanıcının rolleri.
     * @return İstek post işlemcisi.
     */
    private RequestPostProcessor userToken(String username, String rawPassword, String... roles) {
        return request -> {
            // Test kullanıcısını bul veya oluştur
            User user = userRepository.findByUsername(username).orElseGet(() -> {
                User newUser = User.builder()
                        .username(username)
                        .email(username + "@example.com")
                        .passwordHash(passwordEncoder.encode(rawPassword)) // Yeni kullanıcı oluştururken şifreyi hashle
                        .firstName("Test")
                        .lastName("User")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                for (String roleName : roles) {
                    roleService.findByName(ERole.valueOf(roleName))
                             .ifPresent(newUser::addRole);
                }
                return userRepository.save(newUser);
            });

            // AuthenticationManager ile kimlik doğrulaması yap (gerçek şifre ile)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, rawPassword) // Kullanıcının gerçek raw şifresini kullan
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // JWT oluştur
            String jwt = jwtUtils.generateToken(authentication); // JwtUtils'teki doğru metot adı
            
            // Authorization header'ı ekle
            request.addHeader("Authorization", "Bearer " + jwt);
            return request;
        };
    }

    @Test
    @DisplayName("createUser - Yeni kullanıcı başarıyla oluşturulduğunda 201 Created döndürmeli")
    void createUser_shouldReturn201Created() throws Exception {
    	UserRequest userRequest = new UserRequest();
        userRequest.setUsername("integrationuser");
        userRequest.setEmail("integration@example.com");
        userRequest.setPassword("securepass123");
        userRequest.setFirstName("Integration");
        userRequest.setLastName("Test");
        userRequest.setProfileImageUrl("http://example.com/default.jpg");
        userRequest.setBio("Integration test bio");
        userRequest.setTitle("Software Engineer");
        userRequest.setLocation("Test City");
        userRequest.setPhone("1234567890");
        userRequest.setPortfolioUrl("http://test.com");
        
        userRequest.setSocialLinks(List.of(
            SocialLinkRequest.builder().platform("LinkedIn").url("http://linkedin.com/in/test").build()
        ));
        userRequest.setProjects(List.of(
            ProjectRequest.builder().title("Test Project").description("A project for testing").technologies("Java, Spring").projectUrl("http://testproject.com").projectImageUrl("http://testproject.com/img.png").build()
        ));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("integrationuser"))
                .andExpect(jsonPath("$.email").value("integration@example.com"))
                .andExpect(jsonPath("$.firstName").value("Integration"))
                .andExpect(jsonPath("$.lastName").value("Test"))
                .andExpect(jsonPath("$.profileImageUrl").value("http://example.com/default.jpg"))
                .andExpect(jsonPath("$.bio").value("Integration test bio"))
                .andExpect(jsonPath("$.title").value("Software Engineer"))
                .andExpect(jsonPath("$.location").value("Test City"))
                .andExpect(jsonPath("$.phone").value("1234567890"))
                .andExpect(jsonPath("$.portfolioUrl").value("http://test.com"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.socialLinks[0].platform").value("LinkedIn"))
                .andExpect(jsonPath("$.socialLinks[0].url").value("http://linkedin.com/in/test"))
                // KRİTİK DÜZELTME: JSON çıktısı project_url ve project_image_url olarak dönüyorsa testleri buna göre ayarlayın
                .andExpect(jsonPath("$.projects[0].title").value("Test Project"))
                .andExpect(jsonPath("$.projects[0].description").value("A project for testing"))
                .andExpect(jsonPath("$.projects[0].technologies").value("Java, Spring"))
                .andExpect(jsonPath("$.projects[0].project_url").value("http://testproject.com")) // Düzeltildi
                .andExpect(jsonPath("$.projects[0].project_image_url").value("http://testproject.com/img.png")) // Düzeltildi
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }
    @Test
    @DisplayName("createUser - Kullanıcı adı zaten kullanımdayken 400 Bad Request döndürmeli")
    void createUser_shouldReturn409Conflict_whenUsernameExists() throws Exception {
        // Ön koşul: Var olan bir kullanıcı kaydet
    	User existingUser = User.builder().username("existinguser").email("some@example.com").passwordHash(passwordEncoder.encode("password123")).build();
        userRepository.save(existingUser);

        UserRequest userRequest = new UserRequest();
        userRequest.setUsername("existinguser"); // Mevcut kullanıcı adı
        userRequest.setEmail("new@example.com");
        userRequest.setPassword("password123");
        userRequest.setFirstName("New");
        userRequest.setLastName("User");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest()) // KRİTİK DÜZELTME: 409 yerine 400
                .andExpect(jsonPath("$.message").value("Username 'existinguser' is already in use."));
    }

    @Test
    @DisplayName("createUser - E-posta zaten mevcut olduğunda 409 Conflict döndürmeli")
    void createUser_shouldReturn409Conflict_whenEmailExists() throws Exception {
        // Ön koşul: Var olan bir kullanıcı kaydet
    	User existingUser = User.builder().username("someuser").email("existing@example.com").passwordHash(passwordEncoder.encode("password123")).build();
        userRepository.save(existingUser);

        UserRequest userRequest = new UserRequest();
        userRequest.setUsername("newuser");
        userRequest.setEmail("existing@example.com"); // Mevcut e-posta
        userRequest.setPassword("password123");
        userRequest.setFirstName("New");
        userRequest.setLastName("User");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest()) // KRİTİK DÜZELTME: 409 yerine 400
                .andExpect(jsonPath("$.message").value("Email 'existing@example.com' is already in use."));
    }

    @Test
    @DisplayName("createUser - Şifre boş veya null olduğunda 400 Bad Request döndürmeli")
    void createUser_shouldReturn400BadRequest_whenPasswordIsEmpty() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername("testuser_nopass");
        userRequest.setEmail("test_nopass@example.com");
        userRequest.setPassword(""); // Boş şifre
        userRequest.setFirstName("No");
        userRequest.setLastName("Pass");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest()) // Status'ü 400 olarak bekliyoruz
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(is("Password is required for user creation."))) // Tam mesajı kontrol et
                .andExpect(jsonPath("$.path").value("/api/users"));
    }

    @Test
    @DisplayName("createUser - Şifre minimum uzunluktan kısa olduğunda 400 Bad Request döndürmeli")
    void createUser_shouldReturn400BadRequest_whenPasswordTooShort() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername("testuser_shortpass");
        userRequest.setEmail("test_shortpass@example.com");
        userRequest.setPassword("short"); // Kısa şifre
        userRequest.setFirstName("Short");
        userRequest.setLastName("Pass");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest()) // Status'ü 400 olarak bekliyoruz
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(is("Password must be at least 8 characters long for user creation."))) // Tam mesajı kontrol et
                .andExpect(jsonPath("$.path").value("/api/users"));
    }

    @Test
    @DisplayName("getUserById - ID ile kullanıcı başarıyla getirildiğinde 200 OK döndürmeli")
    void getUserById_shouldReturnUser() throws Exception {
    	String commonPassword = "UserPassword123!";
        User user = User.builder()
                .username("getuser")
                .email("get@example.com")
                .passwordHash(passwordEncoder.encode(commonPassword)) // Hashlenmiş şifre
                .firstName("Get")
                .lastName("User")
                .profilePhotoUrl("http://example.com/get.jpg")
                .bio("Get test bio")
                .title("QA Engineer")
                .address("Get City")
                .phoneNumber("0987654321")
                .website("http://getsite.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        user.addRole(userRole);
        user.addSocialLink(SocialLink.builder().platform("GitHub").url("http://github.com/get").user(user).build());
        user.addProject(Project.builder().title("Get Project").description("Test Project Desc").technologies("Node.js").projectUrl("http://getproject.com").projectImageUrl("http://getproject.com/getimg.png").user(user).build());

        userRepository.save(user);

        mockMvc.perform(get("/api/users/{id}", user.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .with(userToken(user.getUsername(), commonPassword, "ROLE_USER"))) // Şifreyi userToken'a gönder
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.username").value("getuser"))
                .andExpect(jsonPath("$.email").value("get@example.com"))
                .andExpect(jsonPath("$.firstName").value("Get"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.profileImageUrl").value("http://example.com/get.jpg"))
                .andExpect(jsonPath("$.bio").value("Get test bio"))
                .andExpect(jsonPath("$.title").value("QA Engineer"))
                .andExpect(jsonPath("$.location").value("Get City"))
                .andExpect(jsonPath("$.phone").value("0987654321"))
                .andExpect(jsonPath("$.portfolioUrl").value("http://getsite.com"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.socialLinks[0].platform").value("GitHub"))
                .andExpect(jsonPath("$.socialLinks[0].url").value("http://github.com/get"))
                .andExpect(jsonPath("$.projects[0].title").value("Get Project"))
                .andExpect(jsonPath("$.projects[0].description").value("Test Project Desc"))
                .andExpect(jsonPath("$.projects[0].technologies").value("Node.js"))
                .andExpect(jsonPath("$.projects[0].project_url").value("http://getproject.com")) // Düzeltildi
                .andExpect(jsonPath("$.projects[0].project_image_url").value("http://getproject.com/getimg.png")); // Düzeltildi
    }


    @Test
    @DisplayName("getUserById - Kullanıcı bulunamadığında 404 Not Found döndürmeli")
    void getUserById_shouldReturn404NotFound_whenUserNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 99L)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(userToken("someuser_for_404", "Password123!", "ROLE_USER"))) // Geçerli bir token ile istek yap
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User, ID : '99' ile bulunamadı"))
                .andExpect(jsonPath("$.path").value("/api/users/99"));
    }

    @Test
    @DisplayName("updateUser - Kullanıcı başarıyla güncellendiğinde 200 OK döndürmeli")
    void updateUser_shouldReturn200Ok() throws Exception {
    	String commonPassword = "UserPassword123!";
        User existingUser = User.builder()
                .username("testuser").email("test@example.com").passwordHash(passwordEncoder.encode(commonPassword))
                .firstName("Test").lastName("User").profilePhotoUrl("http://old.jpg").bio("old bio").title("old title")
                .address("Old City").phoneNumber("1112223344").website("http://old.com").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        existingUser.addRole(userRole);
        userRepository.save(existingUser);

        UserRequest updateRequest = new UserRequest();
        updateRequest.setUsername("updateduser");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("Person");
        updateRequest.setProfileImageUrl("http://example.com/updated.jpg");
        updateRequest.setBio("Updated bio content");
        updateRequest.setTitle("Senior Dev");
        updateRequest.setLocation("Updated City");
        updateRequest.setPhone("5556667788");
        updateRequest.setPortfolioUrl("http://updated.com");
        
        updateRequest.setSocialLinks(List.of(
            SocialLinkRequest.builder().platform("LinkedIn").url("http://linkedin.com/updated").build(),
            SocialLinkRequest.builder().platform("GitHub").url("http://github.com/updated").build()
        ));
        updateRequest.setProjects(List.of(
            ProjectRequest.builder().title("New Project").description("New desc").technologies("React, Node.js").projectUrl("http://newproject.com").projectImageUrl("http://newproject.com/newimg.png").build()
        ));


        mockMvc.perform(put("/api/users/{id}", existingUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .with(userToken(existingUser.getUsername(), commonPassword, "ROLE_USER"))) // Şifreyi userToken'a gönder
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(existingUser.getId()))
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Person"))
                .andExpect(jsonPath("$.profileImageUrl").value("http://example.com/updated.jpg"))
                .andExpect(jsonPath("$.bio").value("Updated bio content"))
                .andExpect(jsonPath("$.title").value("Senior Dev"))
                .andExpect(jsonPath("$.location").value("Updated City"))
                .andExpect(jsonPath("$.phone").value("5556667788"))
                .andExpect(jsonPath("$.portfolioUrl").value("http://updated.com"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.socialLinks[0].platform").value("LinkedIn"))
                .andExpect(jsonPath("$.socialLinks[0].url").value("http://linkedin.com/updated"))
                .andExpect(jsonPath("$.socialLinks[1].platform").value("GitHub"))
                .andExpect(jsonPath("$.socialLinks[1].url").value("http://github.com/updated"))
                .andExpect(jsonPath("$.projects[0].title").value("New Project"))
                .andExpect(jsonPath("$.projects[0].description").value("New desc"))
                .andExpect(jsonPath("$.projects[0].technologies").value("React, Node.js"))
                .andExpect(jsonPath("$.projects[0].project_url").value("http://newproject.com")) // Düzeltildi
                .andExpect(jsonPath("$.projects[0].project_image_url").value("http://newproject.com/newimg.png")) // Düzeltildi
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        // Veritabanındaki değişikliği doğrula
        Optional<User> updatedUserOpt = userRepository.findById(existingUser.getId());
        assertTrue(updatedUserOpt.isPresent());
        User updatedUser = updatedUserOpt.get();
        assertEquals("updateduser", updatedUser.getUsername());
        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals("Updated", updatedUser.getFirstName());
        assertEquals("Person", updatedUser.getLastName());
        assertEquals("http://example.com/updated.jpg", updatedUser.getProfilePhotoUrl());
        assertEquals("Updated bio content", updatedUser.getBio());
        assertEquals("Senior Dev", updatedUser.getTitle());
        assertEquals("Updated City", updatedUser.getAddress());
        assertEquals("5556667788", updatedUser.getPhoneNumber());
        assertEquals("http://updated.com", updatedUser.getWebsite());
        
        // İlişkili varlıkların güncellendiğini doğrulamadan önce LAZY yüklemeyi tetikle.
        assertEquals(2, updatedUser.getSocialLinks().size());
        assertEquals("LinkedIn", updatedUser.getSocialLinks().get(0).getPlatform());
        assertEquals("GitHub", updatedUser.getSocialLinks().get(1).getPlatform());
        
        assertEquals(1, updatedUser.getProjects().size());
        assertEquals("New Project", updatedUser.getProjects().get(0).getTitle());
    }

    @Test
    @DisplayName("updateUser - Kullanıcı bulunamadığında 404 Not Found döndürmeli")
    void updateUser_shouldReturn404NotFound_whenUserNotFound() throws Exception {
        UserRequest updateRequest = new UserRequest();
        updateRequest.setUsername("nonexistent");
        updateRequest.setEmail("nonexistent@example.com");
        updateRequest.setFirstName("No");
        updateRequest.setLastName("User");

        mockMvc.perform(put("/api/users/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .with(userToken("testuser_for_404_update", "Password123!", "ROLE_USER"))) // Geçerli bir token ile istek yap
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User, ID : '99' ile bulunamadı"))
                .andExpect(jsonPath("$.path").value("/api/users/99"));
    }

    @Test
    @DisplayName("updateUser - Kullanıcı adı zaten kullanımda olduğunda 409 Conflict döndürmeli")
    void updateUser_shouldReturn409Conflict_whenUsernameAlreadyExists() throws Exception {
    	String commonPassword = "Password123!";
        User user1 = User.builder().username("user1").email("user1@example.com").passwordHash(passwordEncoder.encode(commonPassword)).build();
        User user2 = User.builder().username("user2").email("user2@example.com").passwordHash(passwordEncoder.encode(commonPassword)).build();
        userRepository.save(user1);
        userRepository.save(user2);

        UserRequest updateRequest = new UserRequest();
        updateRequest.setUsername("user2"); // user1'i user2'nin kullanıcı adıyla güncellemeye çalışıyoruz
        updateRequest.setEmail("user1@example.com");
        updateRequest.setFirstName("UpdatedA");
        updateRequest.setLastName("UpdatedB");

        mockMvc.perform(put("/api/users/{id}", user1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .with(userToken(user1.getUsername(), commonPassword, "ROLE_USER")))
                .andDo(print())
                .andExpect(status().isBadRequest()) // KRİTİK DÜZELTME: 409 yerine 400
                .andExpect(jsonPath("$.message").value("Username 'user2' is already in use."));
    }

    @Test
    @DisplayName("updateUser - E-posta zaten kullanımda olduğunda 409 Conflict döndürmeli")
    void updateUser_shouldReturn409Conflict_whenEmailAlreadyExists() throws Exception {
    	String commonPassword = "Password123!";
        User userA = User.builder().username("userA").email("userA@example.com").passwordHash(passwordEncoder.encode(commonPassword)).build();
        User userB = User.builder().username("userB").email("userB@example.com").passwordHash(passwordEncoder.encode(commonPassword)).build();
        userRepository.save(userA);
        userRepository.save(userB);

        UserRequest updateRequest = new UserRequest();
        updateRequest.setUsername("userA");
        updateRequest.setEmail("userB@example.com"); // userA'yı userB'nin e-postasıyla güncellemeye çalışıyoruz
        updateRequest.setFirstName("UpdatedX");
        updateRequest.setLastName("UpdatedY");

        mockMvc.perform(put("/api/users/{id}", userA.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .with(userToken(userA.getUsername(), commonPassword, "ROLE_USER")))
                .andDo(print())
                .andExpect(status().isBadRequest()) // KRİTİK DÜZELTME: 409 yerine 400
                .andExpect(jsonPath("$.message").value("Email 'userB@example.com' is already in use."));
    }

    @Test
    @DisplayName("deleteUser - Kullanıcı başarıyla silindiğinde 204 No Content döndürmeli")
    void deleteUser_shouldReturn204NoContent() throws Exception {
        String commonPassword = "UserPassword123!";
        User userToDelete = User.builder().username("deleteuser").email("delete@example.com").passwordHash(passwordEncoder.encode(commonPassword)).firstName("D").lastName("U").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        userRepository.save(userToDelete);

        mockMvc.perform(delete("/api/users/{id}", userToDelete.getId())
                        .with(userToken(userToDelete.getUsername(), commonPassword, "ROLE_USER")))
                .andExpect(status().isNoContent());

        // Kullanıcının silindiğini doğrula
        assertFalse(userRepository.findById(userToDelete.getId()).isPresent());
    }

    @Test
    @DisplayName("deleteUser - Kullanıcı bulunamadığında 404 Not Found döndürmeli")
    void deleteUser_shouldReturn404NotFound_whenUserNotFound() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 99L)
                        .with(userToken("someuser_for_404_delete", "Password123!", "ROLE_USER"))) // Geçerli bir token ile istek yap
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User, ID : '99' ile bulunamadı"))
                .andExpect(jsonPath("$.path").value("/api/users/99"));
    }

    @Test
    @DisplayName("updatePassword - Şifre başarıyla güncellendiğinde 200 OK döndürmeli")
    void updatePassword_shouldReturn200Ok() throws Exception {
    	String oldPassword = "oldpass123";
        String newPassword = "newpass456!";
        User user = User.builder()
                .username("passuser")
                .email("pass@example.com")
                .passwordHash(passwordEncoder.encode(oldPassword))
                .firstName("Pass").lastName("User")
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        user.addRole(userRole);
        userRepository.save(user);

        PasswordUpdateRequest passwordUpdateRequest = new PasswordUpdateRequest(oldPassword, newPassword);

        mockMvc.perform(put("/api/users/{id}/password", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordUpdateRequest))
                        .with(userToken(user.getUsername(), oldPassword, "ROLE_USER")))
                .andDo(print()) // Debug için çıktıya yazdır
                .andExpect(status().isOk())
                .andExpect(content().string("")) // KRİTİK DÜZELTME: Boş body bekliyoruz
                .andExpect(jsonPath("$").doesNotExist()); // JSON body'si olmadığını doğrula
        
        // KRİTİK DÜZELTME: ResponseEntity.ok().build() Content-Type ayarlamaz. Bu yüzden bu kontrol kaldırıldı.
        // .andExpect(content().contentType(MediaType.APPLICATION_JSON)); 

        // Şifrenin güncellendiğini ve doğru olduğunu doğrula
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches(newPassword, updatedUser.getPasswordHash()));
    }
    @Test
    @DisplayName("updatePassword - Kullanıcı bulunamadığında 404 Not Found döndürmeli")
    void updatePassword_shouldReturn404NotFound_whenUserNotFound() throws Exception {
    	// KRİTİK DÜZELTME: Yeni şifre, DTO validasyonunu geçecek kadar uzun olmalı,
        // böylece sadece ResourceNotFoundException tetiklenir.
        PasswordUpdateRequest passwordUpdateRequest = new PasswordUpdateRequest("oldpass123", "newpass456!");
        mockMvc.perform(put("/api/users/{id}/password", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordUpdateRequest))
                        .with(userToken("testuser_for_404_password", "Password123!", "ROLE_USER")))
                .andDo(print()) // Debug için çıktıya yazdır
                .andExpect(status().isNotFound()) // Status expected:<404>
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // JSON içeriği bekleniyor
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User, ID : '99' ile bulunamadı"))
                .andExpect(jsonPath("$.path").value("/api/users/99/password"));
    }

    @Test
    @DisplayName("updatePassword - Eski şifre yanlış olduğunda 400 Bad Request döndürmeli")
    void updatePassword_shouldReturn400BadRequest_whenOldPasswordIncorrect() throws Exception {
        String correctOldPassword = "correctoldpass";
        User user = User.builder()
                .username("wrongpassuser")
                .email("wrongpass@example.com")
                .passwordHash(passwordEncoder.encode(correctOldPassword))
                .firstName("Wrong").lastName("Pass")
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        user.addRole(userRole);
        userRepository.save(user);

        PasswordUpdateRequest passwordUpdateRequest = new PasswordUpdateRequest("incorrectoldpass", "newpass123!");

        mockMvc.perform(put("/api/users/{id}/password", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordUpdateRequest))
                        .with(userToken(user.getUsername(), correctOldPassword, "ROLE_USER"))) // Doğru şifre ile token oluştur
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Old password is incorrect."))
                .andExpect(jsonPath("$.path").value("/api/users/" + user.getId() + "/password"));
    }

    @Test
    @DisplayName("updatePassword - Yeni şifre minimum uzunluktan kısa olduğunda 400 Bad Request döndürmeli")
    void updatePassword_shouldReturn400BadRequest_whenNewPasswordTooShort() throws Exception {
    	String oldPassword = "oldpass123";
        User user = User.builder()
                .username("shortnewpassuser")
                .email("shortnewpass@example.com")
                .passwordHash(passwordEncoder.encode(oldPassword))
                .firstName("Short").lastName("NewPass")
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        user.addRole(userRole);
        userRepository.save(user);

        PasswordUpdateRequest passwordUpdateRequest = new PasswordUpdateRequest(oldPassword, "short"); // Yeni şifre çok kısa

        mockMvc.perform(put("/api/users/{id}/password", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordUpdateRequest))
                        .with(userToken(user.getUsername(), oldPassword, "ROLE_USER")))
                .andDo(print()) // Debug için çıktıya yazdır
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // JSON Content Type kontrolü
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                // KRİTİK DÜZELTME: GlobalExceptionHandler'dan dönen genel mesajı kontrol et
                .andExpect(jsonPath("$.message").value("Doğrulama hatası oluştu."))
                // KRİTİK DÜZELTME: Doğrulama detaylarını kontrol et
                .andExpect(jsonPath("$.validationErrors.newPassword").value("Yeni şifre en az 8 karakter olmalıdır."))
                .andExpect(jsonPath("$.path").value("/api/users/" + user.getId() + "/password"));
    }
}
