// PATH: src/main/java/com/soliner/digitalcard/webApi/dto/auth/LoginRequest.java
package com.soliner.digitalcard.webApi.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kullanıcı giriş isteği için kullanılan veri transfer nesnesi (DTO).
 * Kullanıcı adı (veya e-posta) ve şifre bilgilerini içerir.
 * webApi katmanına aittir.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Username or email is required")
    // KRİTİK DÜZELTME: AuthController'daki authenticateUser metodu ile uyumlu olması için 'username' olarak değiştirildi.
    // Spring Security'nin UsernamePasswordAuthenticationToken sınıfı da 'username' bekler.
    private String username;
    
    @NotBlank(message = "Password is required")
    private String password;
}
