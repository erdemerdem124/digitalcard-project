package com.soliner.digitalcard.webApi.controller; // 'contoller' -> 'controller' olarak düzeltildi

import com.soliner.digitalcard.application.services.interfaces.UserService; // UserService arayüzünü import ediyoruz
import com.soliner.digitalcard.webApi.dto.user.UserRequest; // UserRequest DTO'yu import ediyoruz
import com.soliner.digitalcard.webApi.dto.user.UserResponse; // UserResponse DTO'yu import ediyoruz
import com.soliner.digitalcard.core.types.exceptions.ResourceNotFoundException; // ResourceNotFoundException'ı import edin
import com.soliner.digitalcard.domain.model.User; // Domain katmanındaki User Entity'si
import com.soliner.digitalcard.application.mapper.UserMapper;

import jakarta.validation.Valid; // İstek gövdesi validasyonu için
import org.springframework.http.HttpStatus; // HTTP durum kodları için
import org.springframework.http.ResponseEntity; // API yanıtları için
import org.springframework.web.bind.annotation.*; // RESTful anotasyonlar için

import java.util.List;
import java.util.Optional; // Optional'ı import edin

/**
 * Kullanıcılarla ilgili RESTful API endpoint'lerini yöneten Controller sınıfı.
 * Gelen HTTP isteklerini işler, servis katmanını çağırır ve DTO'lar aracılığıyla yanıt döner.
 * webApi katmanına aittir.
 */
@RestController // Bu sınıfın bir REST Controller olduğunu belirtir
@RequestMapping("/api/users") // Tüm endpoint'ler için temel URL yolu (örn: /api/users)
public class UserController {

	private final UserMapper userMapper;
    private final UserService userService; // Kullanıcı iş mantığı için servis katmanı

    // Constructor Injection ile bağımlılığı enjekte ediyoruz
    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    /**
     * Tüm kullanıcıları listeler.
     * HTTP Metodu: GET
     * Endpoint: /api/users
     * @return Kullanıcıların listesini içeren UserResponse nesneleri ve 200 OK durumu.
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> userResponses = userService.getAllUsers(); // Servis katmanından UserResponse DTO listesini al
        return ResponseEntity.ok(userResponses); // 200 OK yanıtı ile DTO listesini dön
    }

    /**
     * Belirli bir ID'ye sahip kullanıcıyı getirir.
     * HTTP Metodu: GET
     * Endpoint: /api/users/{id}
     * @param id Kullanıcının benzersiz ID'si (URL yolundan alınır).
     * @return Bulunan kullanıcıya ait UserResponse nesnesi ve 200 OK durumu veya bulunamazsa 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        // userService.getUserById(id) artık doğrudan User nesnesi döndürüyor
        // veya kullanıcı bulunamazsa ResourceNotFoundException fırlatıyor.
        User user = userService.getUserById(id); // <-- .orElseThrow() kaldırıldı

        // UserMapper kullanarak User entity'sini UserResponse DTO'ya dönüştürüyoruz.
        UserResponse userResponse = userMapper.toResponse(user);
        
        return ResponseEntity.ok(userResponse); // 200 OK yanıtı ile DTO'yu dön
    }


    /**
     * Belirli bir kullanıcı adına sahip kullanıcıyı getirir.
     * HTTP Metodu: GET
     * Endpoint: /api/users/username/{username}
     * @param username Kullanıcının kullanıcı adı (URL yolundan alınır).
     * @return Bulunan kullanıcıya ait UserResponse nesnesi ve 200 OK durumu veya bulunamazsa 404 Not Found.
     */
    @GetMapping("/username/{username}") // <-- YENİ EKLEDİĞİMİZ ENDPOINT BU!
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        Optional<User> userOptional = userService.findByUsernameOrEmail(username); // findByUsernameOrEmail kullanıldı
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("Kullanıcı", "kullanıcı adı", username);
        }

        // Dönüştürme işlemini MapStruct üstleniyor:
        UserResponse userResponse = userMapper.toResponse(userOptional.get());
        System.out.println("UserController: getUserByUsername - Dönüştürülen UserResponse: " + userResponse);
        return ResponseEntity.ok(userResponse);
    }


    /**
     * Yeni bir kullanıcı oluşturur.
     * HTTP Metodu: POST
     * Endpoint: /api/users
     * @param userRequest Oluşturulacak kullanıcının bilgilerini içeren UserRequest DTO'su (istek gövdesinden alınır).
     * @return Oluşturulan kullanıcıya ait UserResponse nesnesi ve 201 Created durumu.
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest userRequest) {
        // Gelen UserRequest DTO otomatik olarak valide edilir.
        // Eğer validasyon başarısız olursa, MethodArgumentNotValidException fırlatılır
        // ve GlobalExceptionHandler tarafından yakalanır.

        UserResponse createdUser = userService.createUser(userRequest); // Servis katmanında kullanıcıyı oluştur
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED); // 201 Created yanıtı ile DTO'yu dön
    }

    /**
     * Mevcut bir kullanıcıyı günceller.
     * HTTP Metodu: PUT
     * Endpoint: /api/users/{id}
     * @param id Güncellenecek kullanıcının benzersiz ID'si.
     * @param userRequest Güncelleme bilgilerini içeren UserRequest DTO'su.
     * @return Güncellenen kullanıcıya ait UserResponse nesnesi ve 200 OK durumu.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest userRequest) {
        UserResponse updatedUser = userService.updateUser(id, userRequest); // Servis katmanında kullanıcıyı güncelle
        return ResponseEntity.ok(updatedUser); // 200 OK yanıtı ile DTO'yu dön
    }

    /**
     * Belirli bir ID'ye sahip kullanıcıyı siler.
     * HTTP Metodu: DELETE
     * Endpoint: /api/users/{id}
     * @param id Silinecek kullanıcının benzersiz ID'si.
     * @return 204 No Content durumu.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // Başarılı silme durumunda 204 No Content yanıtı dönülmesini sağlar
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id); // Servis katmanında kullanıcıyı sil
        // Servis katmanından bir istisna gelmezse (ResourceNotFoundException gibi),
        // Spring otomatik olarak 204 No Content döner.
    }
}
