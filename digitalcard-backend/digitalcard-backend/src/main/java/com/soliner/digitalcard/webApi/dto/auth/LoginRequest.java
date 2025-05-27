package com.soliner.digitalcard.webApi.dto.auth;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Kullanıcı giriş isteği için kullanılan veri transfer nesnesi (DTO).
 * Kullanıcı adı (veya e-posta) ve şifre bilgilerini içerir.
 * webApi katmanına aittir.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Kullanıcı adı veya e-posta boş olamaz.")
    private String usernameOrEmail; // Kullanıcı adı veya e-posta ile giriş yapılabilir

    @NotBlank(message = "Şifre boş olamaz.")
    private String password;
}

