package com.soliner.digitalcard.webApi.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor; // Lombok import edildi
import lombok.Data;
import lombok.Getter;     // Lombok import edildi
import lombok.NoArgsConstructor; // Lombok import edildi
import lombok.Setter;     // Lombok import edildi

/**
 * Şifre güncelleme isteği için kullanılan veri transfer nesnesi (DTO).
 * Mevcut şifre ve yeni şifre bilgilerini içerir.
 * webApi katmanına aittir.
 */
@Data
@Getter // Lombok ile getter metodları otomatik oluşturulacak
@Setter // Lombok ile setter metodları otomatik oluşturulacak
@NoArgsConstructor // Lombok ile argümansız constructor otomatik oluşturulacak
@AllArgsConstructor // Lombok ile tüm alanları içeren constructor otomatik oluşturulacak
public class PasswordUpdateRequest {

    @NotBlank(message = "Mevcut şifre boş olamaz.")
    private String oldPassword; // KRİTİK DÜZELTME: 'currentPassword' yerine 'oldPassword' kullanıldı

    @NotBlank(message = "Yeni şifre boş olamaz.")
    @Size(min = 8, message = "Yeni şifre en az 8 karakter olmalıdır.") // Önceki min 6 idi, 8'e yükseltmek isterseniz manuel değiştirebilirsiniz.
    private String newPassword;

    // KRİTİK DÜZELTME: Manuel constructor'lar, getter'lar ve setter'lar kaldırıldı.
    // Bunları Lombok otomatik olarak sağlayacaktır.
    /*
    public PasswordUpdateRequest() {}

    public PasswordUpdateRequest(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
    */
}
