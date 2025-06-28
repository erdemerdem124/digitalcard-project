package com.soliner.digitalcard.webApi.controller;

import com.soliner.digitalcard.application.services.interfaces.UserService;
import com.soliner.digitalcard.webApi.dto.auth.PasswordUpdateRequest;
import com.soliner.digitalcard.webApi.dto.user.UserRequest;
import com.soliner.digitalcard.webApi.dto.user.UserResponse;
// import com.soliner.digitalcard.core.types.exceptions.ResourceNotFoundException; // Artık doğrudan fırlatıldığı için burada import'a gerek kalmayabilir.
// import com.soliner.digitalcard.domain.model.User; // UserController'da doğrudan User Entity'si kullanılmadığı için kaldırıldı
// import com.soliner.digitalcard.application.mapper.UserMapper; // userService doğrudan UserResponse döndürdüğü için kaldırıldı

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
// import java.util.Optional; // findByUsernameOrEmail metodu hala Optional döndürdüğü için kalsın.

/**
 * Kullanıcılarla ilgili RESTful API endpoint'lerini yöneten Controller sınıfı.
 * Gelen HTTP isteklerini işler, servis katmanını çağırır ve DTO'lar aracılığıyla yanıt döner.
 * webApi katmanına aittir.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    // private final UserMapper userMapper; // Artık burada doğrudan kullanılmadığı için kaldırıldı
    private final UserService userService; // Kullanıcı iş mantığı için servis katmanı

    // Constructor Injection ile bağımlılığı enjekte ediyoruz
    public UserController(UserService userService/*, UserMapper userMapper*/) { // UserMapper kaldırıldı
        this.userService = userService;
        // this.userMapper = userMapper; // Kaldırıldı
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
        // KRİTİK DÜZELTME: userService.getUserById(id) zaten UserResponse döndürüyor.
        // Bu yüzden User entity'sine dönüştürmeye gerek yok, doğrudan kullanıyoruz.
        UserResponse userResponse = userService.getUserById(id);
        
        return ResponseEntity.ok(userResponse); // 200 OK yanıtı ile DTO'yu dön
    }


    /**
     * Belirli bir kullanıcı adına sahip kullanıcıyı getirir.
     * HTTP Metodu: GET
     * Endpoint: /api/users/username/{username}
     * @param username Kullanıcının kullanıcı adı (URL yolundan alınır).
     * @return Bulunan kullanıcıya ait UserResponse nesnesi ve 200 OK durumu veya bulunamazsa 404 Not Found.
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        // KRİTİK DÜZELTME: userService.getUserByUsername(username) zaten UserResponse döndürüyor.
        // Optional<User> ve userMapper.toResponse çağrısına gerek kalmadı.
        UserResponse userResponse = userService.getUserByUsername(username); 
        // System.out.println("UserController: getUserByUsername - Dönüştürülen UserResponse: " + userResponse); // Debug satırını kaldırabilirsiniz
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
        UserResponse createdUser = userService.createUser(userRequest);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
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
        UserResponse updatedUser = userService.updateUser(id, userRequest);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Belirli bir ID'ye sahip kullanıcıyı siler.
     * HTTP Metodu: DELETE
     * Endpoint: /api/users/{id}
     * @param id Silinecek kullanıcının benzersiz ID'si.
     * @return 204 No Content durumu.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
    @PutMapping("/{id}/password") // Bu endpoint ve metot eksikti, şimdi ekliyoruz
    public ResponseEntity<Void> updatePassword(@PathVariable Long id, @Valid @RequestBody PasswordUpdateRequest request) {
        // UserService'deki updatePassword metodunu çağırıyoruz.
        // Bu metodun UserService arayüzünüzde ve implementasyonunuzda mevcut olduğundan emin olun.
        userService.updatePassword(id, request);
        // Şifre güncellendiğinde genellikle bir içerik döndürmeyiz, sadece başarılı olduğunu belirtiriz.
        return ResponseEntity.ok().build(); // 200 OK yanıtı
    }
}
