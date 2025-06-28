// PATH: src/main/java/com/soliner/digitalcard/webApi/dto/auth/RegisterRequest.java
package com.soliner.digitalcard.webApi.dto.auth;

import com.soliner.digitalcard.webApi.dto.user.UserRequest; // UserRequest import edildi
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size; // Size anotasyonu için import edildi
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kullanıcı kayıt isteği için kullanılan veri transfer nesnesi (DTO).
 * Kullanıcı kayıt bilgilerini içerir ve validasyon kurallarını barındırır.
 * webApi katmanına aittir.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Kullanıcı adı boş olamaz")
    @Size(min = 3, max = 20, message = "Kullanıcı adı 3 ila 20 karakter arasında olmalıdır") // Kullanıcı adı uzunluk validasyonu
    private String username;
    
    @NotBlank(message = "E-posta boş olamaz")
    @Size(max = 50, message = "E-posta 50 karakterden uzun olamaz") // E-posta uzunluk validasyonu
    @Email(message = "Geçersiz e-posta formatı")
    private String email;
    
    @NotBlank(message = "Şifre boş olamaz")
    @Size(min = 6, max = 40, message = "Şifre 6 ila 40 karakter arasında olmalıdır") // Şifre uzunluk validasyonu
    private String password;

    @NotBlank(message = "Ad boş olamaz")
    @Size(max = 50, message = "Ad 50 karakterden uzun olamaz") // Ad uzunluk validasyonu
    private String firstName;

    @NotBlank(message = "Soyad boş olamaz")
    @Size(max = 50, message = "Soyad 50 karakterden uzun olamaz") // Soyad uzunluk validasyonu
    private String lastName;

    /**
     * Bu RegisterRequest DTO'sundan bir UserRequest DTO'su oluşturur.
     * Bu, register endpoint'inden gelen verileri, UserService'in beklediği genel UserRequest yapısına dönüştürmek için kullanılır.
     * UserRequest'in ek alanları (bio, title, location vb.) burada boş kalacaktır,
     * çünkü kayıt sırasında bu bilgiler genellikle alınmaz.
     * @return Yeni bir UserRequest nesnesi.
     */
    public UserRequest toUserRequest() {
        return UserRequest.builder()
                .username(this.username)
                .email(this.email)
                .password(this.password)
                .firstName(this.firstName) // KRİTİK DÜZELTME: firstName eklendi
                .lastName(this.lastName)   // KRİTİK DÜZELTME: lastName eklendi
                // UserRequest'in diğer alanları (profileImageUrl, bio, title, location, phone, portfolioUrl, socialLinks, projects)
                // kayıt sırasında verilmediği için null/boş olarak kalacaktır.
                .build();
    }
}
