package com.soliner.digitalcard.webApi.dto.user; // PAKET YOLU GÜNCELLENDİ

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Kullanıcı oluşturma veya güncelleme istekleri için kullanılan Veri Transfer Nesnesi (DTO).
 * Bu DTO, HTTP istek gövdesinin yapısını tanımlar ve validasyon kurallarını içerir.
 * webApi katmanına aittir.
 */
@Builder 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

	// @NotBlank(message = "Kullanıcı adı boş bırakılamaz.")
	// @Size(min = 3, max = 50, message = "Kullanıcı adı 3 ile 50 karakter arasında olmalıdır.")
	private String username;

	// @NotBlank(message = "E-posta boş bırakılamaz.")
	// @Email(message = "Geçerli bir e-posta adresi girin.")
	private String email;

	// @NotBlank(message = "Şifre boş bırakılamaz.")
	// @Size(min = 8, message = "Şifre en az 8 karakter olmalıdır.")
	// @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
//	          message = "Şifre en az bir rakam, bir küçük harf, bir büyük harf, bir özel karakter içermeli ve boşluk içermemelidir.")
	private String password;

	// @NotBlank(message = "İsim boş bırakılamaz.")
	// @Size(max = 50, message = "İsim en fazla 50 karakter olmalıdır.")
	private String firstName;

	// @NotBlank(message = "Soyisim boş bırakılamaz.")
	// @Size(max = 50, message = "Soyisim en fazla 50 karakter olmalıdır.")
	private String lastName;	

    // Önceki versiyonda olan alanlar (profil fotoğrafı ve biyografi) da buraya eklenebilir,
    // ancak validasyon anotasyonları ile birlikte.
    //@Size(max = 255, message = "Profil fotoğrafı URL'si 255 karakterden uzun olamaz")
    private String profilePhotoUrl;

    //@Size(max = 1000, message = "Biyografi 1000 karakterden uzun olamaz")
    private String bio;
}
