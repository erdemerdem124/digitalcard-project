// com.soliner.digitalcard.webApi.dto.auth.PasswordUpdateRequest.java
package com.soliner.digitalcard.webApi.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordUpdateRequest {

    @NotBlank(message = "Mevcut şifre boş bırakılamaz.")
    private String currentPassword;

    @NotBlank(message = "Yeni şifre boş bırakılamaz.")
    @Size(min = 8, message = "Yeni şifre en az 8 karakter olmalıdır.")
    // Ek şifre karmaşıklığı validasyonları eklenebilir (Regex pattern gibi)
    private String newPassword;

    // Constructors, getters, setters
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
}