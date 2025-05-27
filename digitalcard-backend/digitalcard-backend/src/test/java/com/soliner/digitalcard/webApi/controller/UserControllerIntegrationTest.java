package com.soliner.digitalcard.webApi.controller;
//
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soliner.digitalcard.digitalcard_backend.DigitalcardBackendApplication;
import com.soliner.digitalcard.domain.model.User;
import com.soliner.digitalcard.persistence.repository.UserRepository;
import com.soliner.digitalcard.webApi.dto.user.UserRequest;
import com.soliner.digitalcard.webApi.dto.user.UserResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.soliner.digitalcard.webApi.config.TestSecurityConfig;
import org.springframework.context.annotation.Import;


/**
 * UserController için entegrasyon testleri.
 * Bu testler, Controller, Service, Repository ve (in-memory) veritabanı gibi tüm katmanların
 * bir arada doğru çalıştığını doğrular.
 */
@SpringBootTest(classes = DigitalcardBackendApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("UserController Integration Tests")
@Import(TestSecurityConfig.class)
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("should create a new user and return 201 Created status")
    void createUser_shouldReturn201Created() throws Exception {
        // Arrange (Hazırlık): Test için gerekli veriyi ayarla
        UserRequest userRequest = UserRequest.builder()
                .username("integrationuser")
                .email("integration@example.com")
                .password("TestPass123!") // GÜNCELLENDİ: Büyük harf, küçük harf, rakam, özel karakter içeriyor
                .firstName("Integration") // EKLE: @NotBlank kuralına uyacak şekilde
                .lastName("Test")        // EKLE: @NotBlank kuralına uyacak şekilde
                .build();

        // Act (Eylem): MockMvc kullanarak POST isteği yap
        ResultActions response = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)));

        // Assert (Doğrulama): Yanıtı ve veritabanı durumunu doğrula
        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is(userRequest.getUsername())))
                .andExpect(jsonPath("$.email", is(userRequest.getEmail())))
                .andExpect(jsonPath("$.id").exists());

        // Veritabanında kullanıcının gerçekten oluşturulduğunu doğrula
        Optional<User> createdUserOptional = userRepository.findByUsername(userRequest.getUsername());
        assertTrue(createdUserOptional.isPresent());
        User createdUser = createdUserOptional.get();
        assertEquals(userRequest.getUsername(), createdUser.getUsername());
        assertEquals(userRequest.getEmail(), createdUser.getEmail());
        assertTrue(passwordEncoder.matches(userRequest.getPassword(), createdUser.getPasswordHash()));
        assertEquals(userRequest.getFirstName(), createdUser.getFirstName());
        assertEquals(userRequest.getLastName(), createdUser.getLastName());
    }

    @Test
    @DisplayName("should return 409 Conflict when creating user with existing username")
    void createUser_shouldReturn409Conflict_whenUsernameExists() throws Exception {
        // Arrange: Önce var olan bir kullanıcı oluştur ve kaydet
        User existingUser = User.builder()
                .username("existinguser")
                .email("existing@example.com")
                .passwordHash(passwordEncoder.encode("ExistingPass1!")) // GÜNCELLENDİ
                .firstName("Existing") // EKLE
                .lastName("User")     // EKLE
                .build();
        userRepository.save(existingUser);

        // Yeni bir UserRequest oluştur, ancak var olan kullanıcı adını kullan
        UserRequest userRequest = UserRequest.builder()
                .username("existinguser")
                .email("new@example.com")
                .password("NewPass123!") // GÜNCELLENDİ
                .firstName("New")      // EKLE
                .lastName("Person")   // EKLE
                .build();

        // Act: MockMvc kullanarak POST isteği yap
        ResultActions response = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)));

        // Assert: HTTP 409 Conflict durumu ve beklenen hata mesajını doğrula
        response.andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Kullanıcı adı 'existinguser' zaten mevcut.")))
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    @DisplayName("should return 409 Conflict when creating user with existing email")
    void createUser_shouldReturn409Conflict_whenEmailExists() throws Exception {
        // Arrange: Önce var olan bir kullanıcı oluştur ve kaydet
        User existingUser = User.builder()
                .username("uniqueuser")
                .email("existing@example.com")
                .passwordHash(passwordEncoder.encode("ExistingPass1!")) // GÜNCELLENDİ
                .firstName("Unique")   // EKLE
                .lastName("User")      // EKLE
                .build();
        userRepository.save(existingUser);

        // Yeni bir UserRequest oluştur, ancak var olan e-posta adresini kullan
        UserRequest userRequest = UserRequest.builder()
                .username("anotheruser")
                .email("existing@example.com")
                .password("NewPass123!") // GÜNCELLENDİ
                .firstName("Another") // EKLE
                .lastName("Person")   // EKLE
                .build();

        // Act
        ResultActions response = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)));

        // Assert
        response.andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("E-posta adresi 'existing@example.com' zaten kayıtlı.")))
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    @DisplayName("should get user by ID and return 200 OK status")
    void getUserById_shouldReturnUser() throws Exception {
        // Arrange: Önce bir kullanıcı oluştur ve kaydet
        User user = User.builder()
                .username("getuser")
                .email("get@example.com")
                .passwordHash(passwordEncoder.encode("GetPass123!")) // GÜNCELLENDİ
                .firstName("Get")      // EKLE
                .lastName("User")     // EKLE
                .build();
        User savedUser = userRepository.save(user);

        // Act: MockMvc kullanarak GET isteği yap
        ResultActions response = mockMvc.perform(get("/api/users/{id}", savedUser.getId())
                .accept(MediaType.APPLICATION_JSON));

        // Assert: Yanıtı doğrula
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedUser.getId().intValue())))
                .andExpect(jsonPath("$.username", is(savedUser.getUsername())))
                .andExpect(jsonPath("$.email", is(savedUser.getEmail())));
    }

    @Test
    @DisplayName("should return 404 Not Found when getting non-existing user by ID")
    void getUserById_shouldReturn404NotFound_whenUserDoesNotExist() throws Exception {
        // Arrange: Mevcut olmayan bir ID belirle
        Long nonExistingId = 99L;

        // Act
        ResultActions response = mockMvc.perform(get("/api/users/{id}", nonExistingId)
                .accept(MediaType.APPLICATION_JSON));

        // Assert
        response.andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Kullanıcı bulunamadı ID : '" + nonExistingId + "'")))
                .andExpect(jsonPath("$.status", is(404)));
    }
    
    @Test
    @DisplayName("should update an existing user and return 200 OK status")
    void updateUser_shouldReturn200Ok() throws Exception {
        // Arrange: Önce güncellenecek bir kullanıcı oluştur
        User originalUser = User.builder()
                .username("originaluser")
                .email("original@example.com")
                .passwordHash(passwordEncoder.encode("OldPass123!")) // GÜNCELLENDİ
                .firstName("Original")
                .lastName("User")
                .build();
        User savedUser = userRepository.save(originalUser);

        // Güncelleme isteği için yeni veriler
        UserRequest updatedUserRequest = UserRequest.builder()
                .username("updateduser")
                .email("updated@example.com")
                .password("NewPass456@") // GÜNCELLENDİ
                .firstName("Updated")
                .lastName("Person")
                .build();

        // Act: MockMvc kullanarak PUT isteği yap
        ResultActions response = mockMvc.perform(put("/api/users/{id}", savedUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUserRequest)));

        // Assert: Yanıtı doğrula
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedUser.getId().intValue())))
                .andExpect(jsonPath("$.username", is(updatedUserRequest.getUsername())))
                .andExpect(jsonPath("$.email", is(updatedUserRequest.getEmail())));

        // Veritabanında kullanıcının gerçekten güncellendiğini doğrula
        Optional<User> updatedUserOptional = userRepository.findById(savedUser.getId());
        assertTrue(updatedUserOptional.isPresent());
        User actualUpdatedUser = updatedUserOptional.get();
        assertEquals(updatedUserRequest.getUsername(), actualUpdatedUser.getUsername());
        assertEquals(updatedUserRequest.getEmail(), actualUpdatedUser.getEmail());
        assertTrue(passwordEncoder.matches(updatedUserRequest.getPassword(), actualUpdatedUser.getPasswordHash()));
        assertEquals(updatedUserRequest.getFirstName(), actualUpdatedUser.getFirstName());
        assertEquals(updatedUserRequest.getLastName(), actualUpdatedUser.getLastName());
    }

    @Test
    @DisplayName("should return 404 Not Found when updating non-existing user")
    void updateUser_shouldReturn404NotFound_whenUserDoesNotExist() throws Exception {
        // Arrange
        Long nonExistingId = 99L;
        UserRequest userRequest = UserRequest.builder()
                .username("nonexistent")
                .email("nonexistent@example.com")
                .password("Pass123!") // GÜNCELLENDİ
                .firstName("Non")     // EKLE
                .lastName("Existent") // EKLE
                .build();

        // Act
        ResultActions response = mockMvc.perform(put("/api/users/{id}", nonExistingId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)));

        // Assert
        response.andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Kullanıcı bulunamadı ID : '" + nonExistingId + "'")))
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("should return 409 Conflict when updating user with existing username")
    void updateUser_shouldReturn409Conflict_whenUsernameAlreadyExists() throws Exception {
        // Arrange: İki kullanıcı oluştur
        User user1 = User.builder().username("user1").email("user1@example.com").passwordHash(passwordEncoder.encode("Pass123!")).firstName("User").lastName("One").build(); // GÜNCELLENDİ
        User user2 = User.builder().username("user2").email("user2@example.com").passwordHash(passwordEncoder.encode("Pass456@")).firstName("User").lastName("Two").build(); // GÜNCELLENDİ
        userRepository.save(user1);
        userRepository.save(user2);

        // user1'i user2'nin kullanıcı adıyla güncellemeye çalış
        UserRequest updateRequest = UserRequest.builder()
                .username("user2")
                .email("user1@example.com")
                .password("NewPass789#") // GÜNCELLENDİ
                .firstName("Updated")    // EKLE
                .lastName("User")        // EKLE
                .build();

        // Act
        ResultActions response = mockMvc.perform(put("/api/users/{id}", user1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        // Assert
        response.andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Kullanıcı adı 'user2' zaten mevcut.")));
        // Veritabanında user1'in değişmediğini doğrula
        User stillUser1 = userRepository.findById(user1.getId()).orElseThrow();
        assertEquals("user1", stillUser1.getUsername());
    }

    @Test
    @DisplayName("should return 409 Conflict when updating user with existing email")
    void updateUser_shouldReturn409Conflict_whenEmailAlreadyExists() throws Exception {
        // Arrange: İki kullanıcı oluştur
        User user1 = User.builder().username("userA").email("userA@example.com").passwordHash(passwordEncoder.encode("PassA123!")).firstName("User").lastName("A").build();
        User user2 = User.builder().username("userB").email("userB@example.com").passwordHash(passwordEncoder.encode("PassB456@")).firstName("User").lastName("B").build();
        userRepository.save(user1);
        userRepository.save(user2);

        // user1'i user2'nin e-posta adresiyle güncellemeye çalış
        UserRequest updateRequest = UserRequest.builder()
                .username("userA_updated")
                .email("userB@example.com") // Var olan e-posta adresi
                .password("NewPassC789#")
                .firstName("UpdatedUser")
                .lastName("A")
                .build();

        // Act
        ResultActions response = mockMvc.perform(put("/api/users/{id}", user1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        // Assert
        response.andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("E-posta adresi 'userB@example.com' zaten kayıtlı.")));

        // Veritabanında user1'in değişmediğini doğrula (sadece e-posta kısmı)
        User stillUser1 = userRepository.findById(user1.getId()).orElseThrow();
        assertEquals("userA@example.com", stillUser1.getEmail());
    }

    @Test
    @DisplayName("should delete an existing user and return 204 No Content status")
    void deleteUser_shouldReturn204NoContent() throws Exception {
        // Arrange: Silinecek bir kullanıcı oluştur
        User user = User.builder()
                .username("todelete")
                .email("delete@example.com")
                .passwordHash(passwordEncoder.encode("DeletePass1!")) // GÜNCELLENDİ
                .firstName("To")     // EKLE
                .lastName("Delete")  // EKLE
                .build();
        User savedUser = userRepository.save(user);

        // Act: MockMvc kullanarak DELETE isteği yap
        ResultActions response = mockMvc.perform(delete("/api/users/{id}", savedUser.getId()));

        // Assert: Yanıtı ve veritabanı durumunu doğrula
        response.andDo(print())
                .andExpect(status().isNoContent());

        // Veritabanında kullanıcının gerçekten silindiğini doğrula
        Optional<User> deletedUser = userRepository.findById(savedUser.getId());
        assertFalse(deletedUser.isPresent());
    }

    @Test
    @DisplayName("should return 404 Not Found when deleting non-existing user")
    void deleteUser_shouldReturn404NotFound_whenUserDoesNotExist() throws Exception {
        // Arrange: Mevcut olmayan bir ID belirle
        Long nonExistingId = 99L;

        // Act
        ResultActions response = mockMvc.perform(delete("/api/users/{id}", nonExistingId));

        // Assert
        response.andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Kullanıcı bulunamadı ID : '" + nonExistingId + "'")))
                .andExpect(jsonPath("$.status", is(404)));

    }
}