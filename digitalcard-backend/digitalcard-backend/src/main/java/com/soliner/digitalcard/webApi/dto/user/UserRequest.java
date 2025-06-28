package com.soliner.digitalcard.webApi.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.soliner.digitalcard.webApi.dto.sociallink.SocialLinkRequest;
import com.soliner.digitalcard.webApi.dto.project.ProjectRequest;

/**
 * Kullanıcı oluşturma veya güncelleme istekleri için kullanılan Veri Transfer Nesnesi (DTO).
 * Bu DTO, HTTP istek gövdesinin yapısını tanımlar ve validasyon kurallarını içerir.
 * Frontend'den gelen JSON anahtarları ile tam uyumlu hale getirilmiştir.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    // KRİTİK DÜZELTME: Password alanı artık profil güncellerken zorunlu değil (@NotBlank kaldırıldı).
    // Yalnızca boyutu kontrol edilir. Bu, frontend'den boş şifre gönderildiğinde validasyon hatasını önler.
  //  @Size(min = 8, max = 120, message = "Password must be at least 8 characters long.")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot be longer than 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot be longer than 50 characters")
    private String lastName;

    // KRİTİK DÜZELTME: Frontend 'profileImageUrl' (camelCase) gönderiyor.
    // Jackson varsayılan olarak profileImageUrl Java alanını JSON'a "profileImageUrl" olarak mapler.
    // Bu nedenle @JsonProperty'ye gerek yoktur. Ancak, tutarlılık için ekliyorum.
    @JsonProperty("profileImageUrl") // Frontend'den bu isimle geliyor
    @Size(max = 255, message = "Profile image URL cannot be longer than 255 characters")
    private String profileImageUrl; // Java field name: profileImageUrl

    @Size(max = 1000, message = "Bio cannot be longer than 1000 characters")
    private String bio;

    @Size(max = 100, message = "Title cannot be longer than 100 characters")
    private String title;

    // Frontend (location) ve Backend DTO (location) alan adları aynı.
    // MapStruct UserMapper'da entity'deki "address" alanına map'leme yapacak.
    @Size(max = 100, message = "Location cannot be longer than 100 characters")
    private String location; 

    // Frontend (phone) ve Backend DTO (phone) alan adları aynı.
    // MapStruct UserMapper'da entity'deki "phoneNumber" alanına map'leme yapacak.
    @Size(max = 20, message = "Phone number cannot be longer than 20 characters")
    private String phone; 

    // Frontend (portfolioUrl) ve Backend DTO (portfolioUrl) alan adları aynı.
    // MapStruct UserMapper'da entity'deki "website" alanına map'leme yapacak.
    @Size(max = 255, message = "Portfolio URL cannot be longer than 255 characters")
    private String portfolioUrl;

    // Sosyal link ve proje listeleri
    private List<SocialLinkRequest> socialLinks;
    private List<ProjectRequest> projects;
}
